package com.jiyuu.banking.controller;

import com.jiyuu.banking.dto.ApiResponse;
import com.jiyuu.banking.dto.DocumentRequest;
import com.jiyuu.banking.dto.LoanBaseResponse;
import com.jiyuu.banking.dto.LoanRequest;
import com.jiyuu.banking.service.LoanService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/loans")
@AllArgsConstructor
public class LoanController {
    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<ApiResponse<LoanBaseResponse>> createLoan(@Valid @RequestBody LoanRequest request) {
        LoanBaseResponse loan = this.loanService.createLoan(request);

        ApiResponse<LoanBaseResponse> response = new ApiResponse<>(
                loan,
                "Le crédit a bien été créé, veuillez ajouter les documents",
                HttpStatus.CREATED.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{idLoan}/documents")
    public ResponseEntity<ApiResponse<?>> addDocument(
            @PathVariable("idLoan") long id,
            @Valid @RequestBody DocumentRequest request
    ) {
        this.loanService.addDocument(id, request);

        ApiResponse<?> response = new ApiResponse<>(
                null,
                "Le document a bien été ajouté",
                HttpStatus.CREATED.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
