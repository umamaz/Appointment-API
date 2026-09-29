package com.umama.appointmentapi.controller;

import com.umama.appointmentapi.model.Appointment;
import com.umama.appointmentapi.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class AppointmentControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AppointmentRepository repository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private String body(String name, String email, String start, String end) {
        return """
                {
                  "customerName": "%s",
                  "customerEmail": "%s",
                  "startTime": "%s",
                  "endTime": "%s"
                }
                """.formatted(name, email, start, end);
    }

    private Long savedAppointmentId() {
        Appointment a = new Appointment();
        a.setCustomerName("Ali Khan");
        a.setCustomerEmail("ali@example.com");
        a.setStartTime(LocalDateTime.of(2026, 10, 1, 10, 0));
        a.setEndTime(LocalDateTime.of(2026, 10, 1, 11, 0));
        return repository.save(a).getId();
    }

    @Test
    void createAppointment_returns201_andBookedStatus() throws Exception {
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Ali Khan", "ali@example.com",
                                "2026-10-01T10:00:00", "2026-10-01T11:00:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("BOOKED"))
                .andExpect(jsonPath("$.customerName").value("Ali Khan"));
    }

    @Test
    void createOverlappingAppointment_returns409() throws Exception {
        savedAppointmentId();

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Sara Ahmed", "sara@example.com",
                                "2026-10-01T10:30:00", "2026-10-01T11:30:00")))
                .andExpect(status().isConflict());
    }

    @Test
    void createBackToBackAppointment_returns201() throws Exception {
        savedAppointmentId();

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Sara Ahmed", "sara@example.com",
                                "2026-10-01T11:00:00", "2026-10-01T12:00:00")))
                .andExpect(status().isCreated());
    }

    @Test
    void createWithBlankName_returns400() throws Exception {
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("", "ali@example.com",
                                "2026-10-01T10:00:00", "2026-10-01T11:00:00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithEndBeforeStart_returns400() throws Exception {
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Ali Khan", "ali@example.com",
                                "2026-10-01T11:00:00", "2026-10-01T10:00:00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithMalformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not valid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_returnsSavedAppointments() throws Exception {
        savedAppointmentId();

        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getById_returns404_whenMissing() throws Exception {
        mockMvc.perform(get("/api/appointments/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAppointment_returns200_andNewName() throws Exception {
        Long id = savedAppointmentId();

        mockMvc.perform(put("/api/appointments/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Sara Ahmed", "sara@example.com",
                                "2026-10-01T10:00:00", "2026-10-01T11:00:00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Sara Ahmed"));
    }

    @Test
    void cancelAppointment_setsStatusCancelled() throws Exception {
        Long id = savedAppointmentId();

        mockMvc.perform(patch("/api/appointments/" + id + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void filterByStatus_excludesCancelledAppointments() throws Exception {
        Long id = savedAppointmentId();
        mockMvc.perform(patch("/api/appointments/" + id + "/cancel"));

        mockMvc.perform(get("/api/appointments").param("status", "BOOKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}