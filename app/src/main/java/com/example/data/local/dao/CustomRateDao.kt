package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CustomRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomRateDao {
    @Query("SELECT * FROM custom_rates ORDER BY rate ASC")
    fun getAllCustomRates(): Flow<List<CustomRateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomRate(rate: CustomRateEntity): Long

    @Query("DELETE FROM custom_rates WHERE id = :id")
    suspend fun deleteCustomRateById(id: Long)
}
