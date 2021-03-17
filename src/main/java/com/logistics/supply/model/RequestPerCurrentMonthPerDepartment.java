package com.logistics.supply.model;

import lombok.Data;
import org.hibernate.annotations.Immutable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Data
@Immutable
@Table(name = "request_per_current_month_per_department")
public class RequestPerCurrentMonthPerDepartment {
    @Id
    private Integer id;
    @NotNull
    private String Department;
    @NotNull
    private Integer Num_of_Request;
}
