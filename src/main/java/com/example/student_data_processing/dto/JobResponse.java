package com.example.student_data_processing.dto;

import com.example.student_data_processing.entity.JobStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JobResponse {

    private String jobId;
    private JobStatus status;
    private int progress;
    private String message;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}