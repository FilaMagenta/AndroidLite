package com.arnyminerz.filmagentaproto.database.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.arnyminerz.filmagentaproto.database.data.admin.CodeScanned

@Dao
interface AdminDao {
    @Query("SELECT * FROM codes_scanned")
    suspend fun getAllScannedCodes(): List<CodeScanned>

    @Query("SELECT * FROM codes_scanned")
    fun getAllScannedCodesLive(): LiveData<List<CodeScanned>>

    @Query("SELECT * FROM codes_scanned WHERE hash=:hash")
    suspend fun getFromHash(hash: String): CodeScanned?

    @Query("SELECT * FROM codes_scanned WHERE hash=:hash")
    fun getFromHashLive(hash: String): LiveData<CodeScanned?>

    @Insert
    suspend fun insert(vararg data: CodeScanned)

    @Delete
    suspend fun delete(vararg data: CodeScanned)

    @Update
    suspend fun update(vararg data: CodeScanned)
}