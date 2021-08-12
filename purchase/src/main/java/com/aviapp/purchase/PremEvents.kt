package com.aviapp.purchase

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

interface PremEvents {

    suspend fun prIsNeeded(): Boolean
    fun prIsNeededFlow(): Flow<Boolean>
    fun prIsNeededLiveData(): LiveData<Boolean>
    fun updatePrem(prem: Boolean)

}