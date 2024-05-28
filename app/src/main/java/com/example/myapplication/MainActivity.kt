package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.Recipe
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var recipeListView: ListView
    private lateinit var addRecipeButton: Button
    private lateinit var logoutButton: Button
    private lateinit var setSubscriptionPriceButton: Button
    private lateinit var mySubscriptionsButton: Button
    private lateinit var recipes: MutableList<Recipe>
    private lateinit var adapter: RecipeAdapter
    private lateinit var currentUsername: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        currentUsername = sharedPref.getString("username", null) ?: ""

        database = FirebaseDatabase.getInstance().reference.child("recipes")
        recipeListView = findViewById(R.id.recipeListView)
        addRecipeButton = findViewById(R.id.addRecipeButton)
        logoutButton = findViewById(R.id.logoutButton)
        setSubscriptionPriceButton = findViewById(R.id.setSubscriptionPriceButton)
        mySubscriptionsButton = findViewById(R.id.mySubscriptionsButton)
        recipes = mutableListOf()
        adapter = RecipeAdapter(this, recipes)

        recipeListView.adapter = adapter

        addRecipeButton.setOnClickListener {
            startActivity(Intent(this, AddRecipeActivity::class.java))
        }

        logoutButton.setOnClickListener {
            val editor = sharedPref.edit()
            editor.clear()
            editor.apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        setSubscriptionPriceButton.setOnClickListener {
            showSetPriceDialog()
        }

        mySubscriptionsButton.setOnClickListener {
            startActivity(Intent(this, MySubscriptionsActivity::class.java))
        }

        loadRecipes()
    }

    private fun loadRecipes() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                recipes.clear()
                for (recipeSnapshot in snapshot.children) {
                    val recipe = recipeSnapshot.getValue(Recipe::class.java)
                    if (recipe != null) {
                        recipes.add(recipe)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun showSetPriceDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
        val priceEditText = dialogView.findViewById<EditText>(R.id.priceEditText)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.confirmButton).setOnClickListener {
            val price = priceEditText.text.toString()
            if (price.isNotEmpty()) {
                setSubscriptionPrice(price)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Cena nevar būt tūkša", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun setSubscriptionPrice(price: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(currentUsername)
        userRef.child("subscriptionPrice").setValue(price)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Abonēšanas cena noteikta", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Kļuda noteicot abonēšanas cenu", Toast.LENGTH_SHORT).show()
                }
            }
    }
}