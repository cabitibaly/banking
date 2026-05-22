package com.jiyuu.banking.repository.specification;

import com.jiyuu.banking.dto.AccountSearchCriteria;
import com.jiyuu.banking.entity.Account;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor
public class AccountSpecification {
    private static Specification<Account> hasNumero(String numero) {
        return (root, query, cb) ->
                numero == null ? null :
                        cb.like(cb.lower(root.get("numeroAccount")), "%" + numero.toLowerCase() + "%");
    }

    private static Specification<Account> hasStatus(String status) {
        return (root, query, cb) ->
                status == null ? null :
                        cb.equal(root.get("accountStatus"), status);
    }

    private static Specification<Account> hasType(String type) {
        return (root, query, cb) ->
                type == null ? null :
                        cb.equal(root.get("accountType"), type);
    }

    public static Specification<Account> withFiler(AccountSearchCriteria accountSearchCriteria) {
        return Specification
                .where(hasNumero(accountSearchCriteria.numero()))
                .and(hasStatus(accountSearchCriteria.status()))
                .and(hasType(accountSearchCriteria.type()));
    }
}
