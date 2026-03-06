package com.example.student_data_processing.repository;

import com.example.student_data_processing.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByStudentId(String studentId);

    Page<Student> findByStudentId(String studentId, Pageable pageable);

    Page<Student> findByStudentClass(String studentClass, Pageable pageable);

    @Query("SELECT s.studentId FROM Student s")
    List<String> findAllStudentIds();
}