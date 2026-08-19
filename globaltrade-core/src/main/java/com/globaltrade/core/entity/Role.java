package com.globaltrade.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "role")
@NamedQueries({
        @NamedQuery(name = "Role.findByRoleName",
                query = "SELECT r FROM Role r WHERE r.roleName = :roleName"),
        @NamedQuery(name = "Role.findAll",
                query = "SELECT r FROM Role r ORDER BY r.roleName")
})
@Getter
@Setter
@NoArgsConstructor
public class Role implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;
}
