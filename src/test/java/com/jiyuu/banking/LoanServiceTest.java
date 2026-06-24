package com.jiyuu.banking;

import com.jiyuu.banking.audit.context.AuditContext;
import com.jiyuu.banking.dto.*;
import com.jiyuu.banking.entity.Account;
import com.jiyuu.banking.entity.Customer;
import com.jiyuu.banking.entity.Loan;
import com.jiyuu.banking.enums.*;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.AccountRepository;
import com.jiyuu.banking.repository.CustomerRepository;
import com.jiyuu.banking.repository.LoanRepository;
import com.jiyuu.banking.service.LoanInstallmentService;
import com.jiyuu.banking.service.LoanService;
import com.jiyuu.banking.service.TransactionsService;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {
    @Mock
    private LoanRepository loanRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionsService transactionsService;

    @Mock
    private LoanInstallmentService installmentService;

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
    public void shouldThrowResourceNotFoundWhenCustomerIsNotFound() {
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
    public void shouldThrowResourceNotFoundWhenAccountIsNotFound() {
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
    public void shouldCreateLoanSuccessfullyWithDraftStatus() {
        Loan loan = this.buildLoan(LoanStatus.DRAFT);
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

    @Test
    public void shouldThrowResourceNotFoundWhenLoanIsNotFound() {
        when(this.loanRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> this.loanService.rejectLoan(1L)
        );
    }

    @Test
    public void shouldThrowValidationExceptionWhenLoanStatusIsDRAFT() {
        Loan loan = this.buildLoan(LoanStatus.DRAFT);

        when(this.loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        assertThrows(
                ValidationException.class,
                () -> this.loanService.rejectLoan(1L)
        );
    }

    @Test
    public void shouldThrowValidationExceptionWhenLoanStatusIsAlreadyTreated() {
        Loan loan = this.buildLoan(LoanStatus.APPROVED);

        when(this.loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        assertThrows(
                ValidationException.class,
                () -> this.loanService.rejectLoan(1L)
        );
    }

    @Test
    public void shouldRejectLoanSuccessfully() {
        Loan loan = this.buildLoan(LoanStatus.PENDING);
        Loan loanSave = this.buildLoan(LoanStatus.REJECTED);

        when(this.loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        when(this.loanRepository.save(any(Loan.class)))
                .thenReturn(loanSave);

        LoanBaseResponse oldValue = LoanBaseResponse.of(loan);
        this.loanService.rejectLoan(1L);
        LoanBaseResponse newValue = LoanBaseResponse.of(loanSave);

        verify(loanRepository).save(loan);
        assertEquals(oldValue, AuditContext.getOldValue());
        assertEquals(newValue, AuditContext.getNewValue());
    }

    @Test
    public void shouldThrowValidationExceptionWhenInterestIsInvalid() {
        ApproveLoanRequest request = new ApproveLoanRequest(
                BigDecimal.ZERO,
                "Il le merite"
        );

        assertThrows(
                ValidationException.class,
                () -> this.loanService.approveLoan(1L, request)
        );
    }

    @Test
    public void shouldThrowResourceNotFoundWhenLoanIsNotFoundOnApproveLoan() {
        ApproveLoanRequest request = new ApproveLoanRequest(
                BigDecimal.valueOf(10),
                "Il le merite"
        );

        when(this.loanRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> this.loanService.approveLoan(1L, request)
        );
    }

    @Test
    public void shouldThrowValidationExceptionWhenLoanStatusIsDRAFTOnApproveLoan() {
        Loan loan = this.buildLoan(LoanStatus.DRAFT);
        ApproveLoanRequest request = new ApproveLoanRequest(
                BigDecimal.valueOf(10),
                "Il le merite"
        );

        when(this.loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        assertThrows(
                ValidationException.class,
                () -> this.loanService.approveLoan(1L, request)
        );
    }

    @Test
    public void shouldThrowValidationExceptionWhenLoanStatusIsAlreadyTreatedOnApproveLoan() {
        Loan loan = this.buildLoan(LoanStatus.APPROVED);
        ApproveLoanRequest request = new ApproveLoanRequest(
                BigDecimal.valueOf(10),
                "Il le merite"
        );

        when(this.loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        assertThrows(
                ValidationException.class,
                () -> this.loanService.approveLoan(1L, request)
        );
    }

    @Test
    public void shouldApproveLoanSuccessfully() {
        Loan loan = this.buildLoan(LoanStatus.PENDING);
        Loan loanSave = this.buildLoan(LoanStatus.APPROVED);
        ApproveLoanRequest request = new ApproveLoanRequest(
                BigDecimal.valueOf(10),
                "Il le merite"
        );

        TransactionResponse response = new TransactionResponse(
                1L,
                "TRANS1234567",
                "XOF",
                "DEPOSIT",
                loan.getAmount(),
                "SUCCESS",
                null,
                null,
                loan.getAccount().getNumeroAccount(),
                Instant.now().toString()
        );

        when(this.loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        when(this.transactionsService.createTransaction(any(TransactionRequest.class), isNull()))
                .thenReturn(response);

        doNothing().when(this.installmentService)
                .generateInstallment(loan);

        LoanBaseResponse oldValue = LoanBaseResponse.of(loan);
        this.loanService.approveLoan(1L, request);

        loanSave.setInterestRate(BigDecimal.valueOf(10));
        loanSave.setRemainingAmount(loan.getAmount());
        loanSave.setDisbursementDate(LocalDate.now());
        loanSave.setComments(request.comments());
        LoanBaseResponse newValue = LoanBaseResponse.of(loanSave);

        verify(this.transactionsService).createTransaction(any(TransactionRequest.class), isNull());
        verify(this.installmentService).generateInstallment(loan);
        assertEquals(oldValue, AuditContext.getOldValue());
        assertEquals(newValue, AuditContext.getNewValue());
    }

    private Loan buildLoan(LoanStatus status) {
        Account account = this.buildAccount();
        Customer customer = this.buildCustomer();

        return Loan.builder()
                .idLoan(1L)
                .account(account)
                .customer(customer)
                .loanStatus(status)
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
