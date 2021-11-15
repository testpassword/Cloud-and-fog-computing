package testpassword.models

import kotlinx.serialization.Serializable
import testpassword.services.DBsSupport
import testpassword.services.JDBC_Creds

enum class OUTPUT_MODE { EMAIL, HTTP, SMB }

@Serializable data class TestParams(
    val connectionUrl: String,
    val saveBetter: Boolean,
    val queries: List<String>,
    val outputMode: OUTPUT_MODE
) {

    val creds: JDBC_Creds
        get() =
            if (DBsSupport.CONNECTION_URL_PATTERN.matches(connectionUrl)) {
                val (url, rawLogin, rawPass) = connectionUrl.split(";")
                val login = rawLogin.split("user=").last()
                val password = rawPass.split("password=").last()
                JDBC_Creds(url, login, password)
            } else throw java.sql.SQLClientInfoException()
}