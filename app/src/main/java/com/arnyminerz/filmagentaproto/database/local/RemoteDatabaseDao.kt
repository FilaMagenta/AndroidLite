package com.arnyminerz.filmagentaproto.database.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.arnyminerz.filmagentaproto.database.remote.protos.Socio

@Dao
interface RemoteDatabaseDao {
    @Query("SELECT * FROM socios")
    suspend fun getAll(): List<Socio>

    @Query("SELECT * FROM socios WHERE AsociadoCon=:id")
    suspend fun getAllAssociatedWith(id: Long): List<Socio>

    @Query("SELECT * FROM socios")
    fun getAllLive(): LiveData<List<Socio>>

    @Insert
    suspend fun insert(vararg data: Socio)

    @Delete
    suspend fun delete(vararg data: Socio)

    @Update
    suspend fun update(vararg data: Socio)

}