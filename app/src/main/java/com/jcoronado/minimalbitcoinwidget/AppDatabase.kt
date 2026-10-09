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

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Entity(tableName = "debug_logs")
data class DebugLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val message: String,
    val timestamp: String = SimpleDateFormat("yyyy-MM-dd ・ hh:mm:ss a", Locale.US).format(Date()),
    val type: String = TYPE_WIDGET
) {
    companion object {
        const val TYPE_WIDGET = "WIDGET"
        const val TYPE_APP = "APP"
    }
}

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
@Database(entities = [DebugLog::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun debugDao(): DebugDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE debug_logs ADD COLUMN type TEXT NOT NULL DEFAULT 'WIDGET'")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "debug_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}