package com.example.student_data_processing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String type; // EXCEL_GENERATION, CSV_PROCESSING, DB_UPLOAD

    private String status; // STARTED, PROCESSING, COMPLETED, FAILED

    private int progress; // 0 - 100

    private long totalRecords;

    private long processedRecords;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private long durationInSeconds;

    private String message;
}