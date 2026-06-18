package com.sean.payment_api.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice
class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): Map<String, Any> {
        val errors = ex.bindingResult.fieldErrors
            .map{"${it.field}: ${it.defaultMessage}"}
        return mapOf(
            "error" to "VALIDATION_FAILED",
            "messages" to errors
        )

    }
    @ExceptionHandler(RuntimeException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleRuntimeException(ex: RuntimeException): Map<String, Any> {
        return mapOf(
            "error" to "INTERNAL_ERROR",
            "message" to (ex.message ?: "Run Time failed")
        )
    }
}

