package com.arnyminerz.filmagentaproto.database.data.admin

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "codes_scanned")
data class CodeScanned(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val hash: String,
)
