package com.logistics.supply.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class AuthServer {
  @Value("${authServerLocal}")
  private String authServerLocal;

  @Value("${authCode}")
  private String authCode;

  public String getAuthServer() {
    return authServerLocal;
  }
}
