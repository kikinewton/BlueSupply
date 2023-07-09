package com.logistics.supply.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExcelData {

  private List<String> titles;

  private List<List<Object>> rows;

  private String name;
}
