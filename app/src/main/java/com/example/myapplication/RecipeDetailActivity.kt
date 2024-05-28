package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.*

class RecipeDetailActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var recipeImageView: ImageView
    private lateinit var recipeNameTextView: TextView
    private lateinit var recipeDetailsTextView: TextView
    private lateinit var recipeUserTextView: TextView
    private lateinit var premiumTextView: TextView
    private lateinit var backButton: Button
    private lateinit var currentUsername: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        database = FirebaseDatabase.getInstance().reference

        recipeImageView = findViewById(R.id.recipeImageView)
        recipeNameTextView = findViewById(R.id.recipeNameTextView)
        recipeDetailsTextView = findViewById(R.id.recipeDetailsTextView)
        recipeUserTextView = findViewById(R.id.recipeUserTextView)
        premiumTextView = findViewById(R.id.premiumTextView)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

        val recipeId = intent.getStringExtra("recipeId")

        val sharedPref = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        currentUsername = sharedPref.getString("username", null) ?: ""

        if (recipeId != null) {
            database.child("recipes").child(recipeId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val recipe = snapshot.getValue(Recipe::class.java)
                    if (recipe != null) {
                        if (recipe.premium && recipe.username != currentUsername) {
                            checkSubscriptionAndShowDialog(recipe)
                        } else {
                            loadRecipe(recipe)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RecipeDetailActivity, "Kļuda saņemot datus", Toast.LENGTH_SHORT).show()
                }
            })
        }

        recipeUserTextView.setOnClickListener {
            val username = recipeUserTextView.text.toString()
            val intent = Intent(this, UserProfileActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }
    }

    private fun checkSubscriptionAndShowDialog(recipe: Recipe) {
        val subscriptionRef = database.child("subscriptions").child(currentUsername).child(recipe.username)
        subscriptionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    loadRecipe(recipe)
                } else {
                    showSubscriptionDialog(recipe)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RecipeDetailActivity, "Kļuda ar premium pārbaudu", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showSubscriptionDialog(recipe: Recipe) {
        val userRef = database.child("users").child(recipe.username)
        userRef.child("subscriptionPrice").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val subscriptionPrice = dataSnapshot.getValue(String::class.java) ?: "0"
                val dialogView = LayoutInflater.from(this@RecipeDetailActivity).inflate(R.layout.dialog_subscription, null)
                val subscriptionTextView = dialogView.findViewById<TextView>(R.id.subscriptionTextView)
                subscriptionTextView.text = "Abonēt ${recipe.username} maksa $subscriptionPrice€. Abonēt?"

                val alertDialog = AlertDialog.Builder(this@RecipeDetailActivity).setView(dialogView).create()

                dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
                    alertDialog.dismiss()
                    finish()
                }

                dialogView.findViewById<Button>(R.id.confirmButton).setOnClickListener {
                    subscribeToUser(recipe.username, subscriptionPrice)
                    alertDialog.dismiss()
                }

                alertDialog.show()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@RecipeDetailActivity, "Kļūda saņemot abonēšanas cenu", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun subscribeToUser(username: String, price: String) {
        val subscriptionRef = database.child("subscriptions").child(currentUsername).child(username)
        val subscriptionData = mapOf("price" to price)
        subscriptionRef.setValue(subscriptionData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Jūs esat veiksmīgi abonējusi $username", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Abonēšanas kļūda", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadRecipe(recipe: Recipe) {
        recipeNameTextView.text = recipe.name
        recipeDetailsTextView.text = recipe.details
        recipeUserTextView.text = recipe.username
        Glide.with(this).load(recipe.imageUrl).into(recipeImageView)

        if (recipe.premium) {
            premiumTextView.visibility = View.VISIBLE
        } else {
            premiumTextView.visibility = View.GONE
        }
    }
}