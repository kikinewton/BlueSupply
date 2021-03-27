package com.logistics.supply.model;

import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
public class ServiceRequest {

    private Integer id;

    private String name;


}
