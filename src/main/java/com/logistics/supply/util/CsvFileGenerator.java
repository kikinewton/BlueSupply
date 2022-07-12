package com.logistics.supply.util;

import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import static com.logistics.supply.util.Constants.comment_header;

public class CsvFileGenerator {

  @SneakyThrows(value = {IOException.class, Exception.class})
  public static ByteArrayInputStream toCSV(List<List<String>> data) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(outputStream), CSVFormat.DEFAULT);
    csvPrinter.printRecord(Arrays.asList(comment_header));
    data.forEach(
        d -> {
          try {
            csvPrinter.printRecord(d);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
    csvPrinter.flush();
    return new ByteArrayInputStream(outputStream.toByteArray());
  }
}
