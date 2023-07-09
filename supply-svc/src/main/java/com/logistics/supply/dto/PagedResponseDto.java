package com.logistics.supply.dto;

import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class PagedResponseDto<T extends Page> {
  @NonNull String message;
  @NonNull String status;
  MetaData meta;
  List<?> data;

  @Data
  @AllArgsConstructor
  public static class MetaData {
    int total;
    int pageSize;
    int currentPage;
    int totalPages;
  }

  public PagedResponseDto(@NonNull String message, @NonNull String status, MetaData meta, T data) {
    this.message = message;
    this.status = status;
    this.meta = meta;
    this.data = data.getContent();
  }

  public static <T extends Page> ResponseEntity<PagedResponseDto<T>> wrapSuccessResult(
      T data, String message) {
    PagedResponseDto<T> dto = new PagedResponseDto<>();
    MetaData metaData =
        new MetaData(
            data.getNumberOfElements(),
            data.getPageable().getPageSize(),
            data.getNumber(),
            data.getTotalPages());
    dto.message = message;
    dto.status = "SUCCESS";
    dto.meta = metaData;
    dto.data = data.getContent();
    return ResponseEntity.ok(dto);
  }
}
