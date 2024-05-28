package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.google.firebase.database.*

class MySubscriptionsActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var subscriptionListView: ListView
    private lateinit var backButton: Button
    private lateinit var subscriptions: MutableList<Pair<String, String>>
    private lateinit var adapter: SubscriptionAdapter
    private lateinit var currentUsername: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_subscriptions)

        val sharedPref = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        currentUsername = sharedPref.getString("username", null) ?: ""

        database = FirebaseDatabase.getInstance().reference.child("subscriptions").child(currentUsername)
        subscriptionListView = findViewById(R.id.subscriptionListView)
        backButton = findViewById(R.id.backButton)
        subscriptions = mutableListOf()
        adapter = SubscriptionAdapter(this, subscriptions)

        subscriptionListView.adapter = adapter

        backButton.setOnClickListener {
            finish()
        }

        loadSubscriptions()
    }

    fun loadSubscriptions() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                subscriptions.clear()
                for (subscriptionSnapshot in snapshot.children) {
                    val username = subscriptionSnapshot.key ?: ""
                    val price = subscriptionSnapshot.child("price").getValue(String::class.java) ?: "0"
                    subscriptions.add(Pair(username, price))
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MySubscriptionsActivity, "Kļūda", Toast.LENGTH_SHORT).show()
            }
        })
    }
}