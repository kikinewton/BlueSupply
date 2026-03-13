package com.logistics.supply.fixture;

import com.logistics.supply.dto.SupplierDto;

public class SupplierDtoFixture {

    SupplierDtoFixture() {
    }

    public static SupplierDto getSupplierDto() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name = "Test Supplier";
        private String description = "Test supplier";
        private String phoneNo = "0551234567";
        private String location = "Accra";
        private String email = null;
        private String accountNumber = null;
        private String bank = null;
        private boolean registered = false;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder phoneNo(String phoneNo) {
            this.phoneNo = phoneNo;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder accountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public Builder bank(String bank) {
            this.bank = bank;
            return this;
        }

        public Builder registered(boolean registered) {
            this.registered = registered;
            return this;
        }

        public SupplierDto build() {
            SupplierDto dto = new SupplierDto();
            dto.setName(name);
            dto.setDescription(description);
            dto.setPhoneNo(phoneNo);
            dto.setLocation(location);
            dto.setEmail(email);
            dto.setAccountNumber(accountNumber);
            dto.setBank(bank);
            dto.setRegistered(registered);
            return dto;
        }
    }
}
