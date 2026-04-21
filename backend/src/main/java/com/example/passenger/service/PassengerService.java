package com.example.passenger.service;

import com.example.passenger.model.Passenger;
import com.example.passenger.model.UploadResponse;
import com.example.passenger.util.StrictOoxmlConverter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class PassengerService {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z]{1,20}$");
    private static final Pattern NATIONALITY_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private static final Set<String> ALLOWED_GENDERS = Set.of("Male", "Female", "Unknown");

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public UploadResponse processFile(MultipartFile file, String flightNo) throws IOException {
        UploadResponse response = new UploadResponse();
        response.setFlightNo(flightNo);
        response.setFileName(file.getOriginalFilename());

        List<Passenger> passengers = new ArrayList<>();
        List<UploadResponse.RowError> errors = new ArrayList<>();

        try (InputStream raw = file.getInputStream();
             InputStream is = StrictOoxmlConverter.toTransitional(raw);
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Row 0 = header. Data starts at row index 1 (Excel row 2).
            int lastRow = sheet.getLastRowNum();
            int personIndex = 0; // 1-based person counter for error messages

            for (int rowIdx = 1; rowIdx <= lastRow; rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }
                personIndex++;

                String firstName = getStringCell(row.getCell(0));
                String lastName = getStringCell(row.getCell(1));
                String gender = getStringCell(row.getCell(2));
                Cell dobCell = row.getCell(3);
                String nationality = getStringCell(row.getCell(4));

                List<String> invalidCols = new ArrayList<>();

                if (!isValidName(firstName)) {
                    invalidCols.add("First name");
                }
                if (!isValidName(lastName)) {
                    invalidCols.add("Last name");
                }
                if (!isValidGender(gender)) {
                    invalidCols.add("Gender");
                }

                LocalDate dob = parseDate(dobCell);
                if (!isValidDateOfBirth(dob)) {
                    invalidCols.add("Date of birth");
                }
                if (!isValidNationality(nationality)) {
                    invalidCols.add("Nationality");
                }

                if (!invalidCols.isEmpty()) {
                    errors.add(new UploadResponse.RowError(personIndex, invalidCols));
                } else {
                    Passenger p = new Passenger(
                            firstName,
                            lastName,
                            gender,
                            dob.format(DISPLAY_FORMATTER),
                            nationality
                    );
                    passengers.add(p);
                }
            }
        }

        if (errors.isEmpty()) {
            response.setSuccess(true);
            response.setPassengers(passengers);
        } else {
            response.setSuccess(false);
            response.setErrors(errors);
        }
        return response;
    }

    // -------------- Validation helpers --------------

    public boolean isValidName(String value) {
        return value != null && NAME_PATTERN.matcher(value).matches();
    }

    public boolean isValidGender(String value) {
        return value != null && ALLOWED_GENDERS.contains(value);
    }

    public boolean isValidNationality(String value) {
        return value != null && NATIONALITY_PATTERN.matcher(value).matches();
    }

    public boolean isValidDateOfBirth(LocalDate dob) {
        if (dob == null) return false;
        LocalDate today = LocalDate.now();
        return !dob.isAfter(today);
    }

    // -------------- POI cell helpers --------------

    private boolean isRowEmpty(Row row) {
        for (int c = 0; c < 5; c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String v = getStringCell(cell);
                if (v != null && !v.isBlank()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getStringCell(Cell cell) {
        if (cell == null) return null;
        DataFormatter fmt = new DataFormatter();
        switch (cell.getCellType()) {
            case STRING:
                String s = cell.getStringCellValue();
                return s == null ? null : s.trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Numeric date — not what we want for strings; let caller handle.
                    return fmt.formatCellValue(cell);
                }
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return fmt.formatCellValue(cell);
                }
            default:
                return null;
        }
    }

    LocalDate parseDate(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                Date d = cell.getDateCellValue();
                if (d == null) return null;
                return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            String raw = getStringCell(cell);
            if (raw == null || raw.isBlank()) return null;

            // Accept dd/MM/yyyy, dd-MM-yyyy, yyyy-MM-dd
            for (DateTimeFormatter fmt : new DateTimeFormatter[]{
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                    DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
            }) {
                try {
                    return LocalDate.parse(raw, fmt);
                } catch (Exception ignored) {
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
