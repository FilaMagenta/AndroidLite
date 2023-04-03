package com.arnyminerz.filmagentaproto.database.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.arnyminerz.filamagenta.core.data.Transaction

@Dao
interface TransactionsDao {
    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<Transaction>

    @Query("SELECT * FROM transactions")
    fun getAllLive(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE idSocio=:idSocio")
    suspend fun getByIdSocio(idSocio: Long): List<Transaction>

    @Query("SELECT * FROM transactions WHERE idSocio=:idSocio")
    fun getByIdSocioLive(idSocio: Long?): LiveData<List<Transaction>>

    @Insert
    suspend fun insert(vararg data: Transaction)

    @Delete
    suspend fun delete(vararg data: Transaction)

    @Update
    suspend fun update(vararg data: Transaction)

}