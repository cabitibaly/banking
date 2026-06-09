package com.jiyuu.banking.controller;

import com.jiyuu.banking.dto.ActivateRequest;
import com.jiyuu.banking.dto.ApiResponse;
import com.jiyuu.banking.dto.CardRequest;
import com.jiyuu.banking.dto.CardResponse;
import com.jiyuu.banking.enums.CardState;
import com.jiyuu.banking.service.CardService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/card")
@AllArgsConstructor
public class CardController {
    private final CardService cardService;

    @PostMapping
    public ResponseEntity<ApiResponse<CardResponse>> createCard(@Valid @RequestBody CardRequest request) {
        CardResponse cardResponse = this.cardService.createCard(request);

        ApiResponse<CardResponse> response = new ApiResponse<CardResponse>(
                cardResponse,
                "La carte a été créée avec succès",
                HttpStatus.CREATED.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{cardNumber}")
    public ResponseEntity<ApiResponse<?>> activateCard(
            @PathVariable(name = "cardNumber") String cardNumber,
            @Valid @RequestBody ActivateRequest request
    ) {
        this.cardService.activateCard(cardNumber, request.pin());

        ApiResponse<?> response = new ApiResponse<>(
                null,
                "La carte a été activée avec succès",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{cardNumber}/state")
    public ResponseEntity<ApiResponse<?>> changeState(
            @PathVariable(name = "cardNumber") String cardNumber,
            @RequestParam(name = "state") CardState state
    ) {
        this.cardService.changeState(cardNumber, state);

        ApiResponse<?> response = new ApiResponse<>(
                null,
                "Le statut de la carte a été changé avec succès",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
