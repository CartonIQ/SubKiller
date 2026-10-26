package com.subkiller.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.subkiller.app.data.AppDatabase
import com.subkiller.app.data.SubscriptionEntity
import com.subkiller.app.data.SubscriptionRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SubscriptionRepository
    val allSubscriptions: LiveData<List<SubscriptionEntity>>

    // LiveData for Monthly (false) vs Yearly (true)
    private val _isYearly = MutableLiveData(false)
    val isYearly: LiveData<Boolean> = _isYearly

    // Flow for active total cost
    val totalCost: LiveData<Double>

    init {
        val dao = AppDatabase.getDatabase(application).subscriptionDao()
        repository = SubscriptionRepository(dao)
        allSubscriptions = repository.allSubscriptions.asLiveData()

        totalCost = repository.allSubscriptions.map { list ->
            val monthlyTotal = list.filter { !it.isPaused }.sumOf { it.price }
            if (_isYearly.value == true) monthlyTotal * 12 else monthlyTotal
        }.asLiveData()
    }

    fun setYearlyToggle(yearly: Boolean) {
        _isYearly.value = yearly
    }

    fun addSubscription(name: String, price: Double, date: String, category: String, isFreeTrial: Boolean) {
        viewModelScope.launch {
            val entity = SubscriptionEntity(
                name = name,
                price = price,
                nextPaymentDate = date,
                category = category,
                isPaused = false,
                isFreeTrial = isFreeTrial
            )
            repository.insert(entity)
        }
    }

    fun togglePause(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.setPaused(subscription.id, !subscription.isPaused)
        }
    }

    fun deleteSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.delete(subscription)
        }
    }
}
