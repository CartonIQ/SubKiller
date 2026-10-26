package com.subkiller.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity representing a subscription.
 * Required fields: id, name, price, nextPaymentDate, category, isPaused, isFreeTrial.
 */
@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double,
    val nextPaymentDate: String, // Stored as ISO YYYY-MM-DD
    val category: String,
    val isPaused: Boolean = false,
    val isFreeTrial: Boolean = false
)
