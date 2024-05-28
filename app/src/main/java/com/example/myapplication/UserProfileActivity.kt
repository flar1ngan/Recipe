package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class UserProfileActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var userRecipesListView: ListView
    private lateinit var usernameTextView: TextView
    private lateinit var backButton: Button
    private lateinit var subscribeButton: Button
    private lateinit var recipes: MutableList<Recipe>
    private lateinit var adapter: RecipeAdapter
    private lateinit var username: String
    private lateinit var currentUsername: String
    private lateinit var subscriptionRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        username = intent.getStringExtra("username") ?: ""

        val sharedPref = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        currentUsername = sharedPref.getString("username", null) ?: ""

        database = FirebaseDatabase.getInstance().reference.child("recipes")
        userRecipesListView = findViewById(R.id.userRecipesListView)
        usernameTextView = findViewById(R.id.usernameTextView)
        backButton = findViewById(R.id.backButton)
        subscribeButton = findViewById(R.id.subscribeButton)
        recipes = mutableListOf()
        adapter = RecipeAdapter(this, recipes)

        userRecipesListView.adapter = adapter

        usernameTextView.text = "Lietotāja $username receptes"

        backButton.setOnClickListener {
            finish()
        }

        if (username != currentUsername) {
            subscribeButton.visibility = View.VISIBLE
            loadSubscriptionPrice()
        } else {
            subscribeButton.visibility = View.GONE
        }

        subscribeButton.setOnClickListener {
            handleSubscription()
        }

        loadUserRecipes()
    }

    private fun loadUserRecipes() {
        database.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
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
                    // Handle error
                }
            })
    }

    private fun loadSubscriptionPrice() {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(username)
        userRef.child("subscriptionPrice").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val subscriptionPrice = dataSnapshot.getValue(String::class.java) ?: "0"
                checkSubscriptionStatus(subscriptionPrice)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@UserProfileActivity, "Nevar saņemt abonēšanas cenu", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkSubscriptionStatus(subscriptionPrice: String) {
        subscriptionRef = FirebaseDatabase.getInstance().reference
            .child("subscriptions").child(currentUsername).child(username)

        subscriptionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    subscribeButton.isEnabled = false
                    subscribeButton.text = "Esat abonējusi"
                } else {
                    subscribeButton.isEnabled = true
                    subscribeButton.text = "Abonēt ($subscriptionPrice€)"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun handleSubscription() {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(username)
        userRef.child("subscriptionPrice").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val subscriptionPrice = dataSnapshot.getValue(String::class.java) ?: "0"
                val subscriptionData = mapOf("price" to subscriptionPrice)
                subscriptionRef.setValue(subscriptionData).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@UserProfileActivity, "Jūs esat veiksmīgi abonējusi $username", Toast.LENGTH_SHORT).show()
                        subscribeButton.isEnabled = false
                        subscribeButton.text = "Esat abonējusi"
                    } else {
                        Toast.makeText(this@UserProfileActivity, "Abonēšanas kļūda", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@UserProfileActivity, "Nevar saņemt abonēšanas cenu", Toast.LENGTH_SHORT).show()
            }
        })
    }
}