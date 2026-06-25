package com.jiyuu.banking;

import com.jiyuu.banking.entity.Account;
import com.jiyuu.banking.entity.Customer;
import com.jiyuu.banking.entity.Loan;
import com.jiyuu.banking.entity.LoanInstallment;
import com.jiyuu.banking.enums.InstallmentStatus;
import com.jiyuu.banking.enums.LoanStatus;
import com.jiyuu.banking.enums.LoanType;
import com.jiyuu.banking.repository.LoanInstallmentRepository;
import com.jiyuu.banking.service.LoanInstallmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class InstallmentServiceTest {
    @Mock
    private LoanInstallmentRepository installmentRepository;

    @InjectMocks
    private LoanInstallmentService installmentService;

    @Captor
    private ArgumentCaptor<List<LoanInstallment>> installmentsCaptor;

    @Test
    public void shouldGenerateInstallmentSuccessfully() {
        Loan loan = this.buildLoan();

        this.installmentService.generateInstallment(loan);
        verify(this.installmentRepository).saveAll(installmentsCaptor.capture());

        List<LoanInstallment> installments = installmentsCaptor.getValue();
        assertEquals(loan.getDuration(), installments.size());

        LoanInstallment first = installments.get(0);
        assertEquals(1, first.getInstallmentNumber());
        assertEquals(InstallmentStatus.PENDING, first.getInstallmentStatus());
        assertEquals(loan, first.getLoan());
        assertEquals(loan.getDisbursementDate().plusMonths(1), first.getDueDate());

        LoanInstallment last = installments.get(installments.size() - 1);
        assertEquals(loan.getDuration(), last.getInstallmentNumber());
        assertEquals(loan.getDisbursementDate().plusMonths(loan.getDuration()), last.getDueDate());
    }

    private Loan buildLoan() {
        return Loan.builder()
                .idLoan(1L)
                .loanStatus(LoanStatus.PENDING)
                .duration(12)
                .disbursementDate(LocalDate.now())
                .interestRate(BigDecimal.valueOf(10))
                .remainingAmount(BigDecimal.valueOf(1000000))
                .reason("Achat de console")
                .amount(BigDecimal.valueOf(1000000))
                .loanType(LoanType.PERSONAL)
                .build();
    }
}
