package com.example.student_data_processing.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CsvService {

    @Async("taskExecutor")
    public void convertExcelToCsv(MultipartFile file) {

        String safeFilePath = "C:\\var\\log\\applications\\API\\dataprocessing\\temp_uploaded.xlsx";
        String csvFilePath = "C:\\var\\log\\applications\\API\\dataprocessing\\students.csv";

        try {
            File tempFile = new File(safeFilePath);
            file.transferTo(tempFile);

            try (OPCPackage pkg = OPCPackage.open(tempFile);
                 FileWriter fw = new FileWriter(csvFilePath);
                 BufferedWriter bw = new BufferedWriter(fw)) {

                XSSFReader reader = new XSSFReader(pkg);
                ReadOnlySharedStringsTable sst = new ReadOnlySharedStringsTable(pkg);
                StylesTable styles = reader.getStylesTable();

                XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) reader.getSheetsData();

                while (iter.hasNext()) {
                    try (InputStream sheetStream = iter.next()) {

                        XSSFSheetXMLHandler.SheetContentsHandler handler = new XSSFSheetXMLHandler.SheetContentsHandler() {
                            final List<String> rowData = new ArrayList<>();

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
                                } catch (IOException e) {
                                    log.error("Error writing CSV row", e);
                                }
                                if (rowNum % 10000 == 0) log.info("Processed {} rows to CSV", rowNum);
                            }

                            @Override
                            public void cell(String cellReference, String formattedValue, org.apache.poi.xssf.usermodel.XSSFComment comment) {
                                rowData.add(formattedValue);
                            }

                            @Override
                            public void headerFooter(String text, boolean isHeader, String tagName) {}
                        };

                        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
                        saxFactory.setNamespaceAware(true);
                        XMLReader parser = saxFactory.newSAXParser().getXMLReader();
                        parser.setContentHandler(new XSSFSheetXMLHandler(styles, sst, handler, false));
                        parser.parse(new org.xml.sax.InputSource(sheetStream));
                    }
                }

                log.info("CSV generation completed: {}", csvFilePath);

            } catch (OpenXML4JException e) { // Only the superclass
                log.error("Invalid Excel format or OpenXML4J error", e);
            }

            if (tempFile.exists()) tempFile.delete();

        } catch (IOException | ParserConfigurationException | org.xml.sax.SAXException e) {
            log.error("Error converting Excel to CSV", e);
        }
    }
}