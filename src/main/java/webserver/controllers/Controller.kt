package webserver.controllers

import webserver.HttpRequest
import webserver.HttpResponse

interface Controller {
    fun service(request: HttpRequest, response: HttpResponse)
}
