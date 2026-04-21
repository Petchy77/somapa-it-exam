package com.example.passenger.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PassengerServiceTest {

    private final PassengerService service = new PassengerService();

    // -------- First/Last name --------

    @Test
    void validName_acceptsAlphaOnlyAndUpTo20Chars() {
        assertTrue(service.isValidName("Wilbur"));
        assertTrue(service.isValidName("a"));
        assertTrue(service.isValidName("TwentyCharacterssss")); // 19 letters
        assertTrue(service.isValidName("abcdefghijklmnopqrst")); // exactly 20
    }

    @Test
    void validName_rejectsDigitsSpacesSymbolsAndOverLength() {
        assertFalse(service.isValidName(null));
        assertFalse(service.isValidName(""));
        assertFalse(service.isValidName("John1"));
        assertFalse(service.isValidName("L'Oreal"));
        assertFalse(service.isValidName("John Doe"));
        assertFalse(service.isValidName("abcdefghijklmnopqrstu")); // 21 letters
    }

    // -------- Gender --------

    @Test
    void validGender_acceptsOnlyThreeValues() {
        assertTrue(service.isValidGender("Male"));
        assertTrue(service.isValidGender("Female"));
        assertTrue(service.isValidGender("Unknown"));
    }

    @Test
    void validGender_rejectsOtherValues() {
        assertFalse(service.isValidGender(null));
        assertFalse(service.isValidGender(""));
        assertFalse(service.isValidGender("male"));   // case matters
        assertFalse(service.isValidGender("MALE"));
        assertFalse(service.isValidGender("Other"));
    }

    // -------- Nationality --------

    @Test
    void validNationality_acceptsThreeUppercaseLetters() {
        assertTrue(service.isValidNationality("USA"));
        assertTrue(service.isValidNationality("FRA"));
        assertTrue(service.isValidNationality("DEU"));
    }

    @Test
    void validNationality_rejectsWrongLengthOrLowercaseOrDigits() {
        assertFalse(service.isValidNationality(null));
        assertFalse(service.isValidNationality(""));
        assertFalse(service.isValidNationality("US"));
        assertFalse(service.isValidNationality("USAA"));
        assertFalse(service.isValidNationality("usa"));
        assertFalse(service.isValidNationality("US1"));
        assertFalse(service.isValidNationality("xyz"));
    }

    // -------- Date of birth --------

    @Test
    void validDateOfBirth_acceptsPastAndToday() {
        assertTrue(service.isValidDateOfBirth(LocalDate.of(1988, 12, 31)));
        assertTrue(service.isValidDateOfBirth(LocalDate.now()));
    }

    @Test
    void validDateOfBirth_rejectsFutureAndNull() {
        assertFalse(service.isValidDateOfBirth(null));
        assertFalse(service.isValidDateOfBirth(LocalDate.now().plusDays(1)));
    }
}
