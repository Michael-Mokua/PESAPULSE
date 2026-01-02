package com.pesapulse.data.local

import androidx.room.*
import com.pesapulse.data.model.TransactionEntity
import com.pesapulse.data.model.GoalEntity
import com.pesapulse.data.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT SUM(amount) FROM transactions WHERE type IN ('received', 'deposit') AND timestamp >= :startTime")
    fun getTotalIncome(startTime: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type IN ('sent', 'payment', 'withdrawal') AND timestamp >= :startTime")
    fun getTotalExpenses(startTime: Long): Flow<Double?>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT 1")
    fun getLatestTransaction(): Flow<TransactionEntity?>
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)
}

@Database(entities = [TransactionEntity::class, GoalEntity::class, CategoryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pesapulse-db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
