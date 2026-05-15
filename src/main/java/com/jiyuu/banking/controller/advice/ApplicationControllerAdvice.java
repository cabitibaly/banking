package com.jiyuu.banking.controller.advice;

import com.jiyuu.banking.exception.*;
import io.jsonwebtoken.security.MalformedKeyException;
import jakarta.persistence.EntityExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.SignatureException;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ApplicationControllerAdvice {

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(value = EntityExistsException.class)
    public @ResponseBody ProblemDetail entityExistsException(final EntityExistsException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setProperty("erreur", "L'utilisateur existe déjà");
        return problemDetail;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = UsernameNotFoundException.class)
    public @ResponseBody ProblemDetail usernameNotFoundException(final UsernameNotFoundException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "L'utilisateur demandé n'est pas disponible");
        problemDetail.setProperty("erreur", "Verifiez votre email");
        return problemDetail;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = BadCredentialsException.class)
    public @ResponseBody ProblemDetail badCredentialsException(final BadCredentialsException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Identifiants incorrects");
        problemDetail.setProperty("erreur", "Nous n'avons pas pu vous authentifier");
        return problemDetail;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = {SignatureException.class, MalformedKeyException.class})
    public @ResponseBody ProblemDetail signatureException(final Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Votre jeton n'est pas valide, veuillez vous reconnecter");
        problemDetail.setProperty("erreur", "Nous n'avons pas pu vous authentifier");
        return problemDetail;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = AccessDeniedException.class)
    public @ResponseBody ProblemDetail accessDeniedException(final AccessDeniedException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Vos droits ne vous permettent pas d'accéder à cette ressource");
        problemDetail.setProperty("erreur", "Vous n'avez pas les autorisations requises");
        return problemDetail;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = AccountBlockedException.class)
    public @ResponseBody ProblemDetail accountBlockedException(final AccountBlockedException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Votre compte est bloqué");
        problemDetail.setProperty("erreur", "Votre compte est bloqué");
        return problemDetail;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = AuthenticationException.class)
    public @ResponseBody ProblemDetail authenticationException(final AuthenticationException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Identifiants incorrects");
        problemDetail.setProperty("erreur", "Nous n'avons pas pu vous authentifier");
        return problemDetail;
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(value = InsufficientFundsException.class)
    public @ResponseBody ProblemDetail insufficientFundsException(final InsufficientFundsException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
        problemDetail.setProperty("erreur", "Vous n'avez pas assez d'argent");
        return problemDetail;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = ResourceNotFoundException.class)
    public @ResponseBody ProblemDetail resourceNotFoundException(final ResourceNotFoundException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setProperty("erreur", "Désolé mais la demande n'a pas abouti");
        return problemDetail;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = ValidationException.class)
    public @ResponseBody ProblemDetail validationException(final ValidationException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setProperty("erreur", "Le code est invalide");
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage
                ));
    }
}
