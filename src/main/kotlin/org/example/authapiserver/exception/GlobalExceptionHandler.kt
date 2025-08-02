package org.example.authapiserver.exception

import org.example.authapiserver.dto.MSABusinessErrorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MSAServerException::class)
    fun handleBusinessException(exception: MSAServerException): ResponseEntity<MSABusinessErrorResponse> {
        return ResponseEntity.ok().body(
            MSABusinessErrorResponse.of(
                exception.httpStatus,
                exception.msaServiceErrorCode,
                exception.message,
                exception.logics
            )
        )
    }

}