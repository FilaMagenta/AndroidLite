package com.arnyminerz.filmagentaproto.database.data

import android.accounts.Account
import androidx.room.Entity
import androidx.room.Ignore
import org.jsoup.Jsoup

@Entity(tableName = "user_data", primaryKeys = ["accountName", "accountType"])
data class PersonalData(
    val accountName: String,
    val accountType: String,
    val name: String,
    val inwards: Double,
    val outwards: Double,
    val transactions: List<Transaction>,
) {
    companion object {
        fun fromHtml(html: String, account: Account): PersonalData {
            val data = Jsoup.parse(html)
            val name = data.getElementsByClass("nombresocio").first()!!.text()
            val tables = data.getElementsByClass("table-responsive")

            val transactionsTable = tables[0]!!
            val transactionsRows = transactionsTable.getElementsByTag("tr")
            val transactions = transactionsRows
                .map { tr -> tr.getElementsByTag("td") }
                .filter { it.isNotEmpty() }
                .map { tds ->
                    Transaction(
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

            return PersonalData(account.name, account.type, name, inwards, outwards, transactions)
        }
    }

    @Ignore
    val balance: Double = inwards + outwards
}
