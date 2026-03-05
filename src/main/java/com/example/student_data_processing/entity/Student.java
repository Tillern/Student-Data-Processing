package com.example.student_data_processing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "students",
        indexes = {
                @Index(name = "idx_student_id", columnList = "studentId"),
                @Index(name = "idx_class", columnList = "studentClass")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, unique = true)
    private String studentId;

    private String firstName;

    private String lastName;

    private LocalDate dob;

    @Column(name = "studentClass")
    private String studentClass;

    private Integer score;
}
