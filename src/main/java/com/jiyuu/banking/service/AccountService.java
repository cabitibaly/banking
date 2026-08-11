package com.jiyuu.banking.service;

import com.jiyuu.banking.audit.annotation.Auditable;
import com.jiyuu.banking.audit.context.AuditContext;
import com.jiyuu.banking.dto.*;
import com.jiyuu.banking.entity.*;
import com.jiyuu.banking.enums.AccountStatus;
import com.jiyuu.banking.enums.AccountType;
import com.jiyuu.banking.enums.Currency;
import com.jiyuu.banking.exception.AccessDeniedException;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.AccountMembershipRepository;
import com.jiyuu.banking.repository.AccountRepository;
import com.jiyuu.banking.repository.CustomerRepository;
import com.jiyuu.banking.repository.TransactionsRepository;
import com.jiyuu.banking.repository.specification.AccountSpecification;
import com.jiyuu.banking.repository.specification.TransactionsSpecification;
import com.jiyuu.banking.utils.AccountNumberGenerator;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMembershipRepository accountMembershipRepository;
    private final TransactionsRepository transactionsRepository;
    private final CustomerRepository customerRepository;
    private final SpringTemplateEngine springTemplateEngine;
    private final NotificationSender notificationSender;
    private final TransactionsService transactionsService;
    private final AccountNumberGenerator accountNumberGenerator;
    private final CardService cardService;

    @Value("${max-decouvert}")
    private long MAX_DECOUVERT;

    public AccountService(
            AccountRepository accountRepository,
            AccountMembershipRepository accountMembershipRepository,
            TransactionsRepository transactionsRepository,
            CustomerRepository customerRepository,
            TransactionsService transactionsService,
            SpringTemplateEngine springTemplateEngine,
            NotificationSender notificationSender,
            AccountNumberGenerator accountNumberGenerator,
            CardService cardService
    ) {
        this.accountRepository = accountRepository;
        this.accountMembershipRepository = accountMembershipRepository;
        this.transactionsRepository = transactionsRepository;
        this.customerRepository = customerRepository;
        this.transactionsService = transactionsService;
        this.springTemplateEngine = springTemplateEngine;
        this.notificationSender = notificationSender;
        this.accountNumberGenerator = accountNumberGenerator;
        this.cardService = cardService;
    }

    @Auditable(action = "CREATE", entity = "ACCOUNT, ACCOUNTMEMBERSHIP")
    public void createAccount(AccountRequest accountRequest) {
        Customer customer = this.customerRepository.findById(accountRequest.idCustomer())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        String accountNumber = accountNumberGenerator.generate();
        Account account = Account.builder()
                .accountType(AccountType.valueOf(accountRequest.type()))
                .accountStatus(AccountStatus.PENDING)
                .numeroAccount(accountNumber)
                .currencyAccount(Currency.valueOf(accountRequest.currency()))
                .soldeAccount(BigDecimal.ZERO)
                .decouvert(accountRequest.decouvert())
                .build();

        account = this.accountRepository.save(account);

        AccountMembership accountMembership = AccountMembership.builder()
                .isPrimary(true)
                .account(account)
                .customer(customer)
                .build();

        this.accountMembershipRepository.save(accountMembership);

        AccountResponse response = AccountResponse.of(account);

        AuditContext.setNewValue(response);
    }

    @Auditable(action = "UPDATE", entity = "ACCOUNT")
    public void changeAccountStatus(long idAccount, String status) {
        Account account = this.accountRepository.findById(idAccount)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        AccountResponse oldValue = AccountResponse.of(account);
        AuditContext.setOldValue(oldValue);

        account.setAccountStatus(AccountStatus.valueOf(status));
        account = this.accountRepository.save(account);

        AccountResponse newValue = AccountResponse.of(account);
        AuditContext.setNewValue(newValue);
    }

    @Auditable(action = "UPDATE", entity = "ACCOUNT")
    public void changeDecouvert(long idAccount, BigDecimal decouvert) {
        Account account = this.accountRepository.findById(idAccount)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if(decouvert.compareTo(BigDecimal.valueOf(MAX_DECOUVERT)) > 0) {
            throw new ValidationException("Le montant de découverte ne peut pas dépasser le maximum de " + MAX_DECOUVERT);
        }

        AccountResponse oldValue = AccountResponse.of(account);
        AuditContext.setOldValue(oldValue);

        account.setDecouvert(decouvert);
        account = this.accountRepository.save(account);

        AccountResponse newValue = AccountResponse.of(account);
        AuditContext.setNewValue(newValue);
    }

    @Auditable(action = "READ", entity = "ACCOUNT")
    public PagedResponse<AccountResponse> getAccounts(AccountSearchCriteria accountSearchCriteria, int page, int size, String sortBy, String direction) {
        Specification<Account> spec = AccountSpecification.withFiler(accountSearchCriteria);

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AccountResponse> accounts = this.accountRepository
                .findAll(spec, pageable)
                .map(AccountResponse::of);

        return PagedResponse.of(accounts);
    }

    @Auditable(action = "READ", entity = "ACCOUNT")
    public AccountResponse getAccount(long id) {
        Account account = this.accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        return AccountResponse.of(account);
    }

    public Account getAccountEntity(long id) {
        return this.accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    @Auditable(action = "READ", entity = "TRANSACTION")
    public PagedResponse<TransactionResponse> getMyTransactions(
            Long idAccount,
            String start,
            String end,
            String cardNumber,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        LocalDateTime startDate = start == null || start.isEmpty()  ? null : LocalDateTime.parse(start);
        LocalDateTime endDate =  end == null || end.isEmpty() ? null : LocalDateTime.parse(end);
        Specification<Transactions> spec = TransactionsSpecification.withFiler(idAccount, startDate, endDate, cardNumber);

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TransactionResponse> transactions = this.transactionsRepository
                .findAll(spec, pageable)
                .map(this.transactionsService::toResponse);

        return PagedResponse.of(transactions);
    }

    public void statement(long idAccount, String start, String end) throws MessagingException {
        LocalDateTime startDate = start == null || start.isEmpty()  ? null : LocalDateTime.parse(start);
        LocalDateTime endDate =  end == null || end.isEmpty() ? null : LocalDateTime.parse(end);
        Specification<Transactions> spec = TransactionsSpecification.withFiler(idAccount, startDate, endDate, null);

        List<TransactionResponse> transactions = this.transactionsRepository
                .findAll(spec)
                .stream()
                .map(this.transactionsService::toResponse)
                .toList();

        Context context = new Context();
        context.setVariable("transactions", transactions);
        context.setVariable("start", startDate.toString());
        context.setVariable("end", endDate.toString());
        String html = springTemplateEngine.process("transactions-report", context);

        Document document = Jsoup.parse(html);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        String xhtml = document.html();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(xhtml, "http://localhost:8080/");
        renderer.layout();
        renderer.createPDF(stream);
        byte[] pdf = stream.toByteArray();
        this.notificationSender.sendTransactionReport("test@example.com", pdf);
    }

    public void collectFee() {
        log.info("Application des frais");
        List<Account> accounts = this.accountRepository.findAll();

        for (Account account : accounts) {
            try {
                if (account.getAccountStatus() == AccountStatus.CLOSED) {
                    continue;
                }

                TransactionRequest request = TransactionRequest.builder()
                        .amount(BigDecimal.valueOf(500))
                        .currency("XOF")
                        .type("FEE")
                        .source(account.getNumeroAccount())
                        .build();

                this.transactionsService.createTransaction(request, null);
            } catch (Exception e) {
                log.error("Erreur application des frais {} : {}", account.getNumeroAccount(), e.getMessage());
            }
        }

        log.info("Terminé");
    }

    public TransactionResponse debitAccountWithCard(DebitRequest request) {
        Card card = this.cardService
                .validCard(
                        request.cardNumber(),
                        request.pin(),
                        request.cvv(),
                        request.expirationDate()
                );

        TransactionRequest transactionRequest = TransactionRequest.builder()
                .amount(request.amount())
                .currency(request.currency())
                .type("WITHDRAW")
                .source(card.getAccount().getNumeroAccount())
                .build();

        return this.transactionsService.createTransaction(transactionRequest, card);
    }
}
