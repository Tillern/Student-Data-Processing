package com.example.student_data_processing.repository;

import com.example.student_data_processing.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, String> {
}