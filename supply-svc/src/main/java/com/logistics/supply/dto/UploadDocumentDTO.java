package com.logistics.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UploadDocumentDTO {
  private int id;
  private String fileName;
  private long fileSize;
  private String fileType;
  private String fileDownloadUri;
}
