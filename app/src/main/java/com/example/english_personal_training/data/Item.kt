package com.example.english_personal_training.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tag: String,
    val word: String,
    val meaning: String
)
