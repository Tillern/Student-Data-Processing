package com.example.student_data_processing.controller;

import com.example.student_data_processing.entity.Job;
import com.example.student_data_processing.service.CsvService;
import com.example.student_data_processing.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/csv")
@RequiredArgsConstructor
public class CsvController {

    private final CsvService csvService;
    private final JobService jobService;

    @Operation(
            summary = "Upload Excel and convert to CSV",
            description = "Uploads Excel file, converts to CSV asynchronously, and adds +10 to score",
            responses = {
                    @ApiResponse(responseCode = "200", description = "CSV conversion started")
            }
    )
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File cannot be empty");
        }

        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            return ResponseEntity.badRequest().body("Only .xlsx files are allowed");
        }

        // Create Job
        Job job = jobService.createJob("CSV_PROCESSING", 0);

        // Start async processing
        csvService.convertExcelToCsv(file, job.getId());

        return ResponseEntity.ok(
                "CSV processing started successfully. Job ID: " + job.getId()
        );
    }


}