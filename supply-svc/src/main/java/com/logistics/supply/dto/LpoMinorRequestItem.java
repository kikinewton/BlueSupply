package com.logistics.supply.dto;


import com.logistics.supply.enums.PriorityLevel;
import com.logistics.supply.enums.RequestReason;
import com.logistics.supply.enums.RequestType;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Store;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LpoMinorRequestItem {

    @NotBlank(message = "Provide the name")
    private String name;

    @NotNull(message = "Provide the reason")
    private RequestReason reason;

    @NotBlank(message = "Provide the purpose")
    private String purpose;

    @Positive
    private Integer quantity;

    @NotNull(message = "Provide the user department")
    private Department userDepartment;

    @NotNull(message = "Provide the request type")
    private RequestType requestType;

    @NotNull(message = "Provide the priority level")
    private PriorityLevel priorityLevel;

    private Store receivingStore;
}

