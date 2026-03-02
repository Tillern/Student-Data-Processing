package com.example.student_data_processing.controller;

import com.example.student_data_processing.service.CsvService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/csv")
public class CsvController {

    @Autowired
    private CsvService csvService;

    @Operation(
            summary = "Upload Excel and convert to CSV",
            description = "Upload an Excel file. System will convert it to CSV asynchronously, adding 10 to each score.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "CSV generation started")
            }
    )
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        csvService.convertExcelToCsv(file);
        return ResponseEntity.ok("CSV generation started. Progress on logs.");
    }
}
