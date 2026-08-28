package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CustomRateDao
import com.example.data.local.dao.HistoryDao
import com.example.data.local.entity.CustomRateEntity
import com.example.data.local.entity.HistoryEntity

@Database(
    entities = [HistoryEntity::class, CustomRateEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun customRateDao(): CustomRateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gst_calculator.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
