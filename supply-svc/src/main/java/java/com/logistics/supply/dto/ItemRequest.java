package java.com.logistics.supply.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.com.logistics.supply.enums.RequestReason;
import java.com.logistics.supply.model.Department;
import java.com.logistics.supply.model.RequestCategory;
import java.util.Date;

@Data
@NoArgsConstructor
public class ItemRequest {
    private String requestItemRef;
    private RequestReason reason;
    private Date createdDate;
    private String purpose;
    private int quantity;
    private String name;
    private Department userDepartment;
    private RequestCategory requestCategory;
}
