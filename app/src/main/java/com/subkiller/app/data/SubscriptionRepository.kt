package com.subkiller.app.data

import kotlinx.coroutines.flow.Flow

class SubscriptionRepository(private val dao: SubscriptionDao) {

    val allSubscriptions: Flow<List<SubscriptionEntity>> = dao.getAllSubscriptionsFlow()

    suspend fun getActiveSubscriptionsSync(): List<SubscriptionEntity> {
        return dao.getActiveSubscriptionsSync()
    }

    suspend fun insert(subscription: SubscriptionEntity): Long {
        return dao.insertSubscription(subscription)
    }

    suspend fun update(subscription: SubscriptionEntity) {
        dao.updateSubscription(subscription)
    }

    suspend fun delete(subscription: SubscriptionEntity) {
        dao.deleteSubscription(subscription)
    }

    suspend fun setPaused(id: Long, isPaused: Boolean) {
        dao.setPausedStatus(id, isPaused)
    }
}
