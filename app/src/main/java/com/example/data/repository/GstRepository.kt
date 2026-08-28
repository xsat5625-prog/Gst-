package com.example.data.repository

import com.example.data.local.dao.CustomRateDao
import com.example.data.local.dao.HistoryDao
import com.example.data.local.entity.CustomRateEntity
import com.example.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

class GstRepository(
    private val historyDao: HistoryDao,
    private val customRateDao: CustomRateDao
) {
    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()
    val allCustomRates: Flow<List<CustomRateEntity>> = customRateDao.getAllCustomRates()

    suspend fun saveHistory(history: HistoryEntity): Long {
        return historyDao.insertHistory(history)
    }

    suspend fun deleteHistory(id: Long) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }

    suspend fun addCustomRate(rate: Double, label: String, description: String = ""): Long {
        return customRateDao.insertCustomRate(
            CustomRateEntity(
                rate = rate,
                label = label,
                description = description
            )
        )
    }

    suspend fun deleteCustomRate(id: Long) {
        customRateDao.deleteCustomRateById(id)
    }
}
