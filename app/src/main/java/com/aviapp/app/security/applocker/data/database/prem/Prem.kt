package com.aviapp.app.security.applocker.data.database.prem

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prem")
data class Prem (

    @PrimaryKey @ColumnInfo(name = "id") var id: Int = 0,
    @ColumnInfo(name = "prIsNeeded") var prIsNeeded: Boolean = true
)