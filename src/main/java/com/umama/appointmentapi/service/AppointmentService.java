package com.umama.appointmentapi.service;

import com.umama.appointmentapi.exception.AppointmentConflictException;
import com.umama.appointmentapi.exception.AppointmentNotFoundException;
import com.umama.appointmentapi.model.Appointment;
import com.umama.appointmentapi.model.AppointmentStatus;
import com.umama.appointmentapi.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository repository;

    public AppointmentService(AppointmentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Appointment create(Appointment appointment) {
        validateTimes(appointment.getStartTime(), appointment.getEndTime());
        checkConflict(appointment.getStartTime(), appointment.getEndTime(), -1L);
        appointment.setStatus(AppointmentStatus.BOOKED);
        return repository.save(appointment);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAll(AppointmentStatus status) {
        return status == null ? repository.findAll() : repository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Appointment findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    @Transactional
    public Appointment update(Long id, Appointment changes) {
        Appointment existing = findById(id);
        validateTimes(changes.getStartTime(), changes.getEndTime());
        checkConflict(changes.getStartTime(), changes.getEndTime(), id);
        existing.setCustomerName(changes.getCustomerName());
        existing.setCustomerEmail(changes.getCustomerEmail());
        existing.setStartTime(changes.getStartTime());
        existing.setEndTime(changes.getEndTime());
        return repository.save(existing);
    }

    @Transactional
    public Appointment cancel(Long id) {
        Appointment existing = findById(id);
        existing.setStatus(AppointmentStatus.CANCELLED);
        return repository.save(existing);
    }

    private void validateTimes(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    private void checkConflict(LocalDateTime start, LocalDateTime end, Long excludeId) {
        if (repository.existsConflict(AppointmentStatus.BOOKED, start, end, excludeId)) {
            throw new AppointmentConflictException(
                    "That time slot overlaps an existing appointment");
        }
    }
}