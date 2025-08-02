package org.example.authapiserver.dto

import org.example.authapiserver.enums.MSAServiceErrorCode
import org.springframework.http.HttpStatus

class GetUserInfoResponseFromServer(
    override val httpStatus: HttpStatus,
    override val msaServiceErrorCode: MSAServiceErrorCode,
    override val errorMessage: String? = null,
    override val logics: String? = null,
    val result: GetUserInfoResult? = null
) : MSABusinessErrorResponse(httpStatus, msaServiceErrorCode, errorMessage, logics) {

    companion object {
        fun of(result: GetUserInfoResult): GetUserInfoResponseFromServer {
            return GetUserInfoResponseFromServer(
                httpStatus = HttpStatus.OK,
                msaServiceErrorCode = MSAServiceErrorCode.OK,
                result = result
            )
        }
    }
}
