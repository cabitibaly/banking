package com.jiyuu.banking.service;

import com.jiyuu.banking.entity.EmailVerification;
import com.jiyuu.banking.entity.Role;
import com.jiyuu.banking.entity.User;
import com.jiyuu.banking.enums.TypeOfRole;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepository
                .findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User register(String email, String password) {
        Optional<User> userOptional = this.userRepository.findByEmail(email);

        if(userOptional.isPresent()) {
            throw new EntityExistsException("Email déjà utilisé");
        }

        Role role = new Role();
        role.setLabel(TypeOfRole.CUSTOMER);

        String hashedPassword = this.passwordEncoder.encode(password);

        User user = new User();
        user.setEnabled(false);
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setRole(role);

        user = this.userRepository.save(user);
        this.emailVerificationService.createNewCode(user);

        return user;
    }

    public void resendCode(String email) {
        this.emailVerificationService.resendCode(email);
    }

    public void verifyCode(String code) {
        EmailVerification emailVerification = this.emailVerificationService.readByCode(code);

        if(emailVerification.isExpired()) {
            throw new ValidationException("Le code fourni a expiré");
        }

        User user = this.userRepository
                .findByEmail(emailVerification.getUser().getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setEnabled(true);
        this.userRepository.save(user);
    }
}
