package com.logistics.supply.util;

import com.logistics.supply.dto.ResponseDTO;
import lombok.experimental.UtilityClass;
import org.springframework.http.ResponseEntity;

import static com.logistics.supply.util.Constants.ERROR;

@UtilityClass
public class ResponseUtil {

  public ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
