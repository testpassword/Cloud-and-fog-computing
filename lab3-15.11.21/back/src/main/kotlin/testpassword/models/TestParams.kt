package testpassword.models

import kotlinx.serialization.Serializable
import testpassword.services.DBsSupport
import testpassword.services.JDBC_Creds

enum class OUTPUT_MODE { EMAIL, HTTP, SMB }

@Serializable data class TestParams(
    val connectionUrl: String,
    val queries: List<String>,
    val outputMode: OUTPUT_MODE,
    val outputParams: String = "",
    val saveBetter: Boolean = false,
) {

    val creds: JDBC_Creds
        get() =
            if (DBsSupport.CONNECTION_URL_PATTERN.matches(connectionUrl)) {
                val (url, login, pass) = connectionUrl.split(";")
                JDBC_Creds(url, login, pass)
            } else throw java.sql.SQLClientInfoException()
}