package com.jiyuu.banking.service;

import com.jiyuu.banking.audit.annotation.Auditable;
import com.jiyuu.banking.audit.context.AuditContext;
import com.jiyuu.banking.dto.*;
import com.jiyuu.banking.entity.*;
import com.jiyuu.banking.enums.DocumentType;
import com.jiyuu.banking.enums.InstallmentStatus;
import com.jiyuu.banking.enums.LoanStatus;
import com.jiyuu.banking.enums.LoanType;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.AccountRepository;
import com.jiyuu.banking.repository.CustomerRepository;
import com.jiyuu.banking.repository.LoanDocumentRepository;
import com.jiyuu.banking.repository.LoanRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final LoanDocumentRepository loanDocumentRepository;
    private final LoanInstallmentService installmentService;
    private final TransactionsService transactionsService;

    @Auditable(action = "CREATE", entity = "Loan")
    public LoanBaseResponse createLoan(LoanRequest request) {
        if (request.duration() == 0) {
            throw new ValidationException("La durée doit être supérieure à 1");
        }

        if (request.amount().compareTo(BigDecimal.ZERO) == 0) {
            throw new ValidationException("Le montant doit être supérieur à 0");
        }

        Customer customer = this.customerRepository.findBytelephoneCustomer(request.telephone())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Account account = this.accountRepository.findBynumeroAccount(request.accountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Loan loan = Loan.builder()
                .customer(customer)
                .account(account)
                .amount(request.amount())
                .reason(request.reason())
                .duration(request.duration())
                .loanType(LoanType.valueOf(request.type()))
                .loanStatus(LoanStatus.DRAFT)
                .build();

        loan = this.loanRepository.save(loan);

        LoanBaseResponse response = LoanBaseResponse.of(loan);
        AuditContext.setNewValue(response);
        return response;
    }

    @Auditable(action = "CREATE", entity = "LoanDocument")
    public void addDocument(long idLoan, DocumentRequest request) {
        Loan loan = this.loanRepository.findById(idLoan)
                .orElseThrow(() -> new ResourceNotFoundException("Ce crédit n'existe pas"));

        Set <LoanStatus> statuses = EnumSet.of(
                LoanStatus.APPROVED,
                LoanStatus.REJECTED,
                LoanStatus.ACTIVE,
                LoanStatus.CLOSED,
                LoanStatus.UNDER_REVIEW
        );

        if(statuses.contains(loan.getLoanStatus())) {
            throw new ValidationException("Ce crédit a dejà été traité ou est en cours de traitement");
        }

        loan.getDocuments().forEach(loanDocument -> {
            if (loanDocument.getDocumentType().toString().equals(request.kycType())) {
                throw new ValidationException(
                        String.format("Le document de type %s a déjà été ajouté", request.kycType())
                );
            }
        });

        LoanDocument loanDocument = LoanDocument.builder()
                .loan(loan)
                .documentType(DocumentType.valueOf(request.kycType()))
                .urlDocument(request.fileUrl())
                .build();

        if(loan.getLoanStatus() == LoanStatus.DRAFT) {
            loan.setLoanStatus(LoanStatus.PENDING);
        }


        this.loanDocumentRepository.save(loanDocument);
    }

    @Auditable(action = "READ", entity = "Loan")
    public PagedResponse<LoanBaseResponse> AllLoans(int page, int size, String soortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(soortBy).descending()
                : Sort.by(soortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LoanBaseResponse> loans = this.loanRepository
                .findAll(pageable)
                .map(LoanBaseResponse::of);

        return PagedResponse.of(loans);
    }

    @Auditable(action = "READ", entity = "Loan")
    public LoanWithDocumentResponse getLoan(long id) {
        Loan loan = this.loanRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Ce crédit n'existe pas")
        );

        return LoanWithDocumentResponse.of(loan);
    }

    @Auditable(action = "UPDATE", entity = "Loan")
    public void approveLoan(long id, ApproveLoanRequest request) {

        if(request.interest().compareTo(BigDecimal.ZERO) == 0) {
            throw new ValidationException("L'intérêt doit être supérieur à 0");
        }

        Loan loan = this.loanRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Ce crédit n'existe pas")
        );

        if (loan.getLoanStatus() == LoanStatus.DRAFT) {
            throw new ValidationException("Impossible de traiter ce crédit");
        }

        Set<LoanStatus> statuses = EnumSet.of(
                LoanStatus.APPROVED,
                LoanStatus.REJECTED,
                LoanStatus.ACTIVE,
                LoanStatus.CLOSED,
                LoanStatus.UNDER_REVIEW
        );

        if(statuses.contains(loan.getLoanStatus())) {
            throw new ValidationException("Ce crédit a dejà été traité.");
        }

        TransactionRequest transactionRequest = TransactionRequest.builder()
                .amount(loan.getAmount())
                .currency("XOF")
                .type("DEPOSIT")
                .target(loan.getAccount().getNumeroAccount())
                .build();

        this.transactionsService.createTransaction(transactionRequest, null);

        LoanBaseResponse oldValue = LoanBaseResponse.of(loan);
        AuditContext.setOldValue(oldValue);

        loan.setLoanStatus(LoanStatus.APPROVED);
        loan.setRemainingAmount(loan.getAmount());
        loan.setDisbursementDate(LocalDate.now());
        loan.setInterestRate(request.interest());
        loan.setComments(request.comments());

        LoanBaseResponse newValue = LoanBaseResponse.of(loan);
        AuditContext.setNewValue(newValue);

        this.installmentService.generateInstallment(loan);
    }

    @Auditable(action = "UPDATE", entity = "Loan")
    public void rejectLoan(long id) {
        Loan loan = this.loanRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Ce crédit n'existe pas")
        );

        if (loan.getLoanStatus() == LoanStatus.DRAFT) {
            throw new ValidationException("Impossible de traiter ce crédit");
        }

        Set<LoanStatus> statuses = EnumSet.of(
                LoanStatus.APPROVED,
                LoanStatus.REJECTED,
                LoanStatus.ACTIVE,
                LoanStatus.CLOSED,
                LoanStatus.UNDER_REVIEW
        );

        if(statuses.contains(loan.getLoanStatus())) {
            throw new ValidationException("Ce crédit a dejà été traité.");
        }

        LoanBaseResponse oldValue = LoanBaseResponse.of(loan);
        AuditContext.setOldValue(oldValue);

        loan.setLoanStatus(LoanStatus.REJECTED);
        loan = this.loanRepository.save(loan);

        LoanBaseResponse newValue = LoanBaseResponse.of(loan);
        AuditContext.setNewValue(newValue);
    }

    @Auditable(action = "UPDATE", entity = "Loan")
    public void earlyRepayment(long id) {
        Loan loan = this.loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ce crédit n'existe pas"));

        if (loan.getLoanStatus() == LoanStatus.DRAFT) {
            throw new ValidationException("Impossible de traiter ce crédit");
        }

        Set<LoanStatus> statuses = EnumSet.of(
                LoanStatus.REJECTED,
                LoanStatus.CLOSED,
                LoanStatus.UNDER_REVIEW
        );

        if(statuses.contains(loan.getLoanStatus())) {
            throw new ValidationException("Ce crédit a dejà été traité ou est en cours de traitement.");
        }

        BigDecimal remainingAmount = loan.getRemainingAmount();
        if (remainingAmount.compareTo(BigDecimal.ZERO) == 0 && loan.getLoanStatus() == LoanStatus.CLOSED) {
            throw new ValidationException("Ce crédit a déjà été payé");
        }

        TransactionRequest request = TransactionRequest.builder()
                .amount(remainingAmount)
                .currency("XOF")
                .type("REPAYMENT")
                .source(loan.getAccount().getNumeroAccount())
                .build();

        Transactions transactions = this.transactionsService.createTransactionEntity(request, null);
        this.installmentService.repayment(loan, transactions);

        loan.setRemainingAmount(BigDecimal.ZERO);
        loan.setLoanStatus(LoanStatus.CLOSED);
        this.loanRepository.save(loan);
    }

    public void processMonthlyInstallment() {
        List<LoanInstallment> installments = this.installmentService.PendingInstallment();

        for (LoanInstallment installment : installments) {
            try {
                this.collectInstallment(installment);
            } catch (Exception e) {
                log.error("Erreur mensualité {} : {}", installment.getIdInstallment(), e.getMessage());
            }
        }
    }

    public void collectInstallment(LoanInstallment installment) {
        try {
            TransactionRequest request = TransactionRequest.builder()
                    .amount(installment.getTotalAmount())
                    .currency("XOF")
                    .type("INTEREST")
                    .source(installment.getLoan().getAccount().getNumeroAccount())
                    .build();

            Transactions tx = transactionsService.createTransactionEntity(request, null);
            installment.setTransaction(tx);

            installment.setInstallmentStatus(InstallmentStatus.PAID);
            Loan loan = installment.getLoan();

            BigDecimal newRemaining = loan.getRemainingAmount()
                    .subtract(installment.getTotalAmount())
                    .max(BigDecimal.ZERO);

            loan.setRemainingAmount(newRemaining);

            boolean allPaid = loan.getInstallments()
                    .stream()
                    .allMatch(i -> i.getInstallmentStatus() == InstallmentStatus.PAID);

            if (allPaid) {
                loan.setLoanStatus(LoanStatus.CLOSED);
            }
        } catch (Exception e) {
            this.installmentService.markInstallmentAsOverdue(installment.getIdInstallment());
            throw e;
        }
    }
}
