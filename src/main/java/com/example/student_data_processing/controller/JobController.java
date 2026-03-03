package com.example.student_data_processing.controller;

import com.example.student_data_processing.entity.Job;
import com.example.student_data_processing.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobRepository jobRepository;

    @GetMapping("/{jobId}")
    public Job getJobStatus(@PathVariable String jobId) {
        return jobRepository.findById(jobId).orElseThrow();
    }

}