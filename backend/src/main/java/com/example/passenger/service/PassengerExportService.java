package com.example.passenger.service;

import com.example.passenger.model.Passenger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
public class PassengerExportService {

    /** Parses the app's display format (dd-MM-yyyy) back to a LocalDate. */
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public byte[] toXlsx(List<Passenger> passengers) throws IOException {
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Passengers");

            // Header row
            Row header = sheet.createRow(0);
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] columns = {"First name", "Last name", "Gender", "Date of birth", "Nationality"};
            for (int i = 0; i < columns.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(columns[i]);
                c.setCellStyle(headerStyle);
            }

            // Date format style (dd/MM/yyyy) reused across every date cell
            CellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(
                    wb.getCreationHelper().createDataFormat().getFormat("dd/MM/yyyy"));

            // Data rows
            for (int i = 0; i < passengers.size(); i++) {
                Passenger p = passengers.get(i);
                Row row = sheet.createRow(i + 1);

                row.createCell(0).setCellValue(safe(p.getFirstName()));
                row.createCell(1).setCellValue(safe(p.getLastName()));
                row.createCell(2).setCellValue(safe(p.getGender()));

                Cell dateCell = row.createCell(3);
                LocalDate dob = tryParse(p.getDateOfBirth());
                if (dob != null) {
                    dateCell.setCellValue(toDate(dob));
                    dateCell.setCellStyle(dateStyle);
                } else {
                    dateCell.setCellValue(safe(p.getDateOfBirth()));
                }

                row.createCell(4).setCellValue(safe(p.getNationality()));
            }

            // Auto-size each column so the file is nice to open
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(out);
            return out.toByteArray();
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static LocalDate tryParse(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s, DISPLAY_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    private static Date toDate(LocalDate ld) {
        return Date.from(ld.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
    }
}
