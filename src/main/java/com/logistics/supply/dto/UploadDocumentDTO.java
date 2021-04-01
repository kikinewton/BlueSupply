package com.logistics.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadDocumentDTO {

  private String fileName;
  private long fileSize;
  private String fileType;
  private String fileDownloadUri;
}
