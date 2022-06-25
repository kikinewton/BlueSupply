package com.logistics.supply.dto;

import lombok.*;
import org.springframework.http.ResponseEntity;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class ResponseDTO<T> {

    @NonNull
    String message;
    @NonNull
    String status;
    T data;

    public ResponseDTO(T data) {
        this.data = data;
    }


    public ResponseDTO(@NonNull String message, @NonNull String status, T data) {
        this.message = message;
        this.status = status;
        this.data = data;
    }

    public ResponseDTO(@NonNull String message, @NonNull String status) {
        this.message = message;
        this.status = status;
    }

    public static <T> ResponseEntity<ResponseDTO<T>> wrapSuccessResult(T t, String message) {
        ResponseDTO<T> responseDTO = new ResponseDTO<>(message, "SUCCESS", t);
        return ResponseEntity.ok(responseDTO);
    }

    public static <T> ResponseEntity<ResponseDTO<T>> wrapErrorResult(String message) {
        ResponseDTO<T> responseDTO = new ResponseDTO<>(message, "ERROR", null);
        return ResponseEntity.badRequest().body(responseDTO);
    }
}
