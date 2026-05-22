package com.jiyuu.banking.config;

import com.jiyuu.banking.filter.JwtFilter;
import com.jiyuu.banking.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfigurationSecurityApplication {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtFilter jwtFilter;
    private final UserService userService;

    public ConfigurationSecurityApplication(BCryptPasswordEncoder bCryptPasswordEncoder, JwtFilter jwtFilter, UserService userService) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtFilter = jwtFilter;
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                                        .requestMatchers(HttpMethod.POST, "/auth/resend-code").permitAll()
                                        .requestMatchers(HttpMethod.POST, "/auth/activate").permitAll()
                                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                                        .requestMatchers(HttpMethod.POST, "/auth/refresh-token").permitAll()
                                        .requestMatchers(HttpMethod.POST, "/auth/forgot-password").permitAll()
                                        .requestMatchers(HttpMethod.PATCH, "/auth/reset-password").permitAll()
                                        .requestMatchers(HttpMethod.POST, "/auth/logout").authenticated()
                                        .requestMatchers(HttpMethod.PATCH, "/auth/update-email").hasAnyAuthority("ADMINISTRATOR_UPDATE", "AGENT_UPDATE", "CUSTOMER_UPDATE")
                                        .requestMatchers(HttpMethod.GET, "/auth/test").hasAuthority("CUSTOMER_READ")
                                        .requestMatchers(HttpMethod.POST, "/customers").hasAuthority("CUSTOMER_CREATE")
                                        .requestMatchers(HttpMethod.GET, "/customers").hasAnyAuthority("ADMINISTRATOR_READ", "AGENT_READ")
                                        .requestMatchers(HttpMethod.GET, "/customers/{idCustomer}").hasAnyAuthority("ADMINISTRATOR_READ", "AGENT_READ", "CUSTOMER_READ")
                                        .requestMatchers(HttpMethod.PATCH, "/customers/{idCustomer}/{status}").hasAnyAuthority("ADMINISTRATOR_UPDATE", "AGENT_UPDATE")
                                        .requestMatchers(HttpMethod.PATCH, "/customers/{idCustomer}").hasAnyAuthority("ADMINISTRATOR_UPDATE", "AGENT_UPDATE", "CUSTOMER_UPDATE")
                                        .requestMatchers(HttpMethod.DELETE, "/customers/{idCustomer}").hasAnyAuthority("ADMINISTRATOR_DELETE", "AGENT_DELETE")
                                        .requestMatchers(HttpMethod.POST, "/customers/{idCustomer}/kyc").hasAuthority("CUSTOMER_CREATE")
                                        .requestMatchers(HttpMethod.PATCH, "/customers/{idCustomer}/kyc/{idKycDocument}").hasAnyAuthority("ADMINISTRATOR_UPDATE", "AGENT_UPDATE")
                                        .requestMatchers(HttpMethod.DELETE, "/customers/{idCustomer}/kyc/{idKycDocument}").hasAnyAuthority("ADMINISTRATOR_DELETE", "AGENT_DELETE", "CUSTOMER_DELETE")
                                        .requestMatchers(HttpMethod.POST, "/accounts").hasAnyAuthority("ADMINISTRATOR_CREATE", "AGENT_CREATE")
                                        .requestMatchers(HttpMethod.PATCH, "/accounts/{idAccount}/status").hasAnyAuthority("ADMINISTRATOR_UPDATE", "AGENT_UPDATE")
                                        .requestMatchers(HttpMethod.DELETE, "/accounts/membership/{idAccount}/{idCustomer}").hasAnyAuthority("ADMINISTRATOR_DELETE", "AGENT_DELETE")
                                        .anyRequest().authenticated()
                )
                .sessionManagement(httpSecuritySessionManagementConfigurer ->
                        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userService);
        daoAuthenticationProvider.setPasswordEncoder(bCryptPasswordEncoder);
        return daoAuthenticationProvider;
    }
}
