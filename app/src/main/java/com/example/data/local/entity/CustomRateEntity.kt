package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_rates")
data class CustomRateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rate: Double,
    val label: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
