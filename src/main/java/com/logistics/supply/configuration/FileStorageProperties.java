package com.logistics.supply.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

@ConfigurationProperties(prefix = "file")
@Getter
@Setter
public class FileStorageProperties {

  //  @Value("${file.upload-dir}")
  private String uploadDirectory = System.getProperty("user.home") + File.separator + "BSupplyUploads";

  private String lpoDirectory = System.getProperty("user.home") + File.separator + "lpo";
}
