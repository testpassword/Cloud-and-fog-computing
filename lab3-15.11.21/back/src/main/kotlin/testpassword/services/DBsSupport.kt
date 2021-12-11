package testpassword.services

import java.net.ConnectException
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

typealias JDBC_Creds = Triple<String, String, String>

class DatabaseNotSupportedException: SQLException()

object DBsSupport {

    private const val PING_TIMEOUT_SEC = 20
    val CONNECTION_URL_PATTERN = Regex("jdbc:.*://.*;.*;.*")
    val SUPPORTED_DBs = setOf(
        "sqlserver",
        "postgresql"
    )

    infix fun checkDbAvailability(creds: JDBC_Creds) {
        if ((DBsSupport isDbSupported creds).not()) throw DatabaseNotSupportedException()
        if ((DBsSupport ping creds).not()) throw ConnectException()
    }

    infix fun isDbSupported(creds: JDBC_Creds): Boolean =
        creds.first.split(":")[1] in SUPPORTED_DBs

    infix fun ping(creds: JDBC_Creds): Boolean =
        DriverManager.getConnection(creds.first, creds.second, creds.third).use { it.isValid(PING_TIMEOUT_SEC) }

    fun executeQuery(creds: JDBC_Creds, queryFunc: () -> String): ResultSet? =
        DriverManager
            .getConnection(creds.first, creds.second, creds.third)
            .createStatement()
            .use { it.executeQuery(queryFunc()) }
}