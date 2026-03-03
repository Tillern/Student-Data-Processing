package com.example.student_data_processing.controller;

import com.example.student_data_processing.entity.Student;
import com.example.student_data_processing.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/students/report")
@RequiredArgsConstructor
public class ReportController {

    private final StudentRepository studentRepository;

    /**
     * Export student data as CSV or XLSX with optional filtering by studentId or studentClass
     * Supports pagination
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String studentClass,
            @RequestParam(defaultValue = "xlsx") String format, // csv or xlsx
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size
    ) throws IOException {

        // Step 1: Create pageable for pagination
        Pageable pageable = PageRequest.of(page, size);
        Page<Student> studentsPage;

        // Step 2: Fetch filtered students
        if (studentId != null) {
            studentsPage = studentRepository.findByStudentId(studentId, pageable);
        } else if (studentClass != null && !studentClass.isEmpty()) {
            studentsPage = studentRepository.findByStudentClass(studentClass, pageable);
        } else {
            studentsPage = studentRepository.findAll(pageable);
        }

        List<Student> students = studentsPage.getContent();

        // Step 3: Generate CSV or Excel
        byte[] data;
        if ("csv".equalsIgnoreCase(format)) {
            data = generateCsv(students);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students.csv")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(data);
        } else { // default XLSX
            data = generateExcel(students);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        }
    }

    /**
     * Convert list of students to CSV bytes
     */
    private byte[] generateCsv(List<Student> students) {
        StringBuilder sb = new StringBuilder();
        sb.append("studentId,firstName,lastName,dob,class,score\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Student s : students) {
            sb.append(s.getStudentId()).append(",")
                    .append(s.getFirstName()).append(",")
                    .append(s.getLastName()).append(",")
                    .append(s.getDob() != null ? s.getDob().format(formatter) : "").append(",")
                    .append(s.getStudentClass()).append(",")
                    .append(s.getScore())
                    .append("\n");
        }
        return sb.toString().getBytes();
    }

    /**
     * Convert list of students to XLSX bytes
     */
    private byte[] generateExcel(List<Student> students) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");

        // Header row
        Row header = sheet.createRow(0);
        String[] columns = {"studentId","firstName","lastName","dob","class","score"};
        for (int i = 0; i < columns.length; i++) {
            header.createCell(i).setCellValue(columns[i]);
        }

        // Data rows
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int rowIdx = 1;
        for (Student s : students) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(s.getStudentId());
            row.createCell(1).setCellValue(s.getFirstName());
            row.createCell(2).setCellValue(s.getLastName());
            row.createCell(3).setCellValue(s.getDob() != null ? s.getDob().format(formatter) : "");
            row.createCell(4).setCellValue(s.getStudentClass());
            row.createCell(5).setCellValue(s.getScore());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }

    @GetMapping("/list")
    public ResponseEntity<?> listStudents(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String studentClass,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Student> studentsPage;

        if (studentId != null) {
            studentsPage = studentRepository.findByStudentId(studentId, pageable);
        } else if (studentClass != null && !studentClass.isEmpty()) {
            studentsPage = studentRepository.findByStudentClass(studentClass, pageable);
        } else {
            studentsPage = studentRepository.findAll(pageable);
        }

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(studentsPage.getTotalElements()))
                .body(studentsPage.getContent());
    }
}