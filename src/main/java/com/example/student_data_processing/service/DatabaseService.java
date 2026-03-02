package com.example.student_data_processing.service;

import com.example.student_data_processing.entity.Student;
import com.example.student_data_processing.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DatabaseService {

    private static final int BATCH_SIZE = 1000;

    @Autowired
    private StudentRepository studentRepository;


//    Reads a CSV file and inserts students into the database asynchronously.
//    Adds +5 to each student score (CSV -> DB requirement).
    @Async("taskExecutor")
    public void uploadCsvToDatabase(String csvFilePath) {
        log.info("Starting CSV -> DB upload for file: {}", csvFilePath);

        List<Student> batch = new ArrayList<>();
        int totalRows = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean firstRow = true;

            while ((line = br.readLine()) != null) {
                // Skip header row
                if (firstRow) {
                    firstRow = false;
                    continue;
                }

                String[] fields = line.split(",");
                if (fields.length != 6) continue; // skip malformed rows

                Student student = new Student();
                student.setStudentId(Long.parseLong(fields[0]));
                student.setFirstName(fields[1]);
                student.setLastName(fields[2]);
                student.setDob(fields[3]); // You can convert to LocalDate if needed
                student.setStudentClass(fields[4]);

                int score = Integer.parseInt(fields[5]) + 5; // CSV -> DB increment
                student.setScore(score);

                batch.add(student);
                totalRows++;

                // Batch insert
                if (batch.size() >= BATCH_SIZE) {
                    studentRepository.saveAll(batch);
                    batch.clear();
                    log.info("Inserted {} rows into DB", totalRows);
                }
            }

            // Insert remaining
            if (!batch.isEmpty()) {
                studentRepository.saveAll(batch);
                log.info("Inserted {} rows into DB", totalRows);
            }

            log.info("CSV -> DB upload completed. Total rows: {}", totalRows);

        } catch (IOException e) {
            log.error("Error reading CSV file: {}", csvFilePath, e);
        }
    }
}
