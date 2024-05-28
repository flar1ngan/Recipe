package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddRecipeActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private var imageUri: Uri? = null

    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        database = FirebaseDatabase.getInstance().reference.child("recipes")
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference.child("recipe_images")

        val recipeNameEditText = findViewById<EditText>(R.id.recipeNameEditText)
        val recipeDetailsEditText = findViewById<EditText>(R.id.recipeDetailsEditText)
        val selectImageButton = findViewById<Button>(R.id.selectImageButton)
        val selectedImageView = findViewById<ImageView>(R.id.selectedImageView)
        val submitRecipeButton = findViewById<Button>(R.id.submitRecipeButton)
        val backButton = findViewById<Button>(R.id.backButton)
        val premiumSwitch = findViewById<Switch>(R.id.premiumSwitch) // Добавление Switch

        val sharedPref = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", null)

        if (username == null) {
            Toast.makeText(this, "Jūs neesat autorizējusi", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data?.data
                selectedImageView.setImageURI(imageUri)
            }
        }

        selectImageButton.setOnClickListener {
            selectImage()
        }

        submitRecipeButton.setOnClickListener {
            val name = recipeNameEditText.text.toString()
            val details = recipeDetailsEditText.text.toString()
            val isPremium = premiumSwitch.isChecked // Получение значения Switch

            if (name.isNotEmpty() && details.isNotEmpty() && imageUri != null) {
                uploadImageAndSaveRecipe(name, details, username, isPremium)
            } else {
                Toast.makeText(this, "Aizpildiet visus laukus un izvēlieties attēlu", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        selectImageLauncher.launch(Intent.createChooser(intent, "Izvēlieties attēlu"))
    }

    private fun uploadImageAndSaveRecipe(name: String, details: String, username: String, isPremium: Boolean) {
        val imageId = storageReference.child("${System.currentTimeMillis()}.jpg")
        imageUri?.let { uri ->
            imageId.putFile(uri).addOnSuccessListener {
                imageId.downloadUrl.addOnSuccessListener { uri ->
                    saveRecipeToDatabase(name, details, username, uri.toString(), isPremium)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Kļūda ar attēlu lejupielādi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveRecipeToDatabase(name: String, details: String, username: String, imageUrl: String, isPremium: Boolean) {
        val recipeId = database.push().key
        if (recipeId != null) {
            val recipe = Recipe(recipeId, name, details, username, imageUrl, isPremium)
            database.child(recipeId).setValue(recipe).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    finish()
                } else {
                    Toast.makeText(this, "Kļūda publicējot attēlu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}