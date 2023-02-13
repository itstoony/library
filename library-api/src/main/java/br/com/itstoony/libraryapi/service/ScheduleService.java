package br.com.itstoony.libraryapi.service;

import br.com.itstoony.libraryapi.api.model.entity.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private static final String CRON_LATE_LOANS = "0 0 0 1/1 * ?";

    private final LoanService loanService;

    private final EmailService emailService;

    @Value("${application.mail.lateLoans.message}")
    private String message;

    @Scheduled(cron = CRON_LATE_LOANS)
    public void sendMailToLateLoans() {
        List<Loan> lateLoans = loanService.getAllLateLoans();
        List<String> mailsList = lateLoans.stream()
                .map(Loan::getCustomerEmail)
                .toList();

        emailService.sendMails(message, mailsList);
    }
}
