package com.logistics.supply.errorhandling;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
public class ApiError {

  private HttpStatus status;
  private String message;



  public ApiError(
          final HttpStatus status,
          final String message) {

    super();
    this.status = status;
    this.message = message;
  }


}
