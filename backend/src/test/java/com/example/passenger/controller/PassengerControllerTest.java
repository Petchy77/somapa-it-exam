package com.example.passenger.controller;

import com.example.passenger.service.PassengerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class PassengerControllerTest {

    @Autowired
    private WebApplicationContext webContext;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webContext).build();
    }

    @Test
    void invalidFlightNoReturns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "Passenger_TG129.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{0x50, 0x4B, 0x03, 0x04});
        mockMvc.perform(multipart("/api/passengers/upload")
                        .file(file)
                        .param("flightNo", "TG&128")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonXlsxReturns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "Passenger_TG129.csv",
                "text/csv",
                "col1,col2".getBytes());
        mockMvc.perform(multipart("/api/passengers/upload")
                        .file(file)
                        .param("flightNo", "TG129")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }
}
