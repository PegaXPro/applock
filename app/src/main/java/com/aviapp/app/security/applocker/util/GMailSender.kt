package com.aviapp.app.security.applocker.util

import android.content.Context
import android.util.Log
import com.aviapp.app.security.applocker.BuildConfig
import com.aviapp.app.security.applocker.R
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.AccessController
import java.security.PrivilegedAction
import java.security.Provider
import java.security.Security
import java.util.*
import javax.activation.DataHandler
import javax.activation.DataSource
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class GMailSender(private val context: Context) : Authenticator() {

    private val user = "rusakovhariton@gmail.com"
    private val password = "nqhxdxwuwgqwobxe"
    private val appEmail = "help.aviapp@gmail.com"

    private val session: Session

    override fun getPasswordAuthentication(): PasswordAuthentication {
        return PasswordAuthentication(user, password)
    }

    fun sendMail(clientEmail: String, messageText: String) {

        val versionCode = BuildConfig.VERSION_NAME
        val appName = context.getString(R.string.app_name)
        val sub = "$appName: $versionCode. Client email: $clientEmail"

        try {
            val message = MimeMessage(session)
            val handler = DataHandler(ByteArrayDataSource(messageText.toByteArray(), "text/plain"))
            message.sender = InternetAddress(user)
            message.subject = sub
            message.dataHandler = handler
            if (appEmail.indexOf(',') > 0) message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(appEmail)
            ) else message.setRecipient(
                Message.RecipientType.TO, InternetAddress(appEmail)
            )
            Transport.send(message)
        } catch (e: Exception) {
            Log.d("AndroidView2", "Error in sending: $e")
        }
    }

    inner class ByteArrayDataSource(private var data: ByteArray, private val type: String) : DataSource {


        override fun getContentType(): String {
            return type
        }

        @Throws(IOException::class)
        override fun getInputStream(): InputStream {
            return ByteArrayInputStream(data)
        }

        override fun getName(): String {
            return "ByteArrayDataSource"
        }

        @Throws(IOException::class)
        override fun getOutputStream(): OutputStream {
            throw IOException("Not Supported")
        }
    }

    class JSSEProvider : Provider("HarmonyJSSE", 1.0, "Harmony JSSE Provider") {
        init {
            AccessController.doPrivileged(PrivilegedAction<Void?> {
                put("SSLContext.TLS", "org.apache.harmony.xnet.provider.jsse.SSLContextImpl")
                put("Alg.Alias.SSLContext.TLSv1", "TLS")
                put(
                    "KeyManagerFactory.X509",
                    "org.apache.harmony.xnet.provider.jsse.KeyManagerFactoryImpl"
                )
                put(
                    "TrustManagerFactory.X509",
                    "org.apache.harmony.xnet.provider.jsse.TrustManagerFactoryImpl"
                )
                null
            })
        }
    }

    init {
        Security.addProvider(JSSEProvider())
    }

    init {
        val props = Properties()
        props.setProperty("mail.transport.protocol", "smtp")
        val mailhost = "smtp.gmail.com"
        props.setProperty("mail.host", mailhost)
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.port"] = "465"
        props["mail.smtp.socketFactory.port"] = "465"
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.socketFactory.fallback"] = "false"
        props.setProperty("mail.smtp.quitwait", "false")
        session = Session.getDefaultInstance(props, this)
    }
}