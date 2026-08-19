package com.globaltrade.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "warehouse")
@NamedQueries({
        @NamedQuery(name = "Warehouse.findAll",
                query = "SELECT w FROM Warehouse w ORDER BY w.name")
})
@Getter
@Setter
@NoArgsConstructor
public class Warehouse implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 200)
    private String location;

    private Integer capacity;

}
