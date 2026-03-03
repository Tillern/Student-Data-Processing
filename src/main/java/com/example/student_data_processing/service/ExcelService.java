package com.example.student_data_processing.service;

import com.example.student_data_processing.dto.GenerateExcelRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExcelService {

    private final JobService jobService;

    private static final String[] CLASSES = {"Class1", "Class2", "Class3", "Class4", "Class5"};
    private static final Random RANDOM = new Random();
    private static final String DIRECTORY =
            "C:\\var\\log\\applications\\API\\dataprocessing\\";

    @Async("taskExecutor")
    public void generateExcelAsync(GenerateExcelRequest request, String jobId) {

        try {

            String filePath = DIRECTORY + "students_" + System.currentTimeMillis() + ".xlsx";

            File folder = new File(DIRECTORY);
            if (!folder.exists()) folder.mkdirs();

            SXSSFWorkbook workbook = new SXSSFWorkbook(100);
            Sheet sheet = workbook.createSheet("Students");

            // Header
            Row header = sheet.createRow(0);
            String[] columns = {"studentId", "firstName", "lastName", "DOB", "class", "score"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            long total = request.getNumberOfRecords();

            for (int i = 1; i <= total; i++) {

                Row row = sheet.createRow(i);

                row.createCell(0).setCellValue(i);
                row.createCell(1).setCellValue(randomString(3, 8));
                row.createCell(2).setCellValue(randomString(3, 8));
                row.createCell(3).setCellValue(randomDate(2000, 2010).toString());
                row.createCell(4).setCellValue(CLASSES[RANDOM.nextInt(CLASSES.length)]);
                row.createCell(5).setCellValue(RANDOM.nextInt(21) + 55);

                if (i % 10000 == 0) {
                    log.info("Generated {} records", i);
                    jobService.updateProgress(jobId, i);
                }
            }

            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }

            workbook.dispose();
            workbook.close();

            log.info("Excel generation completed: {}", filePath);
            jobService.completeJob(jobId);

        } catch (Exception e) {
            log.error("Error generating Excel", e);
            jobService.failJob(jobId, e.getMessage());
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