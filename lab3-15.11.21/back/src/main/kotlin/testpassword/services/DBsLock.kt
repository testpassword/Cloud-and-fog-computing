package testpassword.services

import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisShardInfo
import testpassword.plugins.printErr
import java.sql.SQLException

class DatabaseBusyException: SQLException()

object DBsLock {

    private var client: Jedis? = null
    operator fun invoke(): Unit =
        try {
            client = Jedis(parseCredsFromEnv()).also { it.ping() }
        } catch (e: Exception) {
            printErr(when (e) {
                is IndexOutOfBoundsException -> "REDIS_CACHE_CREDS should match pattern 'url:port;password'"
                is NumberFormatException -> "port should be in range [0; 65535]"
                else -> "Unexpected exception: ${e.stackTraceToString()}"
            })
        }

    private infix fun <T> isInit(onSuccess: () -> T): T =
        if (client != null) onSuccess() else throw IllegalStateException("you should call invoke() method before use it to initialize")

    private fun parseCredsFromEnv(): JedisShardInfo {
        val (url, pass) = System.getenv("REDIS_CACHE_CREDS").split(";").let { it[0] to it[1] }
        val (host, port) = url.split(":")
        return JedisShardInfo(host, port.toInt(), true).apply { password = pass }
    }

    operator fun contains(dbUrl: String): Boolean = isInit { client!!.get(dbUrl) != null }

    operator fun plus(dbUrl: String): Unit = isInit { client!!.set(dbUrl, true.toString()) }

    operator fun minus(dbUrl: String): Unit = isInit { client!!.set(dbUrl, false.toString()) }

    fun executeLocking(dbUrl: String, lockingOps: () -> Unit): Unit =
        if (dbUrl !in this) {
            this + dbUrl
            lockingOps()
            this - dbUrl
        } else throw DatabaseBusyException()
}