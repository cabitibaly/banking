package com.jiyuu.banking.entity;

import com.jiyuu.banking.enums.TypeOfRole;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_role;
    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private TypeOfRole label;
}
