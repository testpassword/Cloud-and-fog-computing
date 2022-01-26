package testpassword.services

import testpassword.models.IndexQueryStatement
import java.net.ConnectException
import java.sql.*
import kotlin.runCatching
import kotlin.system.measureTimeMillis

typealias JDBC_Creds = Triple<String, String, String>

class DatabaseNotSupportedException: SQLException()

class IndexCreationError(badQuery: String): SQLException(badQuery)

enum class SUPPORTED_DBs(val indexTypes: Set<String>) {

    /*sqlserver(setOf("CLUSTERED", "NONCLUSTERED")) {
        override fun buildDBSpecificIndexQueries(indexQuery: IndexQueryStatement): Set<IndexQueryStatement> {
            val (beginning, end) = indexQuery.createIndexStatement.split(" ", limit = 2, ignoreCase = true)
            if (arrayOf(beginning to "CREATE", end to "INDEX").all { it.first.startsWith(it.second, ignoreCase = true) })
            return indexTypes.map {
                indexQuery.copy().apply {
                    indexName = "${indexName}_${it}"
                    createIndexStatement = "$beginning $it $end"
                    dropIndexStatement = "DROP INDEX ${indexName}-${it} ON "
                }
            }.toSet()
            else throw IndexCreationError(indexQuery.createIndexStatement)
        }
    },*/

    postgresql(setOf("HASH", "BTREE")) {
        override fun buildDBSpecificIndexQueries(indexQuery: IndexQueryStatement): List<IndexQueryStatement> {
            val (beginning, end) = indexQuery.createIndexStatement.split(" (", limit = 2, ignoreCase = true)
            if (beginning.endsWith(indexQuery.table))
                return indexTypes.mapNotNull {
                    if (it == "HASH" && indexQuery.columns.count() > 1) null
                    else indexQuery.copy().apply {
                        val newName = "${indexName}${it.toUpperCase()}"
                        createIndexStatement = "$beginning USING $it($end".replace(indexName, newName)
                        indexName = newName
                        dropIndexStatement = "DROP INDEX IF EXISTS $indexName RESTRICT;"
                    }
                }
            else throw IndexCreationError(indexQuery.createIndexStatement)
        }
    };

    abstract fun buildDBSpecificIndexQueries(indexQuery: IndexQueryStatement): List<IndexQueryStatement>
}

class DBsSupport(val creds: JDBC_Creds) {

    companion object {
        private const val PING_TIMEOUT_SEC = 20
        val CONNECTION_URL_PATTERN = Regex("jdbc:.*://.*;.*;.*")
    }

    fun checkDbAvailability() {
        if (this.isDbSupported().not()) throw DatabaseNotSupportedException()
        if (this.ping().not()) throw ConnectException()
    }

    private fun getConnection(): Connection =
        DriverManager.getConnection(creds.first, creds.second, creds.third)

    fun isDbSupported(): Boolean =
        runCatching { SUPPORTED_DBs.valueOf(creds.first.split(":")[1]); true }.getOrDefault(false)

    fun ping(): Boolean = getConnection().use { it.isValid(PING_TIMEOUT_SEC) }

    fun execute(queryFunc: () -> String): Boolean =
        getConnection()
            .createStatement()
            .use {
                it.execute(queryFunc())
            }

    fun measureQuery(queryFunc: () -> String): Long =
        getConnection()
            .createStatement()
            .use {
                measureTimeMillis {
                    it.execute(queryFunc())
                }
            }

    fun getTableColumns(tableName: String): Set<String> =
        getConnection()
            .createStatement()
            .executeQuery("SELECT * FROM $tableName;" )
            .metaData.let { md ->
                generateSequence(1) { i -> i + 1 }
                    .take(md.columnCount)
                    .map { i -> md.getColumnName(i) }
                    .toSet()
        }
}