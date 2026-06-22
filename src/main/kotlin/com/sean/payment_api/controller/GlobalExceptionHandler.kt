package com.sean.payment_api.controller

import com.sean.payment_api.exception.BadRequestException
import com.sean.payment_api.exception.ConflictException
import com.sean.payment_api.exception.NotFoundException
import com.sean.payment_api.exception.UnauthorizedException
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

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: NotFoundException): Map<String, Any> =
        mapOf("error" to "NOT_FOUND", "message" to (ex.message ?: "Resource not found"))

    @ExceptionHandler(UnauthorizedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleUnauthorized(ex: UnauthorizedException): Map<String, Any> =
        mapOf("error" to "UNAUTHORIZED", "message" to (ex.message ?: "Unauthorized"))

    @ExceptionHandler(ConflictException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleConflict(ex: ConflictException): Map<String, Any> =
        mapOf("error" to "CONFLICT", "message" to (ex.message ?: "Conflict"))

    @ExceptionHandler(BadRequestException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadRequest(ex: BadRequestException): Map<String, Any> =
        mapOf("error" to "BAD_REQUEST", "message" to (ex.message ?: "Bad request"))
    @ExceptionHandler(RuntimeException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleRuntimeException(ex: RuntimeException): Map<String, Any> {
        return mapOf(
            "error" to "INTERNAL_ERROR",
            "message" to (ex.message ?: "Run Time failed")
        )
    }
}

