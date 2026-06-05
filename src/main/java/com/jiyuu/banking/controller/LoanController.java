package com.jiyuu.banking.controller;

import com.jiyuu.banking.dto.*;
import com.jiyuu.banking.service.LoanInstallmentService;
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
    private final LoanInstallmentService installmentService;

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

    @GetMapping("/{idLoan}")
    public ResponseEntity<ApiResponse<LoanWithDocumentResponse>> getLoan(@PathVariable("idLoan") long id) {
        LoanWithDocumentResponse loan = this.loanService.getLoan(id);

        ApiResponse<LoanWithDocumentResponse> response = new ApiResponse<>(
                loan,
                "Le crédit a bien été récupéré",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<LoanBaseResponse>>> getLoans(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "idLoan") String sort,
            @RequestParam(name = "direction", defaultValue = "asc") String direction
    ) {
        PagedResponse<LoanBaseResponse> loans = this.loanService.AllLoans(page, size, sort, direction);

        ApiResponse<PagedResponse<LoanBaseResponse>> response = new ApiResponse<>(
                loans,
                "Les crédits ont bien été récupérés",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{idLoan}/approve")
    public ResponseEntity<ApiResponse<?>> approve(
            @PathVariable(name = "idLoan") long id,
            @Valid @RequestBody ApproveLoanRequest request
    ) {
        this.loanService.approveLoan(id, request);

        ApiResponse<?> response = new ApiResponse<>(
                null,
                "Le crédit a été approuvé.",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{idLoan}/reject")
    public ResponseEntity<ApiResponse<?>> reject(@PathVariable(name = "idLoan") long id) {
        this.loanService.rejectLoan(id);

        ApiResponse<?> response = new ApiResponse<>(
                null,
                "Le crédit a été rejeté.",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{idLoan}/schedule")
    public ResponseEntity<ApiResponse<PagedResponse<InstallmentResponse>>> scheduleInstallment(
            @PathVariable("idLoan") long id,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "installmentNumber") String sort,
            @RequestParam(name = "direction", defaultValue = "asc") String direction
    ) {
        PagedResponse<InstallmentResponse> installments = this.installmentService.scheduleInstallment(id, page, size, sort, direction);

        ApiResponse<PagedResponse<InstallmentResponse>> response = new ApiResponse<>(
                installments,
                "Les crédits ont bien été récupérés",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{idLoan}/repay")
    public ResponseEntity<ApiResponse<?>> repay(@PathVariable(name = "idLoan") long id) {
        this.loanService.earlyRepayment(id);

        ApiResponse<?> response = new ApiResponse<>(
                null,
                "Remboursement anticipé effectué.",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
