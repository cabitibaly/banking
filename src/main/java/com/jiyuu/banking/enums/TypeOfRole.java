package com.jiyuu.banking.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static com.jiyuu.banking.enums.TypeOfPermission.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public enum TypeOfRole {
    CUSTOMER(
            Set.of(
                    CUSTOMER_CREATE,
                    CUSTOMER_READ,
                    CUSTOMER_UPDATE,
                    CUSTOMER_DELETE
            )
    ),
    AGENT(
            Set.of(
                    AGENT_CREATE,
                    AGENT_READ,
                    AGENT_UPDATE,
                    AGENT_DELETE
            )
    ),
    ADMINISTRATOR(
            Set.of(
                    ADMINSTRATOR_CREATE,
                    ADMINSTRATOR_READ,
                    ADMINSTRATOR_UPDATE,
                    ADMINSTRATOR_DELETE
            )
    );

    @Getter
    Set<TypeOfPermission> permissions;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> grantedAuthorities = this.getPermissions().stream().map(
                permission -> new SimpleGrantedAuthority(permission.name())
        ).collect(Collectors.toList());

        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        return grantedAuthorities;
    }

}
