package com.example.student_data_processing.controller;

import com.example.student_data_processing.dto.GenerateExcelRequest;
import com.example.student_data_processing.entity.Job;
import com.example.student_data_processing.service.ExcelService;
import com.example.student_data_processing.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    @Autowired
    private ExcelService excelService;

    @Autowired
    private JobService jobService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateExcel(@RequestBody GenerateExcelRequest request) {

        Job job = jobService.createJob("EXCEL_GENERATION", request.getNumberOfRecords());

        excelService.generateExcelAsync(request, job.getId());

        return ResponseEntity.ok(job);
    }

    @GetMapping("/job/{id}")
    public ResponseEntity<Job> getJobStatus(@PathVariable String id) {

        Job job = jobService.getJobById(id);

        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(job);
    }
}
