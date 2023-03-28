package com.arnyminerz.filmagentaproto.database.data

import android.accounts.Account
import androidx.room.Entity
import androidx.room.Ignore
import com.arnyminerz.filmagentaproto.exceptions.ParseException
import org.jsoup.Jsoup

@Deprecated("Use Transaction with \"transactions\" table.")
@Entity(tableName = "user_data", primaryKeys = ["accountName", "accountType"])
data class PersonalData(
    val accountName: String,
    val accountType: String,
    val name: String,
    val inwards: Double,
    val outwards: Double,
    val transactions: List<OldTransaction>,
) {
    companion object {
        /**
         * Creates a new [PersonalData] from the contents of the given html page.
         * @param html The html sources to decode.
         * @param account The account that is loading the data.
         * @throws NullPointerException If there's a missing element in the source code.
         */
        fun fromHtml(html: String, account: Account): PersonalData = try {
            val data = Jsoup.parse(html)
            val name = data.getElementsByClass("nombresocio").first()!!.text()
            val tables = data.getElementsByClass("table-responsive")

            val transactionsTable = tables[0]!!
            val transactionsRows = transactionsTable.getElementsByTag("tr")
            val transactions = transactionsRows
                .map { tr -> tr.getElementsByTag("td") }
                .filter { it.isNotEmpty() }
                .map { tds ->
                    OldTransaction(
                        tds[0].text(),
                        tds[1].text(),
                        tds[2].text().toLong(),
                        tds[3].text().replace(',', '.').toDoubleOrNull(),
                        tds[4].text().replace(',', '.').toDoubleOrNull(),
                        false,
                    )
                }

            val inOutTable = tables[1]!!
            val inOutRows = inOutTable
                .getElementsByTag("tr")[1]
                .getElementsByTag("td")
            val inwards = inOutRows[3].text()
                .replace(".", "")
                .replace(',', '.')
                .toDoubleOrNull()
                ?: 0.0
            val outwards = inOutRows[4].text()
                .replace(".", "")
                .replace(',', '.')
                .toDoubleOrNull()
                ?.times(-1)
                ?: 0.0

            PersonalData(account.name, account.type, name, inwards, outwards, transactions)
        } catch (e: Exception) {
            throw ParseException(html, e)
        }
    }

    @Ignore
    val balance: Double = inwards + outwards
}
