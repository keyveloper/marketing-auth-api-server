package org.example.authapiserver.exception

import org.example.authapiserver.enums.MSAServiceErrorCode
import org.springframework.http.HttpStatus

data class NotFoundUserException(
    override val httpStatus: HttpStatus = HttpStatus.NOT_FOUND,
    override val msaServiceErrorCode: MSAServiceErrorCode = MSAServiceErrorCode.USER_NOT_FOUND,
    override val logics: String,
    override val message: String = "User not found",
) : MSAServerException(httpStatus, msaServiceErrorCode, logics, message)
