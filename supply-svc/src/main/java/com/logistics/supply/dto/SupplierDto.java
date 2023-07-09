package com.logistics.supply.dto;

import com.logistics.supply.annotation.ValidDescription;
import com.logistics.supply.annotation.ValidName;
import com.logistics.supply.model.Supplier;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.Email;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class SupplierDto {

  private String phoneNo;
  private String location;
  @ValidDescription private String description;
  @Email private String email;
  private String accountNumber;
  private String bank;
  private boolean registered;
  @ValidName private String name;
  Integer id;

  public static SupplierDto toDto(Supplier supplier) {
    SupplierDto supplierDTO = new SupplierDto();
    BeanUtils.copyProperties(supplier, supplierDTO);
    supplierDTO.setId(supplier.getId());
    return supplierDTO;
  }
}
