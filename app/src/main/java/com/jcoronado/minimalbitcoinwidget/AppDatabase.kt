package com.jcoronado.minimalbitcoinwidget

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "debug_logs")
data class DebugLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val message: String,
    val timestamp: String = SimpleDateFormat("yyyy-MM-dd ・ hh:mm:ss a", Locale.US).format(Date())
)

@Dao
abstract class DebugDao {
    @Insert
    abstract suspend fun insertLog(log: DebugLog)

    @Query("DELETE FROM debug_logs WHERE id NOT IN (SELECT id FROM debug_logs ORDER BY id DESC LIMIT 100)")
    abstract suspend fun deleteOldLogs()

    @Transaction
    open suspend fun insert(log: DebugLog) {
        insertLog(log)
        deleteOldLogs()
    }

    // Returns a Flow so the UI updates automatically
    @Query("SELECT * FROM debug_logs ORDER BY id DESC")
    abstract fun getAllLogs(): Flow<List<DebugLog>>

    @Query("DELETE FROM debug_logs")
    abstract suspend fun clearAll()
}

// --- 3. The Database Singleton ---
@Database(entities = [DebugLog::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun debugDao(): DebugDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "debug_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}