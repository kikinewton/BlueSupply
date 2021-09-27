package com.logistics.supply.dto;

import lombok.*;

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
}
