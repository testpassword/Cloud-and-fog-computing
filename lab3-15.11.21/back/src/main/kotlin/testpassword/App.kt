package testpassword

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.json.Json
import testpassword.plugins.*
import testpassword.routes.actions
import testpassword.services.DBsLock
import testpassword.services.DatabaseNotSupportedException
import testpassword.services.DBsSupport
import java.rmi.ConnectException
import java.sql.SQLClientInfoException

fun Application.configureModules() =
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }
        )
    }

fun Application.configureSecurity() =
    install(CORS) {
        methods.addAll(HttpMethod.DefaultMethods)
        anyHost()
    }

fun Application.configureRouting() =
    routing {
        actions()
    }

fun Application.configureExceptionHandlers() =
    install(StatusPages) {
        exception<DatabaseNotSupportedException> {
            call.respond(HttpStatusCode.BadRequest, """
                This database not supported yet.
                Supported: ${DBsSupport.SUPPORTED_DBs.joinToString(", ")}
            """.trimIndent())
        }
        exception<SQLClientInfoException> {
            call.respond(HttpStatusCode.NotFound, """
                Provided connectionUrl doesn't not match pattern: '${DBsSupport.CONNECTION_URL_PATTERN}'
            """.trimIndent())
        }
        exception<ConnectException> {
            call.respond(HttpStatusCode.GatewayTimeout, """
                Can't ping database. Please check it's availability and try again.
            """.trimIndent())
        }
    }

fun main() {
    try {
        embeddedServer(Netty,
            System.getenv("SERVICE_PORT").toIntOrNull() ?: 80,
            System.getenv("SERVICE_HOST") ?: "0.0.0.0"
        ) {
            DBsLock(System.getenv("REDIS_CACHE_CREDS"))
            configureModules()
            configureSecurity()
            configureExceptionHandlers()
            configureRouting()
        }.start(wait = true)
    } catch (e: NumberFormatException) {
        printErr("port should be in range [0; 65535]")
    }
}