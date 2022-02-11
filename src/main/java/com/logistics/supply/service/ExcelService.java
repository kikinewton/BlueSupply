package com.logistics.supply.service;

import com.logistics.supply.dto.ExcelData;
import com.logistics.supply.repository.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.*;

import static com.logistics.supply.util.Constants.*;

@Service
@Slf4j
@Getter
public class ExcelService {

  @Autowired PaymentRepository paymentRepository;

  @Autowired RequestItemRepository requestItemRepository;

  @Autowired GoodsReceivedNoteRepository goodsReceivedNoteRepository;

  @Autowired PettyCashPaymentRepository pettyCashPaymentRepository;

  @Autowired public FloatOrderRepository floatOrderRepository;

  private static void writeExcel(XSSFWorkbook wb, Sheet sheet, ExcelData data) {

    int rowIndex = 0;

    rowIndex = writeTitlesToExcel(wb, sheet, data.getTitles());
    writeRowsToExcel(wb, sheet, data.getRows(), rowIndex);
    autoSizeColumns(sheet, data.getTitles().size() + 1);
  }

  private static int writeTitlesToExcel(XSSFWorkbook wb, Sheet sheet, List<String> titles) {
    int rowIndex = 0;
    int colIndex = 0;

    Font titleFont = wb.createFont();
    titleFont.setFontName("calibri");
    titleFont.setBold(true);
    titleFont.setFontHeightInPoints((short) 14);
    titleFont.setColor(IndexedColors.BLACK.index);

    XSSFCellStyle titleStyle = wb.createCellStyle();
    titleStyle.setFillForegroundColor(new XSSFColor(new Color(182, 184, 192)));
    titleStyle.setFont(titleFont);
    setBorder(titleStyle, BorderStyle.THIN, new XSSFColor(new Color(0, 0, 0)));

    Row titleRow = sheet.createRow(rowIndex);
    titleRow.setHeightInPoints(25);
    colIndex = 0;

    for (String field : titles) {
      Cell cell = titleRow.createCell(colIndex);
      cell.setCellValue(field);
      cell.setCellStyle(titleStyle);
      colIndex++;
    }

    rowIndex++;
    return rowIndex;
  }

  private static void autoSizeColumns(Sheet sheet, int columnNumber) {

    for (int i = 0; i < columnNumber; i++) {
      int orgWidth = sheet.getColumnWidth(i);
      sheet.autoSizeColumn(i, true);
      int newWidth = (int) (sheet.getColumnWidth(i) + 100);
      if (newWidth > orgWidth) {
        sheet.setColumnWidth(i, newWidth);
      } else {
        sheet.setColumnWidth(i, orgWidth);
      }
    }
  }

