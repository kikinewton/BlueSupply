package com.logistics.supply.model;


import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String name;

    @Column
    private String description;

//    @OneToMany
//    @JoinColumn(name="employee_id")
//    private List<Employee> employees;

}
