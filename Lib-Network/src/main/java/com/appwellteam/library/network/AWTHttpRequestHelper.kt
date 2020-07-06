package com.appwellteam.library.network

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Handler
import com.appwellteam.library.common.AWTLog
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.net.URLEncoder
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*

/**
 * Created by Sambow on 15/12/13.
 */
@Suppress("unused")
class AWTHttpRequestHelper internal constructor(private val handler: Handler) {

    private var uriMap: Map<String, Uri>? = null

    init {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }
        })
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory = sslContext.socketFactory

        HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory)
    }

    fun setUriMap(map: Map<String, Uri>) {
        uriMap = map
    }

    @Throws(IOException::class)
    fun performGet(url: String, params: Map<String, String>?): String {
        val data = parseData(params)
        val tUrl = if (data == "") {
            URL(url)
        } else {
            URL("$url?$data")
        }
        val urlConnection = initURL(tUrl, AWTNetManager.ConnectType.GET)

        return execute(urlConnection)
    }

    @Throws(IOException::class)
    fun performPost(url: String, params: Map<String, String>?): String {
        val data = parseData(params)
        val tUrl = URL(url)
        val urlConnection = initURL(tUrl, AWTNetManager.ConnectType.POST)
        urlConnection.setRequestProperty(CONTENT_TYPE, MIME_FORM_ENCODED)
        val dos = DataOutputStream(urlConnection.outputStream)
        dos.writeBytes(data)
        return execute(urlConnection)
    }

    @Throws(IOException::class)
    fun performPostJson(url: String, jsonObject: JSONObject?): String {
        val data = jsonObject?.toString() ?: ""
        AWTLog.d("AWT-Network", "[params]-$data")
        val tUrl = URL(url)
        val urlConnection = initURL(tUrl, AWTNetManager.ConnectType.POST)
        urlConnection.setRequestProperty(CONTENT_TYPE, MIME_FORM_JSON)
        val dos = DataOutputStream(urlConnection.outputStream)
        dos.write(data.toByteArray(charset("UTF-8")))
        return execute(urlConnection)
    }

    @Throws(IOException::class)
    fun performPost(url: String, params: Map<String, String>?, uriMap: Map<String, Uri>?): String {
        @Suppress("UNUSED_VARIABLE") val data = parseData(params)
        val tUrl = URL(url)
        val urlConnection = initURL(tUrl, AWTNetManager.ConnectType.POST)

        val boundary = "---------------------------" + Calendar.getInstance().timeInMillis

        urlConnection.setRequestProperty("ENCTYPE", "multipart/form-data")
        urlConnection.setRequestProperty(CONTENT_TYPE, MIME_FORM_DATA + boundary)

        val outputStream = urlConnection.outputStream
        val writer = PrintWriter(OutputStreamWriter(outputStream, CHARSET), true)

        val iterator = params?.entries?.iterator()
        while (iterator?.hasNext() == true) {
            val entry = iterator.next()
            addFormField(entry.key, entry.value, writer, boundary)
        }

        if (uriMap != null) {
            val iteratorUri = uriMap.entries.iterator()
            while (iteratorUri.hasNext()) {
                val entry = iteratorUri.next()
                addFilePart(entry.key, entry.value, writer, boundary, outputStream)
            }
        }

        outputStream.flush()

        writer.append(CRLF)
        writer.flush()

        var result = ""

        writer.append(CRLF).flush()
        writer.append("--$boundary--").append(CRLF)
        writer.close()

        // checks server's status code first
        val status = urlConnection.responseCode
        if (status == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(
                    urlConnection.inputStream))
            var line = reader.readLine()
            while (line != null) {
                result += line
                line = reader.readLine()
            }
            reader.close()
            urlConnection.disconnect()
        } else {
            throw IOException("Server returned non-OK status: $status")
        }

        return result
    }

    private fun addFormField(name: String, value: String, writer: PrintWriter, boundary: String) {
        writer.append("--").append(boundary).append(CRLF)
                .append("Content-Disposition: form-data; name=\"").append(name)
                .append("\"").append(CRLF)
                .append("Content-Type: text/plain; charset=").append(CHARSET)
                .append(CRLF).append(CRLF).append(value).append(CRLF)
        writer.flush()
    }

    @Throws(IOException::class)
    fun addFilePart(fieldName: String, uploadFile: Uri, writer: PrintWriter, boundary: String, outputStream: OutputStream) {
        val tUploadFile = File(uploadFile.path!!)
        val fileName = tUploadFile.name
        writer.append("--").append(boundary).append(CRLF)
                .append("Content-Disposition: form-data; Title=\"")
                .append(fieldName).append("\"; name=\"")
                .append(fieldName).append("\"; filename=\"").append(fileName)
                .append("\"").append(CRLF).append("Content-Type: ")
                .append(URLConnection.guessContentTypeFromName(fileName)).append(CRLF)
                .append("Content-Transfer-Encoding: binary").append(CRLF)
                .append(CRLF)

        writer.flush()

        val inputStream = FileInputStream(tUploadFile)
        val buffer = ByteArray(4096)
        var bytesRead = inputStream.read(buffer)
        while (bytesRead != -1) {
            outputStream.write(buffer, 0, bytesRead)
            bytesRead = inputStream.read(buffer)
        }
        inputStream.close()
    }

    @Throws(IOException::class)
    private fun execute(urlConnection: URLConnection): String {
        urlConnection.connect()

        val reader = BufferedReader(InputStreamReader(urlConnection.getInputStream(), CHARSET))

        val line = reader.readLine()
        reader.close()
        return line
    }

    private fun parseData(params: Map<String, String>?): String {
        val sb = StringBuilder("")

        val iterator = params?.entries?.iterator() ?: return sb.toString()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            try {
                val key = URLEncoder.encode(entry.key.trim { it <= ' ' }, CHARSET)
                val value = URLEncoder.encode(entry.value.trim { it <= ' ' }, CHARSET)
                AWTLog.d("AWT-Network", "[params]-$key | $value")
                sb.append(key)
                sb.append("=")
                sb.append(value)
                if (iterator.hasNext()) {
                    sb.append("&")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return sb.toString()
    }

    @Throws(IOException::class)
    private fun initURL(url: URL, type: AWTNetManager.ConnectType): HttpURLConnection {
        val urlConnection: HttpURLConnection
        if (url.protocol.toLowerCase().startsWith(HTTPS)) {
            urlConnection = url.openConnection() as HttpsURLConnection
            urlConnection.hostnameVerifier = HostnameVerifier { _, _ -> true }
        } else {
            urlConnection = url.openConnection() as HttpURLConnection
        }
        urlConnection.requestMethod = type.value
        urlConnection.setRequestProperty(ACCEPT_CHARSET, CHARSET)
        urlConnection.readTimeout = READ_TIMEOUT
        urlConnection.connectTimeout = CONNECT_TIMEOUT
        urlConnection.doInput = true
        urlConnection.doOutput = true
        urlConnection.instanceFollowRedirects = true
        urlConnection.useCaches = false
        urlConnection.allowUserInteraction = true
        HttpURLConnection.setFollowRedirects(true)

        return urlConnection
    }

    companion object {
        private const val CONNECT_TIMEOUT = 10000
        private const val READ_TIMEOUT = 180000
        private const val HTTPS = "https"
        private const val ACCEPT_CHARSET = "Accept-Charset"
        private const val CHARSET = "utf-8"
        private const val CONTENT_TYPE = "Content-Type"
        private const val MIME_FORM_DATA = "multipart/form-data; boundary="
        private const val MIME_FORM_ENCODED = "application/x-www-form-urlencoded"
        private const val MIME_FORM_JSON = "application/json;charset=UTF-8"
        private const val CONTENT_LENGTH = "Content-Length"
        private const val CRLF = "\r\n"
    }
}
