package webserver.controllers


object RequestMapping {
    private val controllers = mutableMapOf(
        "/user/create" to CreateUserController(),
        "/user/login" to LoginController(),
        "/user/list" to ListUserController(),
        "/" to IndexController(),
    )

    fun getController(requestUrl: String): Controller? {
        return controllers[requestUrl]
    }
}

