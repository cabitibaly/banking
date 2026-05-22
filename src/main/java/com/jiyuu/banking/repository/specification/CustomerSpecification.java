package com.jiyuu.banking.repository.specification;

import com.jiyuu.banking.dto.CustomerSearchCriteria;
import com.jiyuu.banking.entity.Customer;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor
public class CustomerSpecification {
    private static Specification<Customer> hasNom(String nom) {
        return (root, query, cb) ->
                nom == null ? null :
                        cb.like(cb.lower(root.get("nomCustomer")), "%" + nom.toLowerCase() + "%");
    }

    private static Specification<Customer> hasTelephone(String telephone) {
        return (root, query, cb) ->
                telephone == null ? null :
                        cb.like(root.get("telephoneCustomer"), "%" + telephone + "%");
    }

    private static Specification<Customer> hasStatus(String status) {
        return (root, query, cb) ->
                status == null ? null :
                        cb.equal(root.get("statusCustomer"), status);
    }

    private static Specification<Customer> hasNumero(String numero) {
        return (root, query, cb) ->
                numero == null ? null :
                        cb.like(cb.lower(root.get("numeroCustomer")), "%" + numero.toLowerCase() + "%");
    }

    public static Specification<Customer> withFiler(CustomerSearchCriteria customerSearchCriteria) {
        return Specification
                .where(hasNom(customerSearchCriteria.nom()))
                .and(hasTelephone(customerSearchCriteria.telephone()))
                .and(hasStatus(customerSearchCriteria.status()))
                .and(hasNumero(customerSearchCriteria.numero()));
    }
}
