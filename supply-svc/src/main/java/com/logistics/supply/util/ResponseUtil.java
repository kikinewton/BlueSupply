package com.logistics.supply.util;

import com.logistics.supply.dto.ResponseDTO;
import lombok.experimental.UtilityClass;
import org.springframework.http.ResponseEntity;

@UtilityClass
public class ResponseUtil {

  public ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, Constants.ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
