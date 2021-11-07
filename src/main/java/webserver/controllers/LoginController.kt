package webserver.controllers

import db.DataBase
import org.slf4j.LoggerFactory
import webserver.HttpRequest
import webserver.HttpResponse

class LoginController: Controller {
    override fun service(request: HttpRequest, response: HttpResponse) {
        val userId = request.parameters.getValue("userId")
        val password = request.parameters.getValue("password")

        val status: Boolean = processLogin(userId, password)

        if (status) {
            response.addHeaders("Set-Cookie", "logined=true")
            response.sendRedirect("/index.html")
        } else {
            response.addHeaders("Set-Cookie", "logined=false")
            response.sendRedirect("/user/login_failed.html")
        }
    }

    private fun processLogin(userId: String, password: String): Boolean {
        val userById = DataBase.findUserById(userId)
        return userById != null && userById.password == password
    }

    companion object {
        private val log = LoggerFactory.getLogger(LoginController::class.java)

    }
}
