package com.example.passenger.model;

import java.util.ArrayList;
import java.util.List;

public class UploadResponse {

    private boolean success;
    private String flightNo;
    private String fileName;
    private List<Passenger> passengers = new ArrayList<>();
    private List<RowError> errors = new ArrayList<>();

    public UploadResponse() {
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getFlightNo() { return flightNo; }
    public void setFlightNo(String flightNo) { this.flightNo = flightNo; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public List<Passenger> getPassengers() { return passengers; }
    public void setPassengers(List<Passenger> passengers) { this.passengers = passengers; }

    public List<RowError> getErrors() { return errors; }
    public void setErrors(List<RowError> errors) { this.errors = errors; }

    public static class RowError {
        private int row;
        private List<String> invalidColumns = new ArrayList<>();

        public RowError() {
        }

        public RowError(int row, List<String> invalidColumns) {
            this.row = row;
            this.invalidColumns = invalidColumns;
        }

        public int getRow() { return row; }
        public void setRow(int row) { this.row = row; }

        public List<String> getInvalidColumns() { return invalidColumns; }
        public void setInvalidColumns(List<String> invalidColumns) { this.invalidColumns = invalidColumns; }
    }
}
