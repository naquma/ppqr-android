package com.mikore.ppqr.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mikore.ppqr.database.Account
import com.mikore.ppqr.database.AccountDao

class AccountViewModel(private val accountDao: AccountDao) : ViewModel() {

    private val _accounts: MutableLiveData<List<Account>> = MutableLiveData()

    fun setAccounts(accounts: List<Account>) {
        _accounts.value = accounts
    }

    fun getAccounts(): LiveData<List<Account>> = _accounts
}

class AccountViewModelFactory(private val accountDao: AccountDao) :  ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountViewModel(accountDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
