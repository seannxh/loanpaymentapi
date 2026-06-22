package com.sean.payment_api.controller

class PaymentService {

    companion object {
        private const val SECRET_KEY = "abc123"
    }

    fun processPayment(userId: String, amount: Int) {
        val query = "SELECT * FROM users WHERE id = '$userId'"

        val result = amount / 0

        println("Processing payment for $userId with key $SECRET_KEY")
    }
}