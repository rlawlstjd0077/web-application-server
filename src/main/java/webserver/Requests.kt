package webserver

import util.IOUtils
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

sealed class HttpRequest {
    abstract val method: Method
    abstract val path: String
    abstract val headers: Map<String, String>
    abstract val parameters: Map<String, String>
    abstract val cookies: Map<String, String>

    fun isLogined(): Boolean {
        return cookies.isNotEmpty() && cookies.get(LOGIN_COOKIE_KEY).toBoolean()
    }

    data class Get(
        override val method: Method,
        override val path: String,
        override val headers: Map<String, String>,
        override val parameters: Map<String, String>,
        override val cookies: Map<String, String>,
    ) : HttpRequest() {

        companion object {
            fun from(reader: BufferedReader, path: String): Get {
                val pathToken = path.split("?")

                val pathWithoutQueryString = pathToken[0]
                val queryString = pathToken.getOrElse(1) { "" }

                val headers = parseHeaders(reader)

                val cookies = parseCookie(headers.getOrElse(COOKIE_KEY) { "" })

                return Get(
                    method = Method.GET,
                    path = pathWithoutQueryString,
                    headers = headers,
                    parameters = parseQueryString(queryString),
                    cookies = cookies
                )
            }
        }
    }

    data class Post(
        override val method: Method,
        override val path: String,
        override val headers: Map<String, String>,
        override val parameters: Map<String, String>,
        override val cookies: Map<String, String>,
    ) : HttpRequest() {

        companion object {
            fun from(reader: BufferedReader, path: String): Post {

                val headers = parseHeaders(reader)
                val contentLength = headers.getValue(CONTENT_LENGTH_STRING).toInt()

                val queryString = IOUtils.readData(reader, contentLength)
                val parameters = parseQueryString(queryString)

                val cookies = parseCookie(headers.getOrElse(COOKIE_KEY) { "" })

                return Post(
                    method = Method.POST,
                    path = path,
                    headers = headers,
                    parameters = parameters,
                    cookies = cookies
                )
            }

            const val CONTENT_LENGTH_STRING = "Content-Length"
        }
    }

    companion object {
        fun from(inputStream: InputStream): HttpRequest {
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val requestLine = reader.readLine()

            val methodString = requestLine.split(" ")[0]
            val path = requestLine.split(" ")[1]

            return when (Method.valueOf(methodString)) {
                Method.GET -> Get.from(reader, path)
                Method.POST -> Post.from(reader, path)
            }
        }

        private fun parseHeaders(reader: BufferedReader): Map<String, String> {
            val headers = mutableMapOf<String, String>()
            var line: String?

            while (!reader.readLine().also { line = it }.isNullOrEmpty()) {
                headers[line!!.split(": ")[0]] = line!!.split(": ")[1]
            }

            return headers
        }

        private fun parseQueryString(queryString: String): Map<String, String> {
            val params = mutableMapOf<String, String>()

            if (queryString.isNotEmpty()) {
                queryString.split("&").forEach {
                    params[it.split("=")[0]] = it.split("=")[1]
                }
            }

            return params
        }

        private fun parseCookie(cookieString: String): Map<String, String> {
            val cookies = mutableMapOf<String, String>()

            if (cookieString.isNotEmpty()) {
                cookieString.split("; ").forEach {
                    cookies[it.split("=")[0]] = it.split("=")[1]
                }
            }
            return cookies
        }

        const val COOKIE_KEY = "Cookie"
        const val LOGIN_COOKIE_KEY = "logined"
    }
}

enum class Method {
    GET, POST
}
