package com.logistics.supply.dto;

import lombok.*;
import org.springframework.http.ResponseEntity;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class ResponseDto<T> {

    @NonNull
    String message;
    @NonNull
    String status;
    T data;

    public ResponseDto(T data) {
        this.data = data;
    }


    public ResponseDto(@NonNull String message, @NonNull String status, T data) {
        this.message = message;
        this.status = status;
        this.data = data;
    }

    public ResponseDto(@NonNull String message, @NonNull String status) {
        this.message = message;
        this.status = status;
    }

    public static <T> ResponseEntity<ResponseDto<T>> wrapSuccessResult(T data, String message) {
        ResponseDto<T> responseDTO = new ResponseDto<>(message, "SUCCESS", data);
        return ResponseEntity.ok(responseDTO);
    }

    public static <T> ResponseEntity<ResponseDto<T>> wrapErrorResult(String message) {
        ResponseDto<T> responseDTO = new ResponseDto<>(message, "ERROR", null);
        return ResponseEntity.badRequest().body(responseDTO);
    }
}
