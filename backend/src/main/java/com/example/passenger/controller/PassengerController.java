package com.example.passenger.controller;

import com.example.passenger.model.Passenger;
import com.example.passenger.model.UploadResponse;
import com.example.passenger.service.PassengerExportService;
import com.example.passenger.service.PassengerService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/passengers")
public class PassengerController {

    private static final long MAX_FILE_SIZE = 1024L * 1024L; // 1 MB
    private static final Pattern FLIGHT_NO_PATTERN = Pattern.compile("^[A-Z0-9]{2}[0-9]{1,4}$");

    private final PassengerService passengerService;
    private final PassengerExportService exportService;

    public PassengerController(PassengerService passengerService,
                               PassengerExportService exportService) {
        this.passengerService = passengerService;
        this.exportService = exportService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestParam("flightNo") String flightNo,
                                    @RequestParam("file") MultipartFile file) {
        // 1. flight no format
        if (flightNo == null || !FLIGHT_NO_PATTERN.matcher(flightNo).matches()) {
            return ResponseEntity.badRequest().body("Invalid flight no format");
        }

        // 2. file presence
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is required");
        }

        // 3. file size
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body("File size must be less than 1 MB");
        }

        // 4. real .xlsx check (filename + content-type + magic bytes)
        if (!isXlsxFile(file)) {
            return ResponseEntity.badRequest().body("File must be .xlsx");
        }

        try {
            UploadResponse response = passengerService.processFile(file, flightNo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Cannot read Excel file: " + e.getMessage());
        }
    }

    /** Write Excel File */
    @PostMapping(value = "/export",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> export(@RequestParam("flightNo") String flightNo,
                                         @RequestBody List<Passenger> passengers) {
        if (flightNo == null || !FLIGHT_NO_PATTERN.matcher(flightNo).matches()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            byte[] bytes = exportService.toXlsx(passengers);
            String fileName = "Passenger_" + flightNo + ".xlsx";
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + fileName + "\"");
            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private boolean isXlsxFile(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".xlsx")) {
            return false;
        }
        // Verify the file really is an OOXML (zip) document: first 4 bytes must be "PK\\x03\\x04"
        try {
            byte[] head = new byte[4];
            int read = file.getInputStream().read(head);
            if (read < 4) return false;
            return head[0] == 0x50 && head[1] == 0x4B && head[2] == 0x03 && head[3] == 0x04;
        } catch (Exception e) {
            return false;
        }
    }
}
