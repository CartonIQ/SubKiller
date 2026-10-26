package com.subkiller.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {

    @Query("SELECT * FROM subscriptions ORDER BY nextPaymentDate ASC")
    fun getAllSubscriptionsFlow(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE isPaused = 0 ORDER BY nextPaymentDate ASC")
    suspend fun getActiveSubscriptionsSync(): List<SubscriptionEntity>

    @Query("SELECT * FROM subscriptions ORDER BY nextPaymentDate ASC")
    suspend fun getAllSubscriptionsSync(): List<SubscriptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity): Long

    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity)

    @Delete
    suspend fun deleteSubscription(subscription: SubscriptionEntity)

    @Query("UPDATE subscriptions SET isPaused = :isPaused WHERE id = :id")
    suspend fun setPausedStatus(id: Long, isPaused: Boolean)
}
