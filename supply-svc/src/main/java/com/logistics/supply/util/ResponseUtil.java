package com.logistics.supply.util;

import com.logistics.supply.dto.ResponseDto;
import lombok.experimental.UtilityClass;
import org.springframework.http.ResponseEntity;

@UtilityClass
public class ResponseUtil {

  public ResponseEntity<ResponseDto> failedResponse(String message) {
    ResponseDto failed = new ResponseDto(message, Constants.ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
