package com.sean.payment_api.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class EmailService(
    // Optional: the JavaMailSender bean only exists when spring.mail.host is set,
    // so the app runs fine without SMTP configured (reminders are just skipped).
    private val mailSender: ObjectProvider<JavaMailSender>,
    @Value("\${app.mail.from:no-reply@loanpay.local}") private val from: String,
) {
    private val log = LoggerFactory.getLogger(EmailService::class.java)

    /** Returns true only if an email was actually sent. */
    fun sendReminder(
        to: String,
        loanName: String,
        installmentNumber: Int,
        dueDate: LocalDate,
        amount: BigDecimal,
    ): Boolean {
        val sender = mailSender.ifAvailable
        if (sender == null) {
            log.info("SMTP not configured — skipping reminder to {} for loan '{}'", to, loanName)
            return false
        }
        return try {
            val message = SimpleMailMessage().apply {
                setFrom(from)
                setTo(to)
                subject = "Payment reminder: $loanName"
                text = """
                    Hi,

                    This is a friendly reminder that payment #$installmentNumber of $amount
                    for your loan "$loanName" is due on $dueDate.

                    — LoanPay
                """.trimIndent()
            }
            sender.send(message)
            log.info("Sent payment reminder to {} for loan '{}' installment {}", to, loanName, installmentNumber)
            true
        } catch (e: Exception) {
            log.warn("Failed to send reminder to {}: {}", to, e.message)
            false
        }
    }

    fun sendPasswordReset(to: String, resetLink: String): Boolean =
        send(
            to = to,
            subject = "Reset your LoanPay password",
            body = """
                Hi,

                We received a request to reset your LoanPay password.
                Open the link below to choose a new one (it expires in 30 minutes):

                $resetLink

                If you didn't request this, you can ignore this email.

                — LoanPay
            """.trimIndent(),
        )

    fun sendTest(to: String): Boolean =
        send(
            to = to,
            subject = "LoanPay test reminder",
            body = "This is a test reminder from LoanPay — your email is configured correctly. 🎉",
        )

    private fun send(to: String, subject: String, body: String): Boolean {
        val sender = mailSender.ifAvailable
        if (sender == null) {
            log.info("SMTP not configured — skipping email to {}", to)
            return false
        }
        return try {
            val message = SimpleMailMessage().apply {
                setFrom(from)
                setTo(to)
                this.subject = subject
                text = body
            }
            sender.send(message)
            log.info("Sent '{}' email to {}", subject, to)
            true
        } catch (e: Exception) {
            log.warn("Failed to send email to {}: {}", to, e.message)
            false
        }
    }
}
