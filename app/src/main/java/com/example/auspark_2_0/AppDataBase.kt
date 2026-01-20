package com.example.auspark_2_0

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StudentEntity::class, ClassEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun auSparkDao(): AuSparkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "auspark_database" // Name of the file inside the phone
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}