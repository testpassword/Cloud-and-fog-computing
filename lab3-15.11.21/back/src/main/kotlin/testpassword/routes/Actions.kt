package testpassword.routes

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import testpassword.services.DBsLock
import testpassword.services.DBsSupport
import testpassword.models.TestParams

// TODO: установить время блокировки (хранения записи в redis), периодически продлевать его, если операция ещё идёт

fun Route.actions() =
    route("/testing/") {
        post {
            val testParams = call.receive<TestParams>()
            val creds = testParams.creds
            DBsSupport.checkDbAvailability(creds)
            DBsLock.executeLocking(creds.first) {

                val results = DBsSupport.executeQuery(creds) {
                    "SELECT @@VERSION as 'ver'"
                }

            }
        }
    }