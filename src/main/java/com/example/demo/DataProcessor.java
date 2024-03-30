package com.example.demo;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class DataProcessor {

    public Message<File> processData(Message<List<Map<String, Object>>> message) {
        List<Map<String, Object>> result = message.getPayload();
        MessageHeaders headers = message.getHeaders();

        String filename = headers.containsKey("filename") ? headers.get("filename", String.class) : "output_file.xlsx";

        File outputFile = new File("D:/Hari/demo_projects/destination/" + filename);

        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(outputFile)) {
            Sheet sheet = workbook.createSheet("Data");

            int rowNum = 0;
            for (Map<String, Object> rowMap : result) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;
                for (Map.Entry<String, Object> entry : rowMap.entrySet()) {
                    Cell cell = row.createCell(colNum++);
                    if (entry.getValue() instanceof String) {
                        cell.setCellValue((String) entry.getValue());
                    } else if (entry.getValue() instanceof Integer) {
                        cell.setCellValue((Integer) entry.getValue());
                    } else if (entry.getValue() instanceof Double) {
                        cell.setCellValue((Double) entry.getValue());
                    } // Add more conditions for other data types if necessary
                }
            }

            workbook.write(fos);
            System.out.println("Data copied to: " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return MessageBuilder.withPayload(outputFile).build();
    }
}
