package webserver.controllers

import db.DataBase
import model.User
import org.slf4j.LoggerFactory
import webserver.HttpRequest
import webserver.HttpResponse

class CreateUserController: Controller {
    override fun service(request: HttpRequest, response: HttpResponse) {
        val userId = request.parameters.getValue("userId")
        val password = request.parameters.getValue("password")
        val userName = request.parameters.getValue("name")
        val email = request.parameters.getValue("email")

        processSignUp(userId, password, userName, email)
        response.sendRedirect("/index.html")
    }

    private fun processSignUp(
        userId: String,
        password: String,
        name: String,
        email: String
    ) {
        val user = User(userId, password, name, email)
        DataBase.addUser(user)
        log.debug("user : {}", user)
    }

    companion object {
        private val log = LoggerFactory.getLogger(CreateUserController::class.java)

    }
}
