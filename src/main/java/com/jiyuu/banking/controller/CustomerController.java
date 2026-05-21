package com.jiyuu.banking.controller;

import com.jiyuu.banking.config.JwtUtils;
import com.jiyuu.banking.dto.*;
import com.jiyuu.banking.entity.User;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.service.CustomerService;
import com.jiyuu.banking.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@AllArgsConstructor
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService customerService;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody CustomerRequest customerRequest) {
        if (authHeader == null) {
            throw new ValidationException("Impossible de valider votre requête");
        }

        String token = authHeader.replace("Bearer ", "");
        String username = this.jwtUtils.extractUsername(token);
        User user = (User) this.userService.loadUserByUsername(username);

        CustomerResponse customerResponse = this.customerService.createCustomer(user, customerRequest);
        ApiResponse<CustomerResponse> response = new ApiResponse<>(
                customerResponse,
                "Création de compte réussie",
                HttpStatus.CREATED.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> getCustomers(
            @RequestParam(name = "nom", required = false) String nom,
            @RequestParam(name = "telephone", required = false) String telephone,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "numero", required = false) String numero,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "idCustomer") String soortBy,
            @RequestParam(name = "orderBy", defaultValue = "asc") String orderBy
    ) {
        CustomerSearchCriteria customerSearchCriteria = new CustomerSearchCriteria(
                nom,
                telephone,
                status,
                numero
        );

        PagedResponse<CustomerResponse> customers = this.customerService
                .getCustomers(customerSearchCriteria,page, size, soortBy, orderBy);

        ApiResponse<PagedResponse<CustomerResponse>> response = new ApiResponse<>(
                customers,
                "Récupération de tous les clients réussie",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{idCustomer}")
    public ResponseEntity<ApiResponse<CustomerWithKycDocumentResponse>> getCustomer(@PathVariable("idCustomer") long id) {
        CustomerWithKycDocumentResponse customerResponse = this.customerService.getCustomer(id);
        ApiResponse<CustomerWithKycDocumentResponse> response = new ApiResponse<>(
                customerResponse,
                "Récupération d'un client réussie",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{idCustomer}/kyc")
    public ResponseEntity<ApiResponse<?>> addKycDocument(@PathVariable("idCustomer") long id, @Valid @RequestBody KycDocumentRequest kycDocumentRequest) {
        this.customerService.addKycDocument(id, kycDocumentRequest);
        ApiResponse<?> response = new ApiResponse<>(
                null,
                "Ajout du document réussi",
                HttpStatus.CREATED.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{idCustomer}/{status}")
    public ResponseEntity<ApiResponse<?>> updateCustomerStatus(@PathVariable("idCustomer") long id, @PathVariable("status") String status) {
        this.customerService.changeCustomerStatus(id, status);
        ApiResponse<?> response = new ApiResponse<>(
                null,
                "Mise à jour du statut du client réussi",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{idCustomer}")
    public ResponseEntity<ApiResponse<?>> updateCustomer(
            @PathVariable("idCustomer") long id,
            @Valid @RequestBody CustomerRequest customerRequest
    ) {
        this.customerService.updateCustomer(id, customerRequest);
        ApiResponse<?> response = new ApiResponse<>(
                null,
                "Mise à jour du client réussi",
                HttpStatus.OK.value(),
                Instant.now()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{idCustomer}")
    public ResponseEntity<ApiResponse<?>> deleteCustomer(@PathVariable("idCustomer") long id) {
        this.customerService.deleteCustomer(id);
        ApiResponse<?> response = new ApiResponse<>(
                null,
                "Suppression du client réussi",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{idCustomer}/kyc/{idKycDocument}")
    public ResponseEntity<ApiResponse<?>> updateKycDocument(
            @PathVariable("idCustomer") long idCustomer,
            @PathVariable("idKycDocument") long idKycDocument,
            @RequestParam(name = "status", required = true) String status
    ) {
        this.customerService.updateKycDocument(idKycDocument, idCustomer, status);
        ApiResponse<?> response = new ApiResponse<>(
                null,
                "Mise à jour du document réussi",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{idCustomer}/kyc/{idKycDocument}")
    public ResponseEntity<ApiResponse<?>> deleteKycDocument(
            @PathVariable("idCustomer") long idCustomer,
            @PathVariable("idKycDocument") long idKycDocument
    ) {
        this.customerService.deleteKycDocument(idKycDocument, idCustomer);
        ApiResponse<?> response = new ApiResponse<>(
                null,
                "Suppression du document réussi",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
