package com.logistics.supply.auth;

import com.logistics.supply.common.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Safety net for Phase 2: jjwt 0.9.1 → 0.12.6.
 *
 * Pins the contract of JwtServiceImpl so that after the upgrade, any API change
 * that breaks token generation, subject extraction, or validation is caught immediately.
 * Uses @IntegrationTest because JwtServiceImpl has a @PostConstruct that loads a JKS keystore,
 * requiring the full Spring context to be present.
 */
@IntegrationTest
class JwtServiceTest {

    @Autowired
    JwtServiceImpl jwtService;

    private Authentication buildAuthentication(String email) {
        return new UsernamePasswordAuthenticationToken(
                email,
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_REGULAR")));
    }

    @Test
    void shouldGenerateNonEmptyToken() {
        Authentication auth = buildAuthentication("kikinewton@gmail.com");
        String token = jwtService.generateToken(auth);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldGenerateTokenWithThreeParts() {
        // A valid compact JWS has the form header.payload.signature
        Authentication auth = buildAuthentication("kikinewton@gmail.com");
        String token = jwtService.generateToken(auth);

        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT must have exactly 3 dot-separated parts");
    }

    @Test
    void shouldExtractCorrectUsernameFromToken() throws Exception {
        String email = "kikinewton@gmail.com";
        Authentication auth = buildAuthentication(email);
        String token = jwtService.generateToken(auth);

        String extractedUsername = jwtService.getUserNameFromJwtToken(token);

        assertEquals(email, extractedUsername);
    }

    @Test
    void shouldValidateWellFormedToken() {
        Authentication auth = buildAuthentication("kikinewton@gmail.com");
        String token = jwtService.generateToken(auth);

        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void shouldRejectTamperedToken() {
        Authentication auth = buildAuthentication("kikinewton@gmail.com");
        String token = jwtService.generateToken(auth);

        // Corrupt the signature segment (last part after the second dot)
        int lastDot = token.lastIndexOf('.');
        String tampered = token.substring(0, lastDot + 1) + "INVALIDSIGNATURE";

        assertFalse(jwtService.validateToken(tampered));
    }

    @Test
    void shouldRejectMalformedToken() {
        assertFalse(jwtService.validateToken("not.a.jwt"));
    }

    @Test
    void shouldRejectEmptyToken() {
        assertFalse(jwtService.validateToken(""));
    }
}
