package com.jiyuu.banking.controller;

import com.jiyuu.banking.config.JwtUtils;
import com.jiyuu.banking.dto.ApiResponse;
import com.jiyuu.banking.dto.CustomerRequest;
import com.jiyuu.banking.dto.CustomerResponse;
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
}
