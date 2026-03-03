package com.example.student_data_processing.service;

import com.example.student_data_processing.entity.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvService {

    private final JobService jobService;

    @Async("taskExecutor")
    public void convertExcelToCsv(MultipartFile file, String jobId) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String tempExcelPath = "C:\\var\\log\\applications\\API\\dataprocessing\\temp_uploaded_" + timestamp + ".xlsx";
        String csvFilePath = "C:\\var\\log\\applications\\API\\dataprocessing\\students_" + timestamp + ".csv";

        try {
            // Save MultipartFile to temp file
            File tempFile = new File(tempExcelPath);
            file.transferTo(tempFile);

            try (OPCPackage pkg = OPCPackage.open(tempFile);
                 BufferedWriter bw = new BufferedWriter(new FileWriter(csvFilePath))) {

                XSSFReader reader = new XSSFReader(pkg);
                ReadOnlySharedStringsTable sst = new ReadOnlySharedStringsTable(pkg);
                StylesTable styles = reader.getStylesTable();
                XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) reader.getSheetsData();

                final List<String> rowData = new ArrayList<>();
                int[] processedRows = {0}; // use array to modify inside anonymous class

                while (iter.hasNext()) {
                    try (InputStream sheetStream = iter.next()) {

                        XSSFSheetXMLHandler.SheetContentsHandler handler = new XSSFSheetXMLHandler.SheetContentsHandler() {

                            @Override
                            public void startRow(int rowNum) {
                                rowData.clear();
                            }

                            @Override
                            public void endRow(int rowNum) {
                                try {
                                    if (rowData.size() == 6 && rowNum != 0) {
                                        int score = Integer.parseInt(rowData.get(5)) + 10;
                                        rowData.set(5, String.valueOf(score));
                                    }
                                    bw.write(String.join(",", rowData));
                                    bw.newLine();

                                    processedRows[0]++;
                                    if (processedRows[0] % 10000 == 0) {
                                        log.info("CSV processed {} rows", processedRows[0]);
                                        jobService.updateProgress(jobId, processedRows[0]);
                                    }


                                } catch (IOException e) {
                                    log.error("Error writing CSV row", e);
                                }
                            }

                            @Override
                            public void cell(String cellReference, String formattedValue, org.apache.poi.xssf.usermodel.XSSFComment comment) {
                                rowData.add(formattedValue);
                            }

                            @Override
                            public void headerFooter(String text, boolean isHeader, String tagName) {
                                // no-op
                            }
                        };

                        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
                        saxFactory.setNamespaceAware(true);
                        XMLReader parser = saxFactory.newSAXParser().getXMLReader();
                        parser.setContentHandler(new XSSFSheetXMLHandler(styles, sst, handler, false));
                        parser.parse(new org.xml.sax.InputSource(sheetStream));
                    }
                }

                // Job completed
                jobService.completeJob(jobId);
                log.info("CSV generation completed: {}", csvFilePath);

            }

            // Cleanup temp Excel
            if (tempFile.exists()) tempFile.delete();

        } catch (Exception e) {
            log.error("Error converting Excel to CSV", e);
            jobService.failJob(jobId, e.getMessage());
        }
    }
}