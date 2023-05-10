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

        database.execSQL("DROP TABLE IF EXISTS socios_old;")
        database.execSQL("ALTER TABLE socios RENAME TO socios_old;")
        database.execSQL("CREATE TABLE IF NOT EXISTS `socios` (`idSocio` INTEGER NOT NULL, `Nombre` TEXT NOT NULL, `Apellidos` TEXT NOT NULL, `Direccion` TEXT, `idCodPostal` INTEGER, `Dni` TEXT, `FecNacimiento` TEXT, `TlfParticular` TEXT, `TlfTrabajo` TEXT, `TlfMovil` TEXT, `eMail` TEXT, `idEstadoCivil` INTEGER, `idEstadoFicha` INTEGER, `bDerechoActos` INTEGER, `FecAlta` TEXT, `FecUltAlta` TEXT, `AnyAltaJuvenil` INTEGER, `AnyAltaFester` INTEGER, `FecBaja` TEXT, `AnyMuntanyesMerit` INTEGER, `bRodaBlancos` INTEGER, `bRodaNegros` INTEGER, `nrAntiguedad` INTEGER, `bArrancoDianaJuvenil` INTEGER, `AnyAltaRodaBlancos` INTEGER, `nrRodaBlancos` INTEGER, `AnyAltaRodaNegros` INTEGER, `nrRodaNegros` INTEGER, `AnyUltEscuadra` INTEGER, `idPuestoEscuadra` INTEGER, `AnyUltEscuadraEspecial` INTEGER, `idPuestoEscuadraEspecial` INTEGER, `AnyGloriero` INTEGER, `idFormaPago` INTEGER, `Sexo` INTEGER, `idTipoFestero` INTEGER, `idCargo` INTEGER, `Obs` TEXT, `Fotografia` TEXT, `AsociadoCon` INTEGER, `bCarnetAvancarga` INTEGER, `FecCaducidadAvancarga` TEXT, `bDisparaAvancarga` INTEGER, `idParentesco` INTEGER, `bEnviadoAsociacion` INTEGER, `bTipoFesteroBloqueado` INTEGER, `ImpLoteriaAbonado` REAL, `bAbonadoLoteria` INTEGER, `idBanco` INTEGER, `Iban` TEXT, `Bic` TEXT, `bUsaMontepio` INTEGER, `TiempoBaja` INTEGER, `IdentificadorBanco` INTEGER, `IdentificadorClienteBanco` INTEGER, `UltNrRemesa` INTEGER, `esAutorizoProteccionDatos` INTEGER NOT NULL, `filaPrincipal` INTEGER, `FecExpedicionAvancarga` TEXT, `RodaBlancaBloqueada` INTEGER, `CtaContable` INTEGER, PRIMARY KEY(`idSocio`))")
        database.execSQL("INSERT INTO socios SELECT * FROM socios_old")
        database.execSQL("DROP TABLE IF EXISTS socios_old;")
    }
}
