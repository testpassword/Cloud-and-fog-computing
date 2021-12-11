package testpassword.consumers

import org.apache.commons.mail.SimpleEmail
import java.net.URL

object Postman {

    private val sender: SimpleEmail =
        SimpleEmail().apply {
            val creds = parseCreds()
            hostName = creds.server.host
            setSmtpPort(creds.server.port)
            setAuthentication(creds.address, creds.pass)
            isSSLOnConnect = true
            setFrom(creds.address)
        }

    private fun parseCreds() =
        System.getenv("EMAIL_SENDER").split(";").let {
            object {
                val address = it[0]
                val pass = it[1]
                val server = URL(it[2])
            }
        }

    operator fun invoke(to: String, subject: String, body: String): String =
        sender.apply {
            setSubject(subject)
            setMsg(body)
            addTo(to)
        }.send()
}