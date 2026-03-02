package com.example.student_data_processing.controller;

import com.example.student_data_processing.service.DatabaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/csv")
public class DatabaseController {

    @Autowired
    private DatabaseService databaseService;

    @Operation(
            summary = "Upload CSV to database",
            description = "Upload a CSV file. System will read and insert student records asynchronously, adding +5 to score.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "CSV upload started")
            }
    )
    @PostMapping("/upload-to-db")
    public ResponseEntity<String> uploadCsvToDb(@RequestParam("file") MultipartFile file) throws IOException {
        // Save uploaded CSV to temp folder
        String tempCsvPath = "C:\\var\\log\\applications\\API\\dataprocessing\\temp_uploaded.csv";
        File tempFile = new File(tempCsvPath);
        file.transferTo(tempFile);

        // Trigger async upload
        databaseService.uploadCsvToDatabase(tempCsvPath);

        return ResponseEntity.ok("CSV upload to database started. Check logs for progress.");
    }
}
