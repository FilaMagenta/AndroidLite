package com.arnyminerz.filmagentaproto.database.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration8To9: Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // <database>_old is used for storing temporally the old database

        database.execSQL("DROP TABLE IF EXISTS events_old;")
        database.execSQL("ALTER TABLE events RENAME TO events_old;")
        database.execSQL("CREATE TABLE IF NOT EXISTS `events` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `slug` TEXT NOT NULL, `permalink` TEXT NOT NULL, `dateCreated` TEXT NOT NULL, `dateModified` TEXT NOT NULL, `description` TEXT NOT NULL, `shortDescription` TEXT NOT NULL, `price` REAL NOT NULL, `attributes` TEXT NOT NULL, `stockStatus` TEXT NOT NULL DEFAULT 'instock', `stockQuantity` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))")
        database.execSQL("INSERT INTO events SELECT * FROM events_old")
        database.execSQL("DROP TABLE IF EXISTS events_old;")

        database.execSQL("DROP TABLE IF EXISTS orders_old;")
        database.execSQL("ALTER TABLE orders RENAME TO orders_old;")
        database.execSQL("CREATE TABLE IF NOT EXISTS `orders` (`id` INTEGER NOT NULL, `status` TEXT NOT NULL, `currency` TEXT NOT NULL, `dateCreated` TEXT NOT NULL, `dateModified` TEXT NOT NULL, `total` REAL NOT NULL, `customerId` INTEGER NOT NULL, `payment` TEXT DEFAULT null, `items` TEXT NOT NULL, PRIMARY KEY(`id`))")
        database.execSQL("INSERT INTO orders SELECT * FROM orders_old")
        database.execSQL("DROP TABLE IF EXISTS orders_old;")

        database.execSQL("DROP TABLE IF EXISTS customers_old;")
        database.execSQL("ALTER TABLE customers RENAME TO customers_old;")
        database.execSQL("CREATE TABLE IF NOT EXISTS `customers` (`id` INTEGER NOT NULL, `dateCreated` TEXT NOT NULL, `dateModified` TEXT NOT NULL, `email` TEXT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `role` TEXT NOT NULL, `username` TEXT NOT NULL, `billing` TEXT NOT NULL, `shipping` TEXT NOT NULL, `isPayingCustomer` INTEGER NOT NULL, `avatarUrl` TEXT NOT NULL, PRIMARY KEY(`id`))")
        database.execSQL("INSERT INTO customers SELECT * FROM customers_old")
        database.execSQL("DROP TABLE IF EXISTS customers_old;")
    }
}
