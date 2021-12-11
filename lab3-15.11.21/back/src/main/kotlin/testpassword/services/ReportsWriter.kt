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

    operator fun invoke(objects: Iterable<Any>) {
        File(reportsDir).mkdirs()
        FileOutputStream(
            File("${reportsDir}/sqlopt_report_${
                DateTimeFormatter
                    .ofPattern("yyyy-MM-dd_HH-mm-ss")
                    .withZone(ZoneOffset.UTC)
                    .format(Instant.now())}.csv")
                .apply { createNewFile() }, true
        ).bufferedWriter()
            .use {
                val fields = objects.first()::class.java.declaredFields.map(Field::getName).filterNot { f -> f == "Companion" }
                it.write(fields.joinToString(","))
                it.write("\n")
                objects.forEach { o ->
                    it.write(
                        buildString {
                            fields.forEach { f ->
                                append("${o::class.java.getDeclaredField(f).apply { isAccessible = true }[o]},")
                            }
                        }.dropLast(1))
                    it.write("\n")
                }
            }
    }

    val Number.ruLocale: String
        get() = this.toString().replace(".", ",")
}