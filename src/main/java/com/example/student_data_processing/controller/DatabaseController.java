package com.example.student_data_processing.controller;

import com.example.student_data_processing.entity.Job;
import com.example.student_data_processing.service.DatabaseService;
import com.example.student_data_processing.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/csv")
@RequiredArgsConstructor
public class DatabaseController {

    private final DatabaseService databaseService;
    private final JobService jobService;

    @Operation(
            summary = "Upload CSV to database",
            description = "Upload CSV file, add +5 to score, and insert to DB asynchronously",
            responses = {
                    @ApiResponse(responseCode = "200", description = "CSV upload started")
            }
    )
    @PostMapping("/upload-to-db")
    public ResponseEntity<String> uploadCsvToDb(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("File cannot be empty");
        if (!file.getOriginalFilename().endsWith(".csv"))
            return ResponseEntity.badRequest().body("Only .csv files are allowed");

        // Save CSV temporarily
        String timestamp = String.valueOf(System.currentTimeMillis());
        String tempCsvPath = "C:\\var\\log\\applications\\API\\dataprocessing\\db_upload_" + timestamp + ".csv";
        File tempFile = new File(tempCsvPath);
        file.transferTo(tempFile);

        // Create Job
        Job job = jobService.createJob("CSV_DB_UPLOAD", 0); // totalRecords will be set in service

        // Start async DB upload
        databaseService.uploadCsvToDatabase(tempCsvPath, job.getId());

        return ResponseEntity.ok("CSV DB upload started. Job ID: " + job.getId());
    }
}