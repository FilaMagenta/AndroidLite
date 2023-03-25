package com.arnyminerz.filmagentaproto.database.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.arnyminerz.filmagentaproto.database.data.admin.ScannedCode

@Dao
interface AdminDao {
    @Query("SELECT * FROM scanned_codes")
    suspend fun getAllScannedCodes(): List<ScannedCode>

    @Query("SELECT * FROM scanned_codes")
    fun getAllScannedCodesLive(): LiveData<List<ScannedCode>>

    @Query("SELECT * FROM scanned_codes WHERE hashCode=:hashCode")
    fun getFromHashCode(hashCode: Long): ScannedCode?

    @Insert
    suspend fun insert(vararg data: ScannedCode)

    @Delete
    suspend fun delete(vararg data: ScannedCode)

    @Update
    suspend fun update(vararg data: ScannedCode)
}