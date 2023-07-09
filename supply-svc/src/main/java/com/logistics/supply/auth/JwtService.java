package com.logistics.supply.auth;

import org.springframework.security.core.Authentication;

public interface JwtService {
    public String generateToken(Authentication authentication) ;
    public boolean validateToken(String token);
}
