package com.arnyminerz.filmagentaproto.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.arnyminerz.filmagentaproto.database.local.AppDatabase
import com.arnyminerz.filmagentaproto.database.local.migration.Migration8To9
import com.arnyminerz.filmagentaproto.utils.getString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

private const val TEST_DB = "migration-test"

class MigrationTests {
    companion object {
        private val legacyDateFormat: SimpleDateFormat
            get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

        private val EVENTS_KEYS = listOf(
            "id", "name", "slug", "permalink", "dateCreated", "dateModified", "description",
            "shortDescription", "price", "attributes"
        )
    }

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate8To9() {
        // This test only checks that dateCreated has been migrated correctly for events, since the
        // same process is done on all tables, so if it works in one, it should be executed
        // correctly on all of them

        var oldDate: LocalDateTime

        var db = helper.createDatabase(TEST_DB, 8)
        db.apply {
            // Database has schema version 8. Insert some data using SQL queries.
            // You can't use DAO classes because they expect the latest schema.
            execSQL("INSERT INTO events (${EVENTS_KEYS.joinToString()}) VALUES ('1', 'Testing event', 'testing-event', 'https://example.com', '2023-05-10T17:00:00', '2023-05-10T17:00:00', 'Description', 'Short description', 10, '')")

            // Load all the data stored before migration
            query("SELECT * FROM events").use { cur ->
                // There should be one event
                assertEquals(1, cur.count)

                // Take first result
                cur.moveToNext()

                val dateCreatedStr: String? = cur.getString("dateCreated")
                assertNotNull(dateCreatedStr)
                val dateCreated: Date? = legacyDateFormat.parse(dateCreatedStr!!)
                assertNotNull(dateCreated)
                oldDate = dateCreated!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
            }

            // Prepare for the next version.
            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 9, true, Migration8To9)

        // Check that after migration data has been updated correctly
        db.query("SELECT * FROM events").use { cur ->
            cur.moveToNext()

            val dateCreatedStr: String? = cur.getString("dateCreated")
            assertNotNull(dateCreatedStr)
            val dateCreated: LocalDateTime? = LocalDateTime.parse(dateCreatedStr)
            assertNotNull(dateCreated)
            assertEquals(oldDate, dateCreated)
        }
    }
}
