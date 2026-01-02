package com.pesapulse.ui.viewmodel

import androidx.lifecycle.*
import androidx.lifecycle.asLiveData
import com.pesapulse.data.model.TransactionEntity
import com.pesapulse.data.repository.AppRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class TransactionViewModel(private val repository: AppRepository) : ViewModel() {
    val allTransactions: LiveData<List<TransactionEntity>> = repository.allTransactions.asLiveData()
    val latestTransaction: LiveData<TransactionEntity?> = repository.latestTransaction.asLiveData()

    private val startTime = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
    }.timeInMillis

    val monthlyIncome: LiveData<Double?> = repository.getTotalIncome(startTime).asLiveData()
    val monthlyExpenses: LiveData<Double?> = repository.getTotalExpenses(startTime).asLiveData()

    fun insertTransaction(transaction: TransactionEntity) = viewModelScope.launch {
        repository.insertTransaction(transaction)
    }
}

class ViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
