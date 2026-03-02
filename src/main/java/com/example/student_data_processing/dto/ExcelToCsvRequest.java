package com.example.student_data_processing.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ExcelToCsvRequest {
    private MultipartFile file; // uploaded Excel
}