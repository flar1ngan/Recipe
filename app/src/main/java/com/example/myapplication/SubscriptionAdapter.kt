package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.R
import com.google.firebase.database.FirebaseDatabase

class SubscriptionAdapter(private val context: Context, private val subscriptions: List<Pair<String, String>>) : BaseAdapter() {

    override fun getCount(): Int {
        return subscriptions.size
    }

    override fun getItem(position: Int): Any {
        return subscriptions[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_subscription, parent, false)
        } else {
            view = convertView
        }

        val subscription = subscriptions[position]

        val usernameTextView = view.findViewById<TextView>(R.id.usernameTextView)
        val priceTextView = view.findViewById<TextView>(R.id.priceTextView)
        val unsubscribeButton = view.findViewById<Button>(R.id.unsubscribeButton)

        usernameTextView.text = subscription.first
        priceTextView.text = "Abonēšanas cena: ${subscription.second}"

        unsubscribeButton.setOnClickListener {
            unsubscribe(subscription.first)
        }

        usernameTextView.setOnClickListener {
            val intent = Intent(context, UserProfileActivity::class.java)
            intent.putExtra("username", subscription.first)
            context.startActivity(intent)
        }

        return view
    }

    private fun unsubscribe(username: String) {
        val sharedPref = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val currentUsername = sharedPref.getString("username", null) ?: ""
        val subscriptionRef = FirebaseDatabase.getInstance().reference.child("subscriptions").child(currentUsername).child(username)

        subscriptionRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Jūs esat anulējusi abonementu $username", Toast.LENGTH_SHORT).show()
                (context as MySubscriptionsActivity).loadSubscriptions()
            } else {
                Toast.makeText(context, "Kļūda anulējot abonementu", Toast.LENGTH_SHORT).show()
            }
        }
    }
}