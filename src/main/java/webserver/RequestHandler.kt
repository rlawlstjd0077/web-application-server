package webserver

import org.slf4j.LoggerFactory
import webserver.controllers.Controller
import webserver.controllers.RequestMapping
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket

class RequestHandler(private val connection: Socket): Thread() {

    override fun run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.inetAddress, connection.port)

        try {
            connection.getInputStream().use { inputStream ->
                connection.getOutputStream().use { outputStream ->
                    val dos = DataOutputStream(outputStream)
                    val request = HttpRequest.from(inputStream)
                    val response = HttpResponse(dos)

                    val controller = RequestMapping.getController(request.path) ?: object : Controller {
                        override fun service(request: HttpRequest, response: HttpResponse) =
                            response.forward(request.path)
                    }

                    controller.service(request, response)
                }
            }
        } catch (e: IOException) {
            log.error(e.message)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(RequestHandler::class.java)
    }
}
