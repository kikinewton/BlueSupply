package com.logistics.supply.model;

import lombok.Data;
import org.hibernate.annotations.Immutable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

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