  private static void setBorder(XSSFCellStyle style, BorderStyle border, XSSFColor color) {
    style.setBorderTop(border);
    style.setBorderLeft(border);
    style.setBorderRight(border);
    style.setBorderBottom(border);
    style.setBorderColor(XSSFCellBorder.BorderSide.TOP, color);
    style.setBorderColor(XSSFCellBorder.BorderSide.LEFT, color);
    style.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, color);
    style.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, color);
  }

  private static int writeRowsToExcel(
      XSSFWorkbook wb, Sheet sheet, List<List<Object>> list, int rowIndex) {
    int colIndex = 0;

    Font dataFont = wb.createFont();
    dataFont.setFontName("calibri");
    dataFont.setFontHeightInPoints((short) 14);
    dataFont.setColor(IndexedColors.BLACK.index);

    XSSFCellStyle dataStyle = wb.createCellStyle();
    dataStyle.setFont(dataFont);
    setBorder(dataStyle, BorderStyle.THIN, new XSSFColor(new Color(0, 0, 0)));

    for (List<Object> rowData : list) {
      Row dataRow = sheet.createRow(rowIndex);
      dataRow.setHeightInPoints(25);
      colIndex = 0;

      for (Object cellData : rowData) {
        Cell cell = dataRow.createCell(colIndex);
        if (cellData != null) {
          if (isNumeric(cellData.toString()))
            cell.setCellValue(Double.parseDouble(cellData.toString()));
          else cell.setCellValue(cellData.toString());
        } else {
          cell.setCellValue("");
        }

        cell.setCellStyle(dataStyle);
        colIndex++;
      }
      rowIndex++;
    }
    return rowIndex;
  }

  public static boolean isNumeric(String strNum) {
    try {
      double d = Double.parseDouble(strNum);
    } catch (NumberFormatException | NullPointerException nfe) {
      return false;
    }
    return true;
  }

  public static ByteArrayInputStream exportExcel(ExcelData data, String fileName) throws Exception {

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    XSSFWorkbook wb = new XSSFWorkbook();
    try {
      String sheetName = data.getName();
      if (null == sheetName) {
        sheetName = "Sheet1";
      }
      XSSFSheet sheet = wb.createSheet(sheetName);
      writeExcel(wb, sheet, data);

      wb.write(byteArrayOutputStream);

      return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    } finally {
      wb.close();
    }
  }

  public ByteArrayInputStream createPaymentDataSheet(Date startDate, Date endDate) {
    ExcelData data = new ExcelData();
    try {
      List<Object[]> result = paymentRepository.getPaymentReport(startDate, endDate);
      @SuppressWarnings({"unchecked", "rawtypes", "unused"})
      List<List<Object>> resultConverted = new <List<Object>>ArrayList();

      for (Object[] a : result) resultConverted.add(Arrays.asList(a));

      data.setRows(resultConverted);
      data.setName("payments");
      data.setTitles(Arrays.asList(payment_report_header));
      String fileName = "", outPutFileName = "", name = "report";

      if (Objects.nonNull(name)) {
        name.replaceAll("\\s+", "");
        name.replaceAll("&", "");
        fileName = "payment_" + name + "_" + System.currentTimeMillis() + ".xlsx";
        outPutFileName = "filesLocation" + File.separator + fileName;
      } else {
        fileName = "payment_" + "_" + System.currentTimeMillis() + ".xlsx";
        outPutFileName = "filesLocation" + File.separator + fileName;
      }
      return exportExcel(data, outPutFileName);

    } catch (Exception e) {
      log.error(e.toString());
      e.printStackTrace();
    }
    return null;
  }

  public ByteArrayInputStream createProcuredItemsDataSheet(Date startDate, Date endDate) {
    ExcelData data = new ExcelData();
    try {
      List<Object[]> result = requestItemRepository.getProcuredItems(startDate, endDate);
      @SuppressWarnings({"unchecked", "rawtypes", "unused"})
      List<List<Object>> resultConverted = new <List<Object>>ArrayList();

      for (Object[] a : result) resultConverted.add(Arrays.asList(a));

      data.setRows(resultConverted);
      data.setName("ProcuredItems");
      data.setTitles(Arrays.asList(procured_items_header));
      String fileName = "", outPutFileName = "", name = "report";

      if (Objects.nonNull(name)) {
        name.replaceAll("\\s+", "");
        name.replaceAll("&", "");
        fileName = "procuredItems_" + name + "_" + System.currentTimeMillis() + ".xlsx";
        outPutFileName = "filesLocation" + File.separator + fileName;
      } else {
        fileName = "procuredItems_" + "_" + System.currentTimeMillis() + ".xlsx";
        outPutFileName = "filesLocation" + File.separator + fileName;
      }
      return exportExcel(data, outPutFileName);

    } catch (Exception e) {
      log.error(e.toString());
      e.printStackTrace();
    }
    return null;
  }

  public ByteArrayInputStream createGRNDataSheet(Date startDate, Date endDate) {
    ExcelData data = new ExcelData();
    try {
      List<Object[]> result =
          goodsReceivedNoteRepository.getGoodsReceivedNoteReport(startDate, endDate);

      @SuppressWarnings({"unchecked", "rawtypes", "unused"})
      List<List<Object>> resultConverted = new <List<Object>>ArrayList();

      for (Object[] a : result) resultConverted.add(Arrays.asList(a));

      data.setRows(resultConverted);
      data.setName("GoodReceivedNote");
      data.setTitles(Arrays.asList(grn_report_header));
      String fileName = "", outPutFileName = "", name = "report";

      if (Objects.nonNull(name)) {
        name.replaceAll("\\s+", "");
        name.replaceAll("&", "");
        fileName = "grn_" + name + "_" + System.currentTimeMillis() + ".xlsx";
        outPutFileName = "filesLocation" + File.separator + fileName;
      } else {
        fileName = "grn" + "_" + System.currentTimeMillis() + ".xlsx";
        outPutFileName = "filesLocation" + File.separator + fileName;
      }
      return exportExcel(data, outPutFileName);

    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public ByteArrayInputStream createFloatAgingAnalysis() {
    ExcelData data = new ExcelData();
    try {
      List<Object[]> result = floatOrderRepository.getAgingAnalysis();
      List<List<Object>> resultConverted = new <List<Object>>ArrayList();

      for (Object[] a : result) resultConverted.add(Arrays.asList(a));
      data.setRows(resultConverted);
      data.setName("FloatsAgeingAnalysis");
      data.setTitles(Arrays.asList(float_ageing_report_header));
      String fileName = "", outPutFileName = "", name = "report";

      if (Objects.nonNull(name)) {
        name.replaceAll("\\s+", "");
        name.replaceAll("&", "");
        fileName = "float_ageing_analysis_" + name + "_" + System.currentTimeMillis() + ".xlsx";
        outPutFileName = "filesLocation" + File.separator + fileName;
      } else {
        fileName = "float_ageing_analysis" + "_" + System.currentTimeMillis() + ".xlsx";
        outPutFileName = "filesLocation" + File.separator + fileName;
      }
      return exportExcel(data, outPutFileName);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public ByteArrayInputStream createPettyCashPaymentDataSheet(
      LocalDate startDate, LocalDate endDate) {
    ExcelData data = new ExcelData();
    try {
      List<Object[]> result = pettyCashPaymentRepository.getPaymentReport(startDate, endDate);

      @SuppressWarnings({"unchecked", "rawtypes", "unused"})
      List<List<Object>> resultConverted = new <List<Object>>ArrayList();

      for (Object[] a : result) resultConverted.add(Arrays.asList(a));

      data.setRows(resultConverted);
      data.setName("PettyCashPayment");
      data.setTitles(Arrays.asList(petty_cash_payment_header));
      String fileName = "", outPutFileName = "", name = "report";

      if (Objects.nonNull(name)) {
        name.replaceAll("\\s+", "");
        name.replaceAll("&", "");
        fileName = "ptc_payment" + name + "_" + System.currentTimeMillis() + ".xlsx";
        outPutFileName = "filesLocation" + File.separator + fileName;
      } else {
        fileName = "ptc_payment" + "_" + System.currentTimeMillis() + ".xlsx";
        outPutFileName = "filesLocation" + File.separator + fileName;
      }
      return exportExcel(data, outPutFileName);

    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }
}
