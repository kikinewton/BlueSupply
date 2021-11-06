package com.logistics.supply.dto;

import com.logistics.supply.annotation.ValidDescription;
import com.logistics.supply.annotation.ValidName;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.Email;

@Getter
@ToString
public class SupplierDTO {

  String phone_no;
  String location;
  @ValidDescription String description;
  @Email String email;
  String accountNumber;
  String bank;
  boolean registered;
  @ValidName private String name;
}
