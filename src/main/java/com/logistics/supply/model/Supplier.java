package com.logistics.supply.model;


import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Data
@ToString

public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = false)
    private String name;

    private String phone_no;

    private String location;

    private String description;

    private String email;




}
