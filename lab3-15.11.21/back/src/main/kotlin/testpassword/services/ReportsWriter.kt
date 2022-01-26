package testpassword.services

import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Field
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object ReportsWriter {

    private val reportsDir: String
        get() = System.getenv("REPORTS_TEMP_STORAGE") ?: "./"

    private val buildName: String
        get() = "${reportsDir}/sqlopt_report_${
            DateTimeFormatter
                .ofPattern("yyyy-MM-dd_HH-mm-ss")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now())}.csv"

    // TODO: чот тут бред происходит
    fun serialize(objects: Iterable<Any>): String =
        buildString {
            val fields = objects.first()::class.java.declaredFields.map(Field::getName).filterNot { f -> f == "Companion" }
            append("$fields\n")
            objects.forEach { o ->
                append(
                    buildString {
                        fields.forEach { f ->
                            append("${o::class.java.getDeclaredField(f).apply { isAccessible = true }[o]},")
                        }
                    }.dropLast(1))
                append("\n")
            }
        }

    fun writeToFile(objects: Iterable<Any>) {
        File(reportsDir).mkdirs()
        FileOutputStream(File(buildName).apply { createNewFile() }, true)
            .bufferedWriter()
            .use { it.write(serialize(objects)) }
    }

    val Number.ruLocale: String
        get() = this.toString().replace(".", ",")
}