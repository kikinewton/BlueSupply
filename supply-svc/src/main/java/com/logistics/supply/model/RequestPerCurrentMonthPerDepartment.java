package com.logistics.supply.model;

import lombok.Data;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;

@Entity
@Data
@Immutable
public class RequestPerCurrentMonthPerDepartment {
    @Id
    private Integer id;
    @NotNull
    private String Department;
    @NotNull
    private Integer Num_of_Request;
}
