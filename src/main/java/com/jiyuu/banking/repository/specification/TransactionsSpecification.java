package com.jiyuu.banking.repository.specification;

import com.jiyuu.banking.entity.Transactions;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

@NoArgsConstructor
public class TransactionsSpecification {
    public static Specification<Transactions> hasAccount(Long idAccount) {
        return (root, query, cb) ->
                cb.or(
                        cb.equal(root.get("sourceAccount").get("idAccount"), idAccount),
                        cb.equal(root.get("targetAccount").get("idAccount"), idAccount)
                );
    }

    public static Specification<Transactions> hasStartDate(LocalDateTime startDate) {
        return (root, query, cb) -> {
            if (startDate == null) return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get("createdAt"), startDate);
        };
    }

    public static Specification<Transactions> hasEndDate(LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (endDate == null) return cb.conjunction();
            return cb.lessThanOrEqualTo(root.get("createdAt"), endDate);
        };
    }

    public static Specification<Transactions> withFiler(Long idAccount, LocalDateTime startDate, LocalDateTime endDate) {
        return Specification
                .where(hasAccount(idAccount))
                .and(hasStartDate(startDate))
                .and(hasEndDate(endDate));
    }
}
