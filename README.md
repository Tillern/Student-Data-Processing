### Data Processing System - Backend
## Project Overview
A robust Spring Boot application for processing large-scale data (up to 1 million records) with asynchronous operations, job tracking, and comprehensive reporting capabilities.

### Architecture
## Technology Stack
* Java 17+ - Core programming language
* Spring Boot 3.4.5 - Application framework
* PostgreSQL - Database
* Apache POI - Excel file processing
* Spring Data JPA - Database operations
* Lombok - Boilerplate code reduction
* SpringDoc OpenAPI - API documentation


## Key Architectural Decisions
* Most of these decision are due to the large data set we are dealing with, we have to make sure we are buildng a scable system that maintains good metrics in execution time and space complexity

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


## Dependencies Used & Their Purpose
* Spring Boot Starter Web - REST APIs and MVC 
* Spring Data JPA - Database operations
* PostgreSQL Driver - Database connectivity
* Apache POI - Excel file generation and parsing 
* POI Lite - Additional Excel utilities
* Lombok - Reduce boilerplate code
* OpenCSV - CSV processing
* SpringDoc OpenAPI - Swagger UI and API docs


## Performance Optimizations
* Streaming Excel Generation: Uses SXSSFWorkbook to keep memory footprint low
* SAX Parsing for Excel: Event-based parsing prevents loading entire file
* Batch Database Inserts: 1000 records per batch
* Database Indexes: Optimized for search queries
* Async Processing: Non-blocking operations with progress tracking
* Connection Pooling: HikariCP for efficient database connections

## Security Notes
CORS configured for Angular frontend (http://localhost:4200)

