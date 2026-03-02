package com.example.student_data_processing.repository;

import com.example.student_data_processing.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Page<Student> findByStudentId(Long studentId, Pageable pageable);

    Page<Student> findByStudentClass(String studentClass, Pageable pageable);

}
