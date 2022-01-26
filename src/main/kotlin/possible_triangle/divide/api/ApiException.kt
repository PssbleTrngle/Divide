package possible_triangle.divide.api

import io.ktor.http.*

class ApiException(message: String? = null, val status: HttpStatusCode = HttpStatusCode.InternalServerError) :
    Exception(message)