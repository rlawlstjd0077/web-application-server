package webserver.controllers

import db.DataBase
import org.slf4j.LoggerFactory
import webserver.HttpRequest
import webserver.HttpResponse

class ListUserController: Controller {
    override fun service(request: HttpRequest, response: HttpResponse) {
        if (!request.isLogined()) {
            response.sendRedirect("/user/login.html")
            return
        }

        val bodyBuilder = StringBuilder()
        val users = DataBase.findAll()
        for (user in users) {
            bodyBuilder.append(String.format("<p>%s</p>", user.name))
        }

        response.forwardBody(bodyBuilder.toString())
    }

    companion object {
        private val log = LoggerFactory.getLogger(ListUserController::class.java)

    }
}
