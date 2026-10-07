package com.saas.backend.serviceImpl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Isolated Excel reader utility for tabular extraction from .xlsx and .xls invoices.
 * Separated from Spring beans so that missing classpath jars in hot-reload do not prevent
 * Spring application context startup or bean creation.
 */
@Slf4j
public class ExcelDocumentReader {

    private ExcelDocumentReader() {
        // Utility class
    }

    public static String extractText(byte[] fileBytes) {
        if (fileBytes == null || fileBytes.length == 0) {
            return "";
        }
        try {
            return doExtractText(fileBytes);
        } catch (NoClassDefFoundError | ClassNotFoundException e) {
            log.warn("Apache POI is not available in the current JVM classpath: {}", e.getMessage());
            return "Note: Excel parsing is not active in this JVM session (Apache POI library needs server restart).";
        } catch (Throwable t) {
            log.error("Failed to read Excel invoice content: {}", t.getMessage(), t);
            return "Failed to extract Excel content: " + t.getMessage();
        }
    }

    private static String doExtractText(byte[] fileBytes) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);
             Workbook workbook = WorkbookFactory.create(bais)) {

            int numberOfSheets = workbook.getNumberOfSheets();
            for (int s = 0; s < numberOfSheets; s++) {
                Sheet sheet = workbook.getSheetAt(s);
                sb.append("--- Sheet: ").append(sheet.getSheetName()).append(" ---\n");
                for (Row row : sheet) {
                    List<String> cellValues = new ArrayList<>();
                    for (Cell cell : row) {
                        cellValues.add(getCellValueAsString(cell));
                    }
                    String rowText = String.join(" | ", cellValues).trim();
                    if (!rowText.isBlank()) {
                        sb.append(rowText).append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    try {
                        yield cell.getStringCellValue().trim();
                    } catch (Exception ex) {
                        yield cell.getCellFormula();
                    }
                }
            }
            default -> "";
        };
    }
}
