package com.example.student_data_processing.controller;

import com.example.student_data_processing.dto.GenerateExcelRequest;
import com.example.student_data_processing.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    @Autowired
    private ExcelService excelService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateExcel(@RequestBody GenerateExcelRequest request) {
        excelService.generateExcelAsync(request);
        return ResponseEntity.ok("Excel generation started asynchronously. Check logs for progress.");
    }
}
