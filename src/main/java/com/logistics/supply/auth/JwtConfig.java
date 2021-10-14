package com.logistics.supply.auth;

import com.sun.istack.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    @NotNull
    @Value("${jwt.secretKey}")
    private String secretKey;

    @NotNull
    @Value("${jwt.issuer}")
    private String issuer;

    @NotNull
    @Value("${jwt.validityInSeconds}")
    private Long validityInSeconds;
}
