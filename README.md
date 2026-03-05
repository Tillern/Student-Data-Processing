Data Processing System - Backend
Project Overview
A robust Spring Boot application for processing large-scale data (up to 1 million records) with asynchronous operations, job tracking, and comprehensive reporting capabilities.

Architecture
Technology Stack
Java 17+ - Core programming language
Spring Boot 3.4.5 - Application framework
PostgreSQL - Database
Apache POI - Excel file processing
Spring Data JPA - Database operations
Lombok - Boilerplate code reduction
SpringDoc OpenAPI - API documentation


## Key Architectural Decisions
Most of these decision are due to the large data set we are dealing with, we have to make sure we are buildng a scable system that maintains good metrics in execution time and space complexity

1. Asynchronous Processing with Job Tracking
All long-running operations (Excel generation, file conversion, DB uploads) are handled asynchronously to pevent HTTP request timeouts when processing large data sets like a million records

we have:

@Async annotation with custom ThreadPoolTaskExecutor
Job entity tracks progress, status, and timing
Client polls job status endpoints

2. Memory-Efficient Excel Processing
Used streaming APIs for Excel operations because Standard Excel libraries would cause OutOfMemoryError with 1M records

We have:
SXSSFWorkbook for generation (streaming, keeps only 100 rows in memory)
SAX parsing with XSSFSheetXMLHandler for reading (event-based)

3. Batch Database Operations
We insert data from csv to database in 1000 records batches because this helps reduce database round-trips and improves performance

we have:
hibernate.jdbc.batch_size=1000 in our aplication properties file
saveAll() with batched collections

4. Indexed Database Queries
Added database indexes on queried columns so as to optimize search performance on large datasets

5. OS-Aware File Paths
Dynamic path resolution based on operating system(where the generated Excel and processed CSV are stored) to bring about compatibility across Windows and Linux environments


### Dependencies Used & Their Purpose
* Spring Boot Starter Web - REST APIs and MVC 
* Spring Data JPA - Database operations
* PostgreSQL Driver - Database connectivity
* Apache POI - Excel file generation and parsing 
* POI Lite - Additional Excel utilities
* Lombok - Reduce boilerplate code
* OpenCSV - CSV processing
* SpringDoc OpenAPI - Swagger UI and API docs


### API Endpoints

Excel Generation
Method	Endpoint	Description	Request Body
POST	/api/excel/generate	Start Excel file generation with specified records	{ "numberOfRecords": 1000000 }
GET	/api/excel/job/{id}	Check status of Excel generation job	-
CSV Processing
Method	Endpoint	Description	Parameters
POST	/api/csv/upload	Upload Excel file and convert to CSV (+10 to score)	file (MultipartFile .xlsx)
POST	/api/csv/upload-to-db	Upload CSV file and save to database (+5 to score)	file (MultipartFile .csv)
Reports & Queries
Method	Endpoint	Description	Parameters
GET	/api/students/report/export	Export student data	studentId (optional), studentClass (optional), format (csv/xlsx), page, size
GET	/api/students/report/list	Get paginated student list	studentId, studentClass, page, size
GET	/api/students/by-id	Search students by ID	studentId, page, size
GET	/api/students/by-class	Filter students by class	studentClass, page, size
File Management
Method	Endpoint	Description
GET	/api/files	List all generated Excel and CSV files
GET	/api/files/download/{filename}	Download a specific file
Job Monitoring
Method	Endpoint	Description
GET	/api/jobs/{jobId}	Get status of any async job
💾 Database Schema
Student Table
sql
CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    dob DATE,
    student_class VARCHAR(255),
    score INTEGER
);
CREATE INDEX idx_student_id ON students(student_id);
CREATE INDEX idx_class ON students(student_class);
Job Table
sql
CREATE TABLE job (
    id VARCHAR(255) PRIMARY KEY,
    type VARCHAR(255),
    status VARCHAR(255),
    progress INTEGER,
    total_records BIGINT,
    processed_records BIGINT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    duration_in_seconds BIGINT,
    message TEXT
);
⚙️ Configuration Highlights
Application Properties
properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/studentdb
spring.datasource.username=postgres
spring.datasource.password=password

# JPA Optimizations
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.jdbc.batch_size=1000
spring.jpa.properties.hibernate.order_inserts=true

# File Upload Limits
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB

# Server
server.port=8080
Async Configuration
java
@Bean(name = "taskExecutor")
public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("Async-");
    return executor;
}


## Performance Optimizations
Streaming Excel Generation: Uses SXSSFWorkbook to keep memory footprint low
SAX Parsing for Excel: Event-based parsing prevents loading entire file
Batch Database Inserts: 1000 records per batch
Database Indexes: Optimized for search queries
Async Processing: Non-blocking operations with progress tracking
Connection Pooling: HikariCP for efficient database connections

Sample API Responses
Job Creation Response
json
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "type": "EXCEL_GENERATION",
    "status": "STARTED",
    "progress": 0,
    "totalRecords": 1000000,
    "processedRecords": 0,
    "startedAt": "2024-01-15T10:30:00",
    "completedAt": null,
    "durationInSeconds": 0,
    "message": null
}

Job Status Response
json
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "type": "EXCEL_GENERATION",
    "status": "PROCESSING",
    "progress": 45,
    "totalRecords": 1000000,
    "processedRecords": 450000,
    "startedAt": "2024-01-15T10:30:00",
    "completedAt": null,
    "durationInSeconds": 125,
    "message": null
}


Security Notes
CORS configured for Angular frontend (http://localhost:4200)

