package com.jiyuu.banking.controller;

import com.jiyuu.banking.dto.AccountRequest;
import com.jiyuu.banking.dto.ApiResponse;
import com.jiyuu.banking.service.AccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
