package com.jiyuu.banking.controller;

import com.jiyuu.banking.dto.*;
import com.jiyuu.banking.service.AccountService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createAccount(@Valid @RequestBody AccountRequest accountRequest) {
        this.accountService.createAccount(accountRequest);

        ApiResponse<?> response = new ApiResponse<>(
                null,
                "Le compte a bien été créé",
                HttpStatus.CREATED.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{idAccount}/decouvert")
    public ResponseEntity<ApiResponse<?>> changeDecouvert(@PathVariable long idAccount, @RequestBody Map<String, BigDecimal> accountReq) {
        this.accountService.changeDecouvert(idAccount, accountReq.get("decouvert"));

        ApiResponse<?> response = new ApiResponse<>(
                null,
                "Le compte a bien été modifié",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{idAccount}/status")
    public ResponseEntity<ApiResponse<?>> changeAccountStatus(@PathVariable long idAccount, @RequestBody Map<String, String> accountReq) {
        this.accountService.changeAccountStatus(idAccount, accountReq.get("status"));

        ApiResponse<?> response = new ApiResponse<>(
                null,
                "Le compte a bien été modifié",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/membership/{idAccount}/{idCustomer}")
    public ResponseEntity<ApiResponse<?>> deleteMember(@PathVariable long idAccount, @PathVariable long idCustomer) {
        this.accountService.deleteMember(idAccount, idCustomer);

        ApiResponse<?> response = new ApiResponse<>(
                null,
                "Le compte a bien été modifié",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AccountResponse>>> getAccounts(
            @RequestParam(name = "numero", required = false) String numero,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "sort", required = false, defaultValue = "idAccount") String sort,
            @RequestParam(name = "direction", required = false, defaultValue = "asc") String direction
    ) {
        AccountSearchCriteria accountSearchCriteria = new AccountSearchCriteria(numero, status, type);

        PagedResponse<AccountResponse> accounts = this.accountService.getAccounts(accountSearchCriteria, page, size, sort, direction);

        ApiResponse<PagedResponse<AccountResponse>> response = new ApiResponse<>(
                accounts,
                "La liste de tous les comptes",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{idAccount}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(@PathVariable long idAccount) {
        AccountResponse account = this.accountService.getAccount(idAccount);

        ApiResponse<AccountResponse> response = new ApiResponse<>(
                account,
                "Le compte a été trouvé",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{idAccount}/transactions")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> getAccountTransactions(
            @PathVariable long idAccount,
            @RequestParam(name = "start", required = false) String start,
            @RequestParam(name = "end", required = false) String end,
            @RequestParam(name = "cardNumber", required = false) String cardNumber,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "sort", required = false, defaultValue = "idTransaction") String sort,
            @RequestParam(name = "direction", required = false, defaultValue = "asc") String direction
    ) {
        PagedResponse<TransactionResponse> transactions = this.accountService
                .getMyTransactions(idAccount, start, end, cardNumber, page, size, sort, direction);

        ApiResponse<PagedResponse<TransactionResponse>> response = new ApiResponse<>(
                transactions,
                "La liste de toutes les transactions",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{idAccount}/statement")
    public ResponseEntity<ApiResponse<?>> statement(
            @PathVariable long idAccount,
            @RequestParam(name = "start", required = false) String start,
            @RequestParam(name = "end", required = false) String end
    ) throws MessagingException {
        this.accountService.statement(idAccount, start, end);

        ApiResponse<Void> response = new ApiResponse<>(
                null,
                "La liste de toutes les transactions",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/debit")
    public ResponseEntity<ApiResponse<?>> debitAccount(@Valid @RequestBody DebitRequest request) {
        TransactionResponse transactionResponse = this.accountService.debitAccountWithCard(request);

        ApiResponse<?> response = new ApiResponse<>(
                transactionResponse,
                "La transaction a bien été effectuée",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
