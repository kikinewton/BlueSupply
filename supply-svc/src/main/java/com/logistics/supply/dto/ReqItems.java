package com.logistics.supply.dto;


import com.logistics.supply.enums.PriorityLevel;
import com.logistics.supply.enums.RequestReason;
import com.logistics.supply.enums.RequestType;
import com.logistics.supply.model.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReqItems {
    @NotBlank
    private String name;
    @NotNull
    private RequestReason reason;
    @NotBlank
    private String purpose;
    @NotBlank @Positive
    private Integer quantity;
    @NotNull
    private Department userDepartment;
    @NotBlank
    private RequestType requestType;
    @NotNull
    private PriorityLevel priorityLevel;
}

