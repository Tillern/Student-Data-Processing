package com.example.student_data_processing.service;

import com.example.student_data_processing.entity.Job;
import com.example.student_data_processing.entity.Student;
import com.example.student_data_processing.repository.JobRepository;
import com.example.student_data_processing.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseService {

    private static final int BATCH_SIZE = 1000;

    private final StudentRepository studentRepository;
    private final JobRepository jobRepository;

    @Async("taskExecutor")
    public void uploadCsvToDatabase(String csvFilePath, String jobId) {
        log.info("Starting CSV -> DB upload for file: {}", csvFilePath);

        Job job = jobRepository.findById(jobId).orElseThrow();
        job.setStatus("PROCESSING");
        job.setStartedAt(LocalDateTime.now());
        job.setTotalRecords(countCsvRows(csvFilePath) - 1); // minus header
        jobRepository.save(job);

        List<Student> batch = new ArrayList<>();
        int processedRows = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean firstRow = true;

            while ((line = br.readLine()) != null) {
                // Skip header
                if (firstRow) {
                    firstRow = false;
                    continue;
                }

                String[] fields = line.split(",");
                if (fields.length != 6) continue;

                Student student = new Student();
                student.setStudentId(Long.parseLong(fields[0]));
                student.setFirstName(fields[1]);
                student.setLastName(fields[2]);
                student.setDob(LocalDate.parse(fields[3]));
                student.setStudentClass(fields[4]);
                student.setScore(Integer.parseInt(fields[5]) + 5); // CSV -> DB +5

                batch.add(student);
                processedRows++;

                if (batch.size() >= BATCH_SIZE) {
                    studentRepository.saveAll(batch);
                    batch.clear();

                    // Update job progress
                    job.setProcessedRecords(processedRows);
                    jobRepository.save(job);

                    log.info("Inserted {} rows into DB", processedRows);
                }
            }

            if (!batch.isEmpty()) {
                studentRepository.saveAll(batch);
                job.setProcessedRecords(processedRows);
                jobRepository.save(job);
            }

            job.setStatus("COMPLETED");
            job.setCompletedAt(LocalDateTime.now());
            job.setDurationInSeconds(Duration.between(job.getStartedAt(), job.getCompletedAt()).toSeconds());
            jobRepository.save(job);

            log.info("CSV -> DB upload completed. Total rows: {}", processedRows);

        } catch (IOException e) {
            log.error("Error reading CSV file: {}", csvFilePath, e);
            job.setStatus("FAILED");
            job.setMessage(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
        }
    }

    private int countCsvRows(String csvFilePath) {
        int rows = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            while (br.readLine() != null) rows++;
        } catch (IOException e) {
            log.error("Error counting CSV rows: {}", csvFilePath, e);
        }
        return rows;
    }
}