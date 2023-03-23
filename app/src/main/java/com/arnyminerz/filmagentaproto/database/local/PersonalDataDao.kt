package com.arnyminerz.filmagentaproto.database.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.arnyminerz.filmagentaproto.database.data.PersonalData
import com.arnyminerz.filmagentaproto.database.data.Transaction

@Dao
interface PersonalDataDao {
    @Query("SELECT * FROM user_data")
    suspend fun getAll(): List<PersonalData>

    @Query("SELECT * FROM user_data")
    fun getAllLive(): LiveData<List<PersonalData>>

    @Query("SELECT * FROM user_data WHERE accountName=:accountName AND accountType=:accountType LIMIT 1")
    suspend fun getByAccount(accountName: String, accountType: String): PersonalData?

    @Insert
    suspend fun insert(vararg data: PersonalData)

    @Delete
    suspend fun delete(vararg data: PersonalData)

    @Update
    suspend fun update(vararg data: PersonalData)

    @Query("UPDATE user_data SET transactions=:transactions WHERE accountName=:accountName")
    suspend fun updateTransactions(accountName: String, transactions: List<Transaction>)

}