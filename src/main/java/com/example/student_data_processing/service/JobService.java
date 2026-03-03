package com.example.student_data_processing.service;

import com.example.student_data_processing.entity.Job;
import com.example.student_data_processing.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    public Job createJob(String type, long totalRecords) {
        Job job = Job.builder()
                .type(type)
                .status("STARTED")
                .progress(0)
                .totalRecords(totalRecords)
                .processedRecords(0)
                .startedAt(LocalDateTime.now())
                .build();

        return jobRepository.save(job);
    }

    public void updateProgress(String jobId, long processedRecords) {
        Job job = jobRepository.findById(jobId).orElseThrow();
        job.setProcessedRecords(processedRecords);

        if (job.getTotalRecords() > 0) {
            int percent = (int) ((processedRecords * 100) / job.getTotalRecords());
            job.setProgress(percent);
        }

        job.setStatus("PROCESSING");
        jobRepository.save(job);
    }

    public void completeJob(String jobId) {
        Job job = jobRepository.findById(jobId).orElseThrow();
        job.setStatus("COMPLETED");
        job.setProgress(100);
        job.setCompletedAt(LocalDateTime.now());

        long duration = Duration.between(job.getStartedAt(), job.getCompletedAt()).toSeconds();
        job.setDurationInSeconds(duration);

        jobRepository.save(job);
    }

    public void failJob(String jobId, String message) {
        Job job = jobRepository.findById(jobId).orElseThrow();
        job.setStatus("FAILED");
        job.setMessage(message);
        job.setCompletedAt(LocalDateTime.now());
        jobRepository.save(job);
    }

    public Job getJobById(String jobId) {
        return jobRepository.findById(jobId).orElse(null);
    }
}