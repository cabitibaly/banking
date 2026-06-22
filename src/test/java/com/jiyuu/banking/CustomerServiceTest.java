package com.jiyuu.banking;

import com.jiyuu.banking.dto.CustomerRequest;
import com.jiyuu.banking.dto.CustomerResponse;
import com.jiyuu.banking.entity.Customer;
import com.jiyuu.banking.entity.Role;
import com.jiyuu.banking.entity.User;
import com.jiyuu.banking.enums.StatusCustomer;
import com.jiyuu.banking.enums.TypeOfRole;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.CustomerRepository;
import com.jiyuu.banking.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    public void shouldThrowNullPointerExceptionWhenDateIsNull() {
        User user = this.buildUser();
        CustomerRequest request = new CustomerRequest(
                "Kyotaka Ayanokoji",
                "66583205",
                null
        );

        assertThrows(
                NullPointerException.class,
                () -> customerService.createCustomer(user, request)
        );
    }

    @Test
    public void shouldThrowsInvalidAgeOnCreateCustomer() {
        User user = this.buildUser();
        CustomerRequest request = new CustomerRequest(
                "Kyotaka Ayanokoji",
                "66583205",
                LocalDateTime.of(2010, 6, 22, 0, 0)
        );

        assertThrows(
                ValidationException.class,
                () -> customerService.createCustomer(user, request)
        );
    }

    @Test
    public void shouldThrowsTelephoneExistOnCreateCustomer() {
        User user = this.buildUser();
        Customer customer = new Customer();
        CustomerRequest request = new CustomerRequest(
                "Kyotaka Ayanokoji",
                "66583205",
                LocalDateTime.of(2000, 6, 22, 0, 0)
        );

        when(customerRepository.findBytelephoneCustomer("66583205"))
                .thenReturn(Optional.of(customer));

        assertThrows(
                ValidationException.class,
                () -> customerService.createCustomer(user, request)
        );
    }

    @Test
    public void shouldReturnCustomerResponseOnCreateCustomer() {
        User user = this.buildUser();
        Customer customer = this.buildCustomer(user, "PENDING");
        CustomerRequest request = new CustomerRequest(
                "Kyotaka Ayanokoji",
                "66583205",
                LocalDateTime.of(2000, 6, 22, 0, 0)
        );

        when(customerRepository.findBytelephoneCustomer("66583205"))
                .thenReturn(Optional.empty());

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(customer);

        CustomerResponse response = customerService.createCustomer(user, request);

        assertNotNull(response);
        assertEquals(customer.getNumeroCustomer(), response.numero());
        assertEquals(StatusCustomer.PENDING, customer.getStatusCustomer());
        verify(customerRepository).save(any(Customer.class));
        verify(customerRepository).findBytelephoneCustomer("66583205");
    }

    private User buildUser() {
        Role role = new Role(1L, TypeOfRole.valueOf("CUSTOMER"));
        return User.builder()
                .idUser(1L)
                .email("example@exalome.com")
                .enabled(true)
                .password("$2a$10$hashedvalue")
                .tokenVersion(1)
                .role(role)
                .customer(null)
                .build();
    }

    private Customer buildCustomer(User user, String status) {
        return Customer.builder()
                .idCustomer(1L)
                .dateNaissance(LocalDateTime.of(2000, 6, 22, 0, 0))
                .telephoneCustomer("66583205")
                .nomCustomer("Kyotaka Ayanokoji")
                .numeroCustomer("CLI12345678")
                .statusCustomer(StatusCustomer.valueOf(status))
                .user(user)
                .build();
    }
}
