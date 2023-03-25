package com.arnyminerz.filmagentaproto.database.data.admin

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_codes")
data class ScannedCode(
    @PrimaryKey val hashCode: Long,
)
