package webserver.controllers

import webserver.HttpRequest
import webserver.HttpResponse

class IndexController: Controller {
    override fun service(request: HttpRequest, response: HttpResponse) {
        response.sendRedirect("/index.html")
    }
}
