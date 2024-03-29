package com.logistics.supply.auth;

import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Date;

@Getter
@Service
@Slf4j
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

  private final JwtConfig jwtConfig;
  @Value("${security.jwt.key-store-password}")
  String SECRET;
  private KeyStore keyStore;

  public String generateToken(Authentication authentication) {

    String subject  =  authentication.getName();
    return Jwts.builder()
        .setSubject(subject)
            .setIssuedAt(new Date())
        .signWith(SignatureAlgorithm.HS512, jwtConfig.getSecretKey())
        .compact();
  }

  @PostConstruct
  public void init() throws CertificateException, IOException, NoSuchAlgorithmException {
    try {
      this.keyStore = KeyStore.getInstance("JKS");
      InputStream resourceAsStream = getClass().getResourceAsStream("/bsupply.jks");
      keyStore.load(resourceAsStream, SECRET.toCharArray());
    } catch (KeyStoreException e) {
      e.printStackTrace();
    }
  }

  private PrivateKey getPrivateKey() {
    try {
      return (PrivateKey) keyStore.getKey("bsupply", SECRET.toCharArray());
    } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
      try {
        throw new KeyException("Exception occurred while retrieving private key from keystore");
      } catch (KeyException ex) {
        ex.printStackTrace();
      }
      return null;
    }
  }

  private PublicKey getPublicKey() throws Exception {
    try {
      return keyStore.getCertificate("bsupply").getPublicKey();
    } catch (KeyStoreException e) {
      throw new KeyException("Exception occurred while retrieving public key from keystore");
    }
  }

  public String getUserNameFromJwtToken(String token) throws Exception {
    return Jwts.parser().setSigningKey(jwtConfig.getSecretKey()).parseClaimsJws(token).getBody().getSubject();
  }

  @Override
  public boolean validateToken(String authToken) {
    try {

      Jwts.parser().setSigningKey(jwtConfig.getSecretKey()).parseClaimsJws(authToken);
      return true;
    } catch (SignatureException e) {
      log.error("Invalid JWT signature: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      log.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      log.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.error("JWT claims string is empty: {}", e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }
}
