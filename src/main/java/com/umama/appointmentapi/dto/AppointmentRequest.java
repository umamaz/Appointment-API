package com.umama.appointmentapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotBlank String customerName,
        @NotBlank @Email String customerEmail,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime) {
}