package com.logistics.supply.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DataSheetDTO {
    private List<Object[]> result;
    private String sheetName;
    private String fileNamePrefix;
    private List<String> columnTitles;
}
