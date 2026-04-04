package com.logistics.supply.dto;

import com.logistics.supply.annotation.ValidDescription;
import com.logistics.supply.annotation.ValidName;
import com.logistics.supply.model.Supplier;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import jakarta.validation.constraints.Email;

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
    SupplierDto supplierDto = new SupplierDto();
    supplierDto.setId(supplier.getId());
    supplierDto.setName(supplier.getName());
    supplierDto.setPhoneNo(supplier.getPhoneNo());
    supplierDto.setLocation(supplier.getLocation());
    supplierDto.setDescription(supplier.getDescription());
    supplierDto.setEmail(supplier.getEmail());
    supplierDto.setAccountNumber(supplier.getAccountNumber());
    supplierDto.setBank(supplier.getBank());
    supplierDto.setRegistered(supplier.isRegistered());
    return supplierDto;
  }
}
