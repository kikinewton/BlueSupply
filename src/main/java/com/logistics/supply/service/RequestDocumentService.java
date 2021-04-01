package com.logistics.supply.service;

import com.logistics.supply.model.RequestDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Optional;

@Service
public class RequestDocumentService extends AbstractDataService {

  private final Path fileStorageLocation;

  @Autowired
  public RequestDocumentService(RequestDocument doc) {
    this.fileStorageLocation = Paths.get(doc.getUploadDirectory()).toAbsolutePath().normalize();
    try {
      Files.createDirectories(this.fileStorageLocation);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public RequestDocument storeFile(MultipartFile file, int employeeId, String docType) {
    String originalFileName = file.getOriginalFilename();
    String fileName =
        com.google.common.io.Files.getNameWithoutExtension(file.getOriginalFilename());
    String fileExtension = getExtension(fileName).get();
    fileName =
        employeeId
            + "_"
            + fileName
            + "_"
            + new Date().getTime()
            + "_"
            + docType
            + "."
            + fileExtension;
    Path targetLocation = this.fileStorageLocation.resolve(fileName);
    try {
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      e.printStackTrace();
    }

    RequestDocument newDoc = new RequestDocument();
    newDoc.setDocumentType(docType);
    newDoc.setEmployeeId(employeeId);
    newDoc.setFileName(fileName);
    newDoc.setDocumentFormat(file.getContentType());
    requestDocumentRepository.save(newDoc);
    return newDoc;
  }

  public Optional<String> getExtension(String filename) {
    return Optional.ofNullable(filename)
        .filter(f -> f.contains("."))
        .map(f -> f.substring(filename.lastIndexOf(".") + 1));
  }

  public Resource loadFileAsResource(String fileName) throws Exception {
    try {
      Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
      Resource resource = new UrlResource(filePath.toUri());
      if (resource.exists()) {
        return resource;
      } else {
        throw new FileNotFoundException("File not found " + fileName);
      }
    } catch (MalformedURLException ex) {
      throw new FileNotFoundException("File not found " + fileName);
    }
  }

  //  public RequestDocument create(RequestDocument doc) {}

}
