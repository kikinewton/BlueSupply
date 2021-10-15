package com.logistics.supply.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties("security")
@Getter
@Setter
@NoArgsConstructor
public class SecurityProperties {

    private JwtProperties jwt;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class JwtProperties {
        private Resource keyStore;
        private String keyStorePassword;
        private String keyPairAlias;
        private String keyPairPassword;
    }
}
