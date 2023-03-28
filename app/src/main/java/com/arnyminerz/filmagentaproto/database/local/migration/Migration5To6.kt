package com.arnyminerz.filmagentaproto.database.local.migration

import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec

@DeleteTable.Entries(
    DeleteTable(
        tableName = "user_data",
    )
)
class Migration5To6: AutoMigrationSpec
