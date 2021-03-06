package com.logistics.supply.auth;

import com.logistics.supply.model.Employee;

import javax.validation.constraints.Email;

public interface JwtService {
    public String generateToken(Employee employee);
    public String validateToken(String token);
}
