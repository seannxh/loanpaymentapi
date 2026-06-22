package com.sean.payment_api.service

import com.sean.payment_api.data.InstallmentStatus
import com.sean.payment_api.repository.InstallmentRepository
import com.sean.payment_api.repository.LoanRepository
import com.sean.payment_api.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ReminderService(
    private val installmentRepository: InstallmentRepository,
    private val loanRepository: LoanRepository,
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    @Value("\${app.mail.reminder-days:3}") private val reminderDays: Long,
) {
    private val log = LoggerFactory.getLogger(ReminderService::class.java)

    // Runs on the schedule in app.mail.reminder-cron (default 09:00 daily).
    // Emails users about PENDING installments due within the reminder window,
    // marking each as reminded only if the email actually went out.
    @Scheduled(cron = "\${app.mail.reminder-cron:0 0 9 * * *}")
    @Transactional
    fun sendUpcomingReminders() {
        val today = LocalDate.now()
        val until = today.plusDays(reminderDays)
        val due = installmentRepository.findByStatusAndReminderSentFalseAndDueDateBetween(
            InstallmentStatus.PENDING,
            today,
            until,
        )
        if (due.isEmpty()) return
        log.info("Found {} upcoming installment(s) to remind about", due.size)

        for (installment in due) {
            val loan = loanRepository.findById(installment.loanId).orElse(null) ?: continue
            val user = userRepository.findById(loan.userId).orElse(null) ?: continue
            val sent = emailService.sendReminder(
                to = user.email,
                loanName = loan.name,
                installmentNumber = installment.installmentNumber,
                dueDate = installment.dueDate,
                amount = installment.paymentAmount,
            )
            if (sent) {
                installmentRepository.save(installment.copy(reminderSent = true))
            }
        }
    }
}
