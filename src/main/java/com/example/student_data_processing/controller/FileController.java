package com.example.student_data_processing.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final String WINDOWS_PATH =
            "C:\\var\\log\\applications\\API\\dataprocessing\\";

    private static final String LINUX_PATH =
            "/var/log/applications/API/dataprocessing/";

    private String getBasePath() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? WINDOWS_PATH : LINUX_PATH;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listFiles() {

        String basePath = getBasePath();
        File folder = new File(basePath);

        List<Map<String, Object>> files = new ArrayList<>();

        if (!folder.exists()) {
            return ResponseEntity.ok(files);
        }

        File[] fileList = folder.listFiles();

        if (fileList != null) {
            for (File file : fileList) {

                if (file.isFile()) {

                    String name = file.getName();
                    String type = "";

                    if (name.endsWith(".xlsx")) {
                        type = "excel";
                    } else if (name.endsWith(".csv")) {
                        type = "csv";
                    } else {
                        continue;
                    }

                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("name", name);
                    fileData.put("type", type);
                    fileData.put("size", file.length());
                    fileData.put("lastModified", file.lastModified());

                    files.add(fileData);
                }
            }
        }

        return ResponseEntity.ok(files);
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            // Use the correct base path for OS
            Path filePath = Paths.get(getBasePath()).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = "application/octet-stream";
            if (filename.endsWith(".csv")) contentType = "text/csv";
            if (filename.endsWith(".xlsx")) contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}