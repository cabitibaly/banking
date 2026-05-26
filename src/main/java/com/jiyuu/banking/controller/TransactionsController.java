package com.jiyuu.banking.controller;

import com.jiyuu.banking.dto.ApiResponse;
import com.jiyuu.banking.dto.TransactionRequest;
import com.jiyuu.banking.dto.TransactionResponse;
import com.jiyuu.banking.service.TransactionsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@AllArgsConstructor
@RequestMapping("/transactions")
public class TransactionsController {
    private final TransactionsService transactionsService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> deposit(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse transactionResponse = this.transactionsService.createTransaction(request);

        ApiResponse<TransactionResponse> response = new ApiResponse<>(
                transactionResponse,
                "Création de transaction réussie",
                HttpStatus.CREATED.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{ref}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(@PathVariable("ref") String ref) {
        TransactionResponse transactionResponse = this.transactionsService.getTransactionByRef(ref);

        ApiResponse<TransactionResponse> response = new ApiResponse<>(
                transactionResponse,
                "Récupération de transaction réussie",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{ref}/reverse")
    public ResponseEntity<ApiResponse<TransactionResponse>> reverseTransaction(@PathVariable("ref") String ref) {
        TransactionResponse transactionResponse = this.transactionsService.reverseTransaction(ref);

        ApiResponse<TransactionResponse> response = new ApiResponse<>(
                transactionResponse,
                "Annulation de transaction réussie",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

