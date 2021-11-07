package webserver

import java.io.DataOutputStream
import java.nio.file.Files
import java.nio.file.Paths

class HttpResponse(private val dos: DataOutputStream) {
    private val headers = mutableMapOf<String, String>()

    fun addHeaders(key: String, value: String) {
        headers[key] = value
    }

    fun forward(resourceUrl: String) {
        val body = Files.readAllBytes(Paths.get(WEB_APP_PATH, resourceUrl))

        val contentType = ContentType.from(resourceUrl)
        addHeaders(ContentType.KEY, contentType.value)
        addHeaders(CONTENT_LENGTH_KEY, body.size.toString())
        response200Header()
        responseBody(body)
    }

    fun forwardBody(body: String) {
        val bodyContents = body.toByteArray()
        addHeaders(ContentType.KEY, ContentType.HTML.value)
        addHeaders(CONTENT_LENGTH_KEY, bodyContents.size.toString())
        responseBody(bodyContents)
    }

    fun sendRedirect(redirectUrl: String) {
        dos.writeBytes("HTTP/1.1 302 FOUND \r\n")
        processHeaders()
        dos.writeBytes("Location: $redirectUrl \r\n")
        dos.writeNewLine()
    }

    private fun response200Header() {
        dos.writeBytes("HTTP/1.1 200 OK \r\n")
        processHeaders()
        dos.writeNewLine()
    }

    private fun responseBody(body: ByteArray) {
        dos.write(body, 0, body.size)
        dos.writeNewLine()
        dos.flush()
    }

    private fun processHeaders() {
        headers.forEach{ dos.writeBytes("${it.key}: ${it.value} \r\n") }
    }

    enum class ContentType(val value: String) {
        CSS("text/css"),
        JS("application/javascript"),
        HTML("text/html;charset=utf-8");

        companion object {
            const val KEY = "Content-Type"

            fun from(url: String): ContentType {
                return when {
                    url.endsWith(".css") -> CSS
                    url.endsWith(".js") -> JS
                    else -> HTML
                }
            }
        }
    }

    companion object {
        const val WEB_APP_PATH = "./webapp"
        const val CONTENT_LENGTH_KEY = "Content-Length"
    }
}

fun DataOutputStream.writeNewLine() = this.writeBytes("\r\n")
