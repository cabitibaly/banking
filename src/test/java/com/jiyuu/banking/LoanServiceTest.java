package com.jiyuu.banking;

import com.jiyuu.banking.audit.context.AuditContext;
import com.jiyuu.banking.dto.LoanBaseResponse;
import com.jiyuu.banking.dto.LoanRequest;
import com.jiyuu.banking.entity.Account;
import com.jiyuu.banking.entity.Customer;
import com.jiyuu.banking.entity.Loan;
import com.jiyuu.banking.entity.User;
import com.jiyuu.banking.enums.*;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.AccountRepository;
import com.jiyuu.banking.repository.CustomerRepository;
import com.jiyuu.banking.repository.LoanRepository;
import com.jiyuu.banking.service.LoanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {
    @Mock
    private LoanRepository loanRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private LoanService loanService;

    @Test
    public void shouldThrowValidationExceptionWhenDurationIsInvalid() {
        LoanRequest request = new LoanRequest(
                "12345678",
                "ABC123456789",
                BigDecimal.valueOf(1000000),
                "Achat de console",
                0,
                "PERSONAL"
        );

        assertThrows(
               ValidationException.class,
                () -> this.loanService.createLoan(request)
        );
    }

    @Test
    public void shouldThrowValidationExceptionWhenAmountIsInvalid() {
        LoanRequest request = new LoanRequest(
                "12345678",
                "ABC123456789",
                BigDecimal.valueOf(0),
                "Achat de console",
                12,
                "PERSONAL"
        );

        assertThrows(
               ValidationException.class,
                () -> this.loanService.createLoan(request)
        );
    }

    @Test
    public void shouldThrowResourceNotFountWhenCustomerIsNotFound() {
        LoanRequest request = new LoanRequest(
                "12345678",
                "ABC123456789",
                BigDecimal.valueOf(1000000),
                "Achat de console",
                12,
                "PERSONAL"
        );

        when(this.customerRepository.findBytelephoneCustomer("12345678"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> this.loanService.createLoan(request)
        );
    }

    @Test
    public void shouldThrowResourceNotFountWhenAccountIsNotFound() {
        LoanRequest request = new LoanRequest(
                "12345678",
                "ABC123456789",
                BigDecimal.valueOf(1000000),
                "Achat de console",
                12,
                "PERSONAL"
        );

        Customer customer = this.buildCustomer();

        when(this.customerRepository.findBytelephoneCustomer("12345678"))
                .thenReturn(Optional.of(customer));

        when(this.accountRepository.findBynumeroAccount("ABC123456789"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> this.loanService.createLoan(request)
        );
    }

    @Test
    public void shouldCreateLoanSuccessfullyWithDraftStatus.() {
        Loan loan = this.buildLoan();
        LoanRequest request = new LoanRequest(
                "12345678",
                "ABC123456789",
                BigDecimal.valueOf(1000000),
                "Achat de console",
                12,
                "PERSONAL"
        );

        Customer customer = this.buildCustomer();
        when(this.customerRepository.findBytelephoneCustomer("12345678"))
                .thenReturn(Optional.of(customer));

        Account account = this.buildAccount();
        when(this.accountRepository.findBynumeroAccount("ABC123456789"))
                .thenReturn(Optional.of(account));

        when(this.loanRepository.save(any(Loan.class)))
                .thenReturn(loan);

        LoanBaseResponse response = this.loanService.createLoan(request);

        verify(loanRepository).save(any(Loan.class));
        assertNotNull(response);
        assertEquals(LoanStatus.DRAFT.toString(), response.status());
        assertEquals(response, AuditContext.getNewValue());
    }

    private Loan buildLoan() {
        Account account = this.buildAccount();
        Customer customer = this.buildCustomer();

        return Loan.builder()
                .idLoan(1L)
                .account(account)
                .customer(customer)
                .loanStatus(LoanStatus.DRAFT)
                .duration(12)
                .reason("Achat de console")
                .amount(BigDecimal.valueOf(1000000))
                .loanType(LoanType.PERSONAL)
                .build();
    }

    private Customer buildCustomer() {
        return Customer.builder()
                .idCustomer(1L)
                .dateNaissance(LocalDateTime.of(2000, 6, 22, 0, 0))
                .telephoneCustomer("12345678")
                .nomCustomer("Kyotaka Ayanokoji")
                .numeroCustomer("CLI12345678")
                .statusCustomer(StatusCustomer.VERIFIED)
                .kycDocuments(new ArrayList<>())
                .build();
    }

    private Account buildAccount() {
        return Account.builder()
                .idAccount(1L)
                .accountType(AccountType.CHECKING)
                .accountStatus(AccountStatus.ACTIVE)
                .numeroAccount("ABC12356789")
                .currencyAccount(Currency.XOF)
                .decouvert(BigDecimal.valueOf(50000))
                .soldeAccount(BigDecimal.valueOf(1000000))
                .build();
    }

}
