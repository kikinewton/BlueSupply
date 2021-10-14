package com.logistics.supply.dto;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class PagedResponseDTO<T> {
  @NonNull String message;
  @NonNull String status;
  MetaData meta;
  T data;

  @Data
  @AllArgsConstructor
  public static class MetaData {
    int total;
    int pageSize;
    int currentPage;
    int totalPages;
  }

  public PagedResponseDTO(@NonNull String message, @NonNull String status, MetaData meta, T data) {
    this.message = message;
    this.status = status;
    this.meta = meta;
    this.data = data;
  }
}
