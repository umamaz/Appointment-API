package com.umama.appointmentapi.controller;

import com.umama.appointmentapi.dto.AppointmentRequest;
import com.umama.appointmentapi.dto.AppointmentResponse;
import com.umama.appointmentapi.model.Appointment;
import com.umama.appointmentapi.model.AppointmentStatus;
import com.umama.appointmentapi.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse create(@Valid @RequestBody AppointmentRequest request) {
        return AppointmentResponse.from(service.create(toEntity(request)));
    }

    @GetMapping
    public List<AppointmentResponse> getAll(@RequestParam(required = false) AppointmentStatus status) {
        return service.findAll(status).stream().map(AppointmentResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AppointmentResponse getById(@PathVariable Long id) {
        return AppointmentResponse.from(service.findById(id));
    }

    @PutMapping("/{id}")
    public AppointmentResponse update(@PathVariable Long id,
                                      @Valid @RequestBody AppointmentRequest request) {
        return AppointmentResponse.from(service.update(id, toEntity(request)));
    }

    @PatchMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable Long id) {
        return AppointmentResponse.from(service.cancel(id));
    }

    private Appointment toEntity(AppointmentRequest r) {
        Appointment a = new Appointment();
        a.setCustomerName(r.customerName());
        a.setCustomerEmail(r.customerEmail());
        a.setStartTime(r.startTime());
        a.setEndTime(r.endTime());
        return a;
    }
}