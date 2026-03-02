package com.example.student_data_processing.service;

import com.example.student_data_processing.dto.GenerateExcelRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.Month;
import java.util.Random;

@Service
@Slf4j
public class ExcelService {

    private static final String[] CLASSES = {"Class1", "Class2", "Class3", "Class4", "Class5"};
    private static final Random RANDOM = new Random();

    @Async("taskExecutor")
    public void generateExcelAsync(GenerateExcelRequest request) {
        try {
            String filePath = "C:\\var\\log\\applications\\API\\dataprocessing\\students.xlsx";
            File folder = new File(filePath).getParentFile();
            if (!folder.exists()) folder.mkdirs();

            // Streaming workbook
            // keep 100 rows in memory
            SXSSFWorkbook workbook = new SXSSFWorkbook(100);
            Sheet sheet = workbook.createSheet("Students");

            // Header
            Row header = sheet.createRow(0);
            String[] columns = {"studentId", "firstName", "lastName", "DOB", "class", "score"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
            }

            for (int i = 1; i <= request.getNumberOfRecords(); i++) {
                Row row = sheet.createRow(i);

                row.createCell(0).setCellValue(i); // studentId
                row.createCell(1).setCellValue(randomString(3, 8)); // firstName
                row.createCell(2).setCellValue(randomString(3, 8)); // lastName
                row.createCell(3).setCellValue(randomDate(2000, 2010).toString()); // DOB
                row.createCell(4).setCellValue(CLASSES[RANDOM.nextInt(CLASSES.length)]); // class
                row.createCell(5).setCellValue(RANDOM.nextInt(21) + 55); // score 55-75

                if (i % 10000 == 0) log.info("Generated {} records", i);
            }

            // Write to file
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }
            workbook.dispose();
            workbook.close();

            log.info("Excel generation completed: {}", filePath);
        } catch (Exception e) {
            log.error("Error generating Excel", e);
        }
    }

    private static String randomString(int minLen, int maxLen) {
        int len = RANDOM.nextInt(maxLen - minLen + 1) + minLen;
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append((char) (RANDOM.nextInt(26) + 'A'));
        }
        return sb.toString();
    }

    private static LocalDate randomDate(int startYear, int endYear) {
        int day = RANDOM.nextInt(28) + 1;
        int month = RANDOM.nextInt(12) + 1;
        int year = RANDOM.nextInt(endYear - startYear + 1) + startYear;
        return LocalDate.of(year, month, day);
    }
}
