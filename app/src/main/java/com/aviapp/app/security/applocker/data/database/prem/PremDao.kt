package com.aviapp.app.security.applocker.data.database.prem

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PremDao {

    @Query("SELECT * FROM prem")
    fun getPremFlow(): Flow<Prem?>

    @Query("SELECT * FROM prem")
    fun getPrem(): Prem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePrem(prem: Prem)
}