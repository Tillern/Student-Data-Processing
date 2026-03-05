package com.example.student_data_processing.service;

import com.example.student_data_processing.dto.GenerateExcelRequest;
import com.example.student_data_processing.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExcelService {

    private final JobService jobService;
    private final StudentRepository studentRepository;

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final char[] CHAR_POOL = ALPHANUMERIC.toCharArray();

    private static final String[] CLASSES = {"Class1", "Class2", "Class3", "Class4", "Class5"};

    private static final String DIRECTORY = "C:\\var\\log\\applications\\API\\dataprocessing\\";

    @Async("taskExecutor")
    public void generateExcelAsync(GenerateExcelRequest request, String jobId) {

        try {

            long total = request.getNumberOfRecords();

            String filePath = DIRECTORY + "students_" + System.currentTimeMillis() + ".xlsx";

            File folder = new File(DIRECTORY);
            if (!folder.exists()) folder.mkdirs();

            // Load existing IDs once
            Set<String> existingIds = new HashSet<>(studentRepository.findAllStudentIds());

            // Pre-size set to avoid resizing
            Set<String> generatedIds = new HashSet<>((int) total);

            SXSSFWorkbook workbook = new SXSSFWorkbook(100);
            Sheet sheet = workbook.createSheet("Students");

            Row header = sheet.createRow(0);
            String[] columns = {"studentId", "firstName", "lastName", "DOB", "class", "score"};

            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            for (int i = 1; i <= total; i++) {

                Row row = sheet.createRow(i);

                String studentId = generateUniqueStudentId(existingIds, generatedIds);

                row.createCell(0).setCellValue(studentId);
                row.createCell(1).setCellValue(randomString(3, 8));
                row.createCell(2).setCellValue(randomString(3, 8));
                row.createCell(3).setCellValue(randomDate(2000, 2010).toString());
                row.createCell(4).setCellValue(CLASSES[ThreadLocalRandom.current().nextInt(CLASSES.length)]);
                row.createCell(5).setCellValue(ThreadLocalRandom.current().nextInt(55, 76));

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

    private String generateUniqueStudentId(Set<String> existingIds, Set<String> generatedIds) {

        ThreadLocalRandom random = ThreadLocalRandom.current();

        while (true) {

            char[] buffer = new char[10];

            for (int i = 0; i < 10; i++) {
                buffer[i] = CHAR_POOL[random.nextInt(CHAR_POOL.length)];
            }

            String id = new String(buffer);

            if (!existingIds.contains(id) && generatedIds.add(id)) {
                return id;
            }
        }
    }

    private static String randomString(int minLen, int maxLen) {

        ThreadLocalRandom random = ThreadLocalRandom.current();

        int len = random.nextInt(minLen, maxLen + 1);

        char[] chars = new char[len];

        for (int i = 0; i < len; i++) {
            chars[i] = (char) ('A' + random.nextInt(26));
        }

        return new String(chars);
    }

    private static LocalDate randomDate(int startYear, int endYear) {

        ThreadLocalRandom random = ThreadLocalRandom.current();

        int day = random.nextInt(1, 29);
        int month = random.nextInt(1, 13);
        int year = random.nextInt(startYear, endYear + 1);

        return LocalDate.of(year, month, day);
    }
}