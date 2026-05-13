package com.jiyuu.banking.controller;

import com.jiyuu.banking.dto.AuthRequest;
import com.jiyuu.banking.entity.User;
import com.jiyuu.banking.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody AuthRequest authRequest) {
        User userCreated = this.userService.register(authRequest.email(), authRequest.password());
        return new ResponseEntity<>(userCreated, HttpStatus.CREATED);
    }

    @PostMapping("/activate")
    public void activate(@RequestParam String code) {
        this.userService.verifyCode(code);
    }

    @PostMapping("/resend-code")
    public void resendCode(@RequestParam String email) {
        this.userService.resendCode(email);
    }
}
