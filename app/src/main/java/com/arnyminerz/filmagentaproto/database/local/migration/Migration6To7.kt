package com.arnyminerz.filmagentaproto.database.local.migration

import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec

@DeleteTable.Entries(
    DeleteTable(
        tableName = "scanned_codes",
    )
)
class Migration6To7: AutoMigrationSpec
