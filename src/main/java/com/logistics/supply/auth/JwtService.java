package com.logistics.supply.auth;

import com.logistics.supply.model.Employee;
import org.springframework.security.core.Authentication;

import javax.validation.constraints.Email;

public interface JwtService {
    public String generateToken(Authentication authentication);
    public boolean validateToken(String token);
}
