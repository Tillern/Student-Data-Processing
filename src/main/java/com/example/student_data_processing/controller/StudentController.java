package com.example.student_data_processing.controller;

import com.example.student_data_processing.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/by-id")
    public ResponseEntity<?> getByStudentId(
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        var result = studentRepository.findByStudentId(studentId, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-class")
    public ResponseEntity<?> getByClass(
            @RequestParam String studentClass,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        var result = studentRepository.findByStudentClass(studentClass, pageable);
        return ResponseEntity.ok(result);
    }
}
