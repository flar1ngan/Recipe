package com.example.myapplication

data class Recipe(
    val id: String = "",
    val name: String = "",
    val details: String = "",
    val username: String = "",
    val imageUrl: String = "",
    val premium: Boolean = false
) {
    constructor() : this("", "", "", "", "", false)
}
