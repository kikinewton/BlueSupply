package com.logistics.supply.service;

import com.logistics.supply.dto.DataSheetDTO;
import com.logistics.supply.dto.ExcelData;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.repository.*;
import com.logistics.supply.repository.RequestItemRepository;
import com.logistics.supply.util.Constants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.*;

@Service
@Slf4j
@Getter
public class ExcelService {
  @Autowired
  FloatAgingAnalysisRepository floatAgingAnalysisRepository;
  @Autowired
  PaymentReportRepository paymentReportRepository;
  @Autowired
  RequestItemRepository requestItemRepository;
  @Autowired
  GoodsReceivedNoteRepository goodsReceivedNoteRepository;
  @Autowired
  PettyCashPaymentReportRepository pettyCashPaymentReportRepository;

  @Autowired
  FloatOrderPaymentReportRepository floatOrderPaymentReportRepository;


  private static void writeExcel(XSSFWorkbook wb, Sheet sheet, ExcelData data) {

    int rowIndex;

    rowIndex = writeTitlesToExcel(wb, sheet, data.getTitles());
    writeRowsToExcel(wb, sheet, data.getRows(), rowIndex);
    autoSizeColumns(sheet, data.getTitles().size() + 1);
  }

  private static int writeTitlesToExcel(XSSFWorkbook wb, Sheet sheet, List<String> titles) {
    int rowIndex = 0;
    int colIndex;

    Font titleFont = wb.createFont();
    titleFont.setFontName("calibri");
    titleFont.setBold(true);
    titleFont.setFontHeightInPoints((short) 14);
    titleFont.setColor(IndexedColors.BLACK.index);


    IndexedColorMap colorMap = wb.getStylesSource().getIndexedColors();
    XSSFCellStyle titleStyle = wb.createCellStyle();
    titleStyle.setFillForegroundColor(new XSSFColor(new Color(182, 184, 192), colorMap ));
    titleStyle.setFont(titleFont);
    setBorder(titleStyle, new XSSFColor( new Color(0, 0, 0), colorMap));

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
      int newWidth = sheet.getColumnWidth(i) + 100;
      sheet.setColumnWidth(i, Math.max(newWidth, orgWidth));
    }
  }

  private static void setBorder(XSSFCellStyle style, XSSFColor color) {
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderColor(XSSFCellBorder.BorderSide.TOP, color);
    style.setBorderColor(XSSFCellBorder.BorderSide.LEFT, color);
    style.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, color);
    style.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, color);
  }

  private static void writeRowsToExcel(
      XSSFWorkbook wb, Sheet sheet, List<List<Object>> list, int rowIndex) {
    int colIndex;

    Font dataFont = wb.createFont();
    dataFont.setFontName("calibri");
    dataFont.setFontHeightInPoints((short) 14);
    dataFont.setColor(IndexedColors.BLACK.index);

    XSSFCellStyle dataStyle = wb.createCellStyle();
    dataStyle.setFont(dataFont);
    setBorder(dataStyle, new XSSFColor());

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
  }

  public static boolean isNumeric(String strNum) {
    try {
      Double.parseDouble(strNum);
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

  public ByteArrayInputStream createPaymentDataSheet(Date startDate, Date endDate) throws GeneralException {
    ExcelData data = new ExcelData();
    try {
      List<Object[]> result = paymentReportRepository.findAllByPaymentDateBetween(startDate, endDate);
      @SuppressWarnings({"unchecked", "rawtypes", "unused"})
      List<List<Object>> resultConverted = new <List<Object>>ArrayList();

      for (Object[] a : result) resultConverted.add(Arrays.asList(a));

      data.setRows(resultConverted);
      data.setName("payments");
      data.setTitles(Arrays.asList(Constants.payment_report_header));
      String fileName = "", outPutFileName = "", name = "report";

      if (Objects.nonNull(name)) {
        name.replaceAll("\\s+", "");
        name.replaceAll("&", "");
        fileName = "payment_" + name + "_" + System.currentTimeMillis() + ".xlsx";
      } else {
        fileName = "payment_" + "_" + System.currentTimeMillis() + ".xlsx";
      }
      outPutFileName = "filesLocation" + File.separator + fileName;
      return exportExcel(data, outPutFileName);

    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(Constants.REPORT_GENERATION_FAILED, HttpStatus.BAD_REQUEST);

  }

  public ByteArrayInputStream createProcuredItemsDataSheet(Date startDate, Date endDate) throws GeneralException {
    ExcelData data = new ExcelData();
    try {
      List<Object[]> result = requestItemRepository.getProcuredItems(startDate, endDate);
      @SuppressWarnings({"unchecked", "rawtypes", "unused"})
      List<List<Object>> resultConverted = new <List<Object>>ArrayList();

      for (Object[] a : result) resultConverted.add(Arrays.asList(a));

      data.setRows(resultConverted);
      data.setName("ProcuredItems");
      data.setTitles(Arrays.asList(Constants.procured_items_header));
      String fileName = "";
      String name = "report";

      if (Objects.nonNull(name)) {
        name.replaceAll("\\s+", "");
        name.replaceAll("&", "");
        fileName = "procuredItems_" + name + "_" + System.currentTimeMillis() + ".xlsx";
      } else {
        fileName = "procuredItems_" + "_" + System.currentTimeMillis() + ".xlsx";
      }
      String outPutFileName = "filesLocation" + File.separator + fileName;
      return exportExcel(data, outPutFileName);

    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(Constants.REPORT_GENERATION_FAILED, HttpStatus.BAD_REQUEST);

  }

  public ByteArrayInputStream createGRNDataSheet(Date startDate, Date endDate) throws GeneralException {
    ExcelData data = new ExcelData();
    try {
      List<Object[]> result =
          goodsReceivedNoteRepository.getGoodsReceivedNoteReport(startDate, endDate);

      @SuppressWarnings({"unchecked", "rawtypes", "unused"})
      List<List<Object>> resultConverted = new <List<Object>>ArrayList();

      for (Object[] a : result) resultConverted.add(Arrays.asList(a));

      data.setRows(resultConverted);
      data.setName("GoodReceivedNote");
      data.setTitles(Arrays.asList(Constants.grn_report_header));
      String fileName = "", outPutFileName = "", name = "report";

      if (Objects.nonNull(name)) {
        name.replaceAll("\\s+", "");
        name.replaceAll("&", "");
        fileName = "grn_" + name + "_" + System.currentTimeMillis() + ".xlsx";
      } else {
        fileName = "grn" + "_" + System.currentTimeMillis() + ".xlsx";
      }
      outPutFileName = "filesLocation" + File.separator + fileName;
      return exportExcel(data, outPutFileName);

    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(Constants.REPORT_GENERATION_FAILED, HttpStatus.BAD_REQUEST);
  }


  public ByteArrayInputStream createFloatAgingAnalysis(Date startDate, Date endDate) throws GeneralException {
    ExcelData data = new ExcelData();
    try {
      List<Object[]> result = floatAgingAnalysisRepository.getAgingAnalysis(startDate, endDate);
      List<List<Object>> resultConverted = new <List<Object>>ArrayList();

      for (Object[] a : result) resultConverted.add(Arrays.asList(a));
      data.setRows(resultConverted);
      data.setName("FloatsAgeingAnalysis");
      data.setTitles(Arrays.asList(Constants.float_ageing_report_header));
      String fileName = "", outPutFileName = "", name = "report";

      if (Objects.nonNull(name)) {
        name.replaceAll("\\s+", "");
        name.replaceAll("&", "");
        fileName = "float_ageing_analysis_" + name + "_" + System.currentTimeMillis() + ".xlsx";
      } else {
        fileName = "float_ageing_analysis" + "_" + System.currentTimeMillis() + ".xlsx";
      }
      outPutFileName = "filesLocation" + File.separator + fileName;
      return exportExcel(data, outPutFileName);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(Constants.REPORT_GENERATION_FAILED, HttpStatus.BAD_REQUEST);

  }

  public ByteArrayInputStream createFloatOrderPaymentDataSheet(Date startDate, Date endDate) throws GeneralException {
    ExcelData data = new ExcelData();
    try {
      List<Object[]> result = floatOrderPaymentReportRepository.getByFundsAllocatedDateBetween(startDate, endDate);
      List<List<Object>> resultConverted = new <List<Object>>ArrayList();

      for (Object[] a : result) resultConverted.add(Arrays.asList(a));
      data.setRows(resultConverted);
      data.setName("FloatOrderPaymentReport");
      data.setTitles(Arrays.asList(Constants.float_order_payment_report_header));
      String fileName = "", outPutFileName = "", name = "report";

      if (Objects.nonNull(name)) {
        name.replaceAll("\\s+", "");
        name.replaceAll("&", "");
        fileName = "float_order_payment_report_" + name + "_" + System.currentTimeMillis() + ".xlsx";
      } else {
        fileName = "float_order_payment_report_" + "_" + System.currentTimeMillis() + ".xlsx";
      }
      outPutFileName = "filesLocation" + File.separator + fileName;
      return exportExcel(data, outPutFileName);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(Constants.REPORT_GENERATION_FAILED, HttpStatus.BAD_REQUEST);

  }



  public ByteArrayInputStream createPettyCashPaymentDataSheet(
      Date startDate, Date endDate) throws GeneralException {
    ExcelData data = new ExcelData();
    try {
      List<Object[]> result = pettyCashPaymentReportRepository.getPaymentReport(startDate, endDate);

      @SuppressWarnings({"unchecked", "rawtypes", "unused"})
      List<List<Object>> resultConverted = new <List<Object>>ArrayList();

      for (Object[] a : result) resultConverted.add(Arrays.asList(a));

      data.setRows(resultConverted);
      data.setName("PettyCashPayment");
      data.setTitles(Arrays.asList(Constants.petty_cash_payment_header));
      String fileName = "", outPutFileName = "", name = "report";

      if (Objects.nonNull(name)) {
        name.replaceAll("\\s+", "");
        name.replaceAll("&", "");
        fileName = "ptc_payment" + name + "_" + System.currentTimeMillis() + ".xlsx";
      } else {
        fileName = "ptc_payment" + "_" + System.currentTimeMillis() + ".xlsx";
      }
      outPutFileName = "filesLocation" + File.separator + fileName;
      return exportExcel(data, outPutFileName);

    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(Constants.REPORT_GENERATION_FAILED, HttpStatus.BAD_REQUEST);
  }

  public ByteArrayInputStream getDataSheet(
          DataSheetDTO sheetDTO) throws GeneralException {
    ExcelData data = new ExcelData();
    try {

      @SuppressWarnings({"unchecked", "rawtypes", "unused"})
      List<List<Object>> resultConverted = new <List<Object>>ArrayList();

      for (Object[] a : sheetDTO.getResult()) resultConverted.add(Arrays.asList(a));

      data.setRows(resultConverted);
      data.setName(sheetDTO.getSheetName());
      data.setTitles(sheetDTO.getColumnTitles());
      String fileName = "", outPutFileName = "", name = "report";

      if (Objects.nonNull(name)) {
        name.replaceAll("\\s+", "");
        name.replaceAll("&", "");
        fileName = sheetDTO.getFileNamePrefix() + name + "_" + System.currentTimeMillis() + ".xlsx";
      } else {
        fileName = sheetDTO.getFileNamePrefix() + "_" + System.currentTimeMillis() + ".xlsx";
      }
      outPutFileName = "filesLocation" + File.separator + fileName;
      return exportExcel(data, outPutFileName);

    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(Constants.REPORT_GENERATION_FAILED, HttpStatus.BAD_REQUEST);
  }
}
