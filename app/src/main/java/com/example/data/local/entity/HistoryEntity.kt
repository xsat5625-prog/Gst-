package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val calculationType: String, // "SINGLE_CALC" or "INVOICE"
    val baseAmount: Double,
    val gstRate: Double,
    val gstAmount: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val totalAmount: Double,
    val isInclusive: Boolean,
    val isInterState: Boolean,
    val quantity: Double = 1.0,
    val discountPercent: Double = 0.0,
    val notes: String = "",
    val itemsSummary: String = ""
)
