package com.logistics.supply.fixture;

import com.logistics.supply.model.RequestDocument;

public class RequestDocumentFixture {

  RequestDocumentFixture() {}

  public static RequestDocument getRequestDocument(String fileName) {

    // Create a list of RequestDocument objects
    RequestDocument document1 = new RequestDocument();
    document1.setFileName(fileName);
    document1.setDocumentType("Text");
    document1.setDocumentFormat("PDF");
    return document1;
  }

}
