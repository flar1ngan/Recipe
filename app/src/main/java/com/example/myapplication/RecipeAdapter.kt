package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.Recipe

class RecipeAdapter(private val context: Context, private val recipes: List<Recipe>) : BaseAdapter() {

    override fun getCount(): Int {
        return recipes.size
    }

    override fun getItem(position: Int): Any {
        return recipes[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false)
        } else {
            view = convertView
        }

        val recipe = recipes[position]

        val recipeNameTextView = view.findViewById<TextView>(R.id.recipeNameTextView)
        val recipeImageView = view.findViewById<ImageView>(R.id.recipeImageView)
        val recipeUserTextView = view.findViewById<TextView>(R.id.recipeUserTextView)
        val premiumTextView = view.findViewById<TextView>(R.id.premiumTextView)

        recipeNameTextView.text = recipe.name
        recipeUserTextView.text = "Autors: ${recipe.username}"
        Glide.with(context).load(recipe.imageUrl).into(recipeImageView)

        Log.d("RecipeAdapter", "Recipe: ${recipe.name}, premium: ${recipe.premium}")
        if (recipe.premium) {
            premiumTextView.visibility = View.VISIBLE
        } else {
            premiumTextView.visibility = View.GONE
        }

        view.setOnClickListener {
            val intent = Intent(context, RecipeDetailActivity::class.java)
            intent.putExtra("recipeId", recipe.id)
            context.startActivity(intent)
        }

        recipeUserTextView.setOnClickListener {
            val intent = Intent(context, UserProfileActivity::class.java)
            intent.putExtra("username", recipe.username)
            context.startActivity(intent)
        }

        return view
    }
}