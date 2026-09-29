package com.umama.appointmentapi.dto;

import com.umama.appointmentapi.model.Appointment;
import com.umama.appointmentapi.model.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        String customerName,
        String customerEmail,
        LocalDateTime startTime,
        LocalDateTime endTime,
        AppointmentStatus status) {

    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(a.getId(), a.getCustomerName(), a.getCustomerEmail(),
                a.getStartTime(), a.getEndTime(), a.getStatus());
    }
}