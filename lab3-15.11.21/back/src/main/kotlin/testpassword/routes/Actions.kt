package testpassword.routes

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import testpassword.consumers.SMB
import testpassword.services.DBsLock
import testpassword.services.DBsSupport
import testpassword.models.TestParams
import testpassword.models.TestResult
import testpassword.services.DBsTester
import testpassword.services.ReportsWriter
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// TODO: установить время блокировки (хранения записи в redis), периодически продлевать его, если операция ещё идёт

fun Route.actions() =
    route("/testing/") {
        post {
            val testParams = call.receive<TestParams>()
            val creds = testParams.creds
            call.respond(DBsLock.executeLocking(creds.first) {
                DBsSupport(creds).let { sup ->
                    sup.checkDbAvailability()
                    val tester = DBsTester(testParams.queries, sup)
                    val benchmarkingResult = tester.benchmarkQuery()
                    val best = tester.findBest(benchmarkingResult)
                    val origTime = sup.measureQuery { testParams.queries.first() }
                    if (testParams.saveBetter && best != null) sup.execute { best.first.createIndexStatement }
                    // TODO: выбор куда сохранять отчёт
                    SMB(
                        testParams.outputParams,
                        ReportsWriter.serialize(benchmarkingResult.map {
                            object {
                                val indexStatement = it.key.createIndexStatement
                                val timeTaken = it.value
                                val diff = origTime - it.value
                            }
                        }).toByteArray(),
                        "sqlopt_report_${
                            DateTimeFormatter
                                .ofPattern("yyyy-MM-dd_HH-mm-ss")
                                .withZone(ZoneOffset.UTC)
                                .format(Instant.now())}.csv"
                    )
                    TestResult(
                        best!!.first.createIndexStatement,
                        origTime,
                        best.second,
                        best.second - origTime
                    )
                }
            })
        }

        get {
            call.respond("WORKING ON AZURE")
        }
    }