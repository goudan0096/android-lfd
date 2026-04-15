package com.warehouse.manager.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.warehouse.manager.data.dao.ProductDao
import com.warehouse.manager.data.dao.StockRecordDao
import com.warehouse.manager.data.model.Product
import com.warehouse.manager.data.model.StockRecord

/**
 * Room 数据库
 */
@Database(entities = [Product::class, StockRecord::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun stockRecordDao(): StockRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "warehouse_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
