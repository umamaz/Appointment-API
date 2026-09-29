package com.umama.appointmentapi.service;

import com.umama.appointmentapi.exception.AppointmentConflictException;
import com.umama.appointmentapi.exception.AppointmentNotFoundException;
import com.umama.appointmentapi.model.Appointment;
import com.umama.appointmentapi.model.AppointmentStatus;
import com.umama.appointmentapi.repository.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository repository;

    @InjectMocks
    private AppointmentService service;

    private final LocalDateTime start = LocalDateTime.of(2026, 10, 1, 10, 0);
    private final LocalDateTime end = LocalDateTime.of(2026, 10, 1, 11, 0);

    private Appointment appointment(Long id, LocalDateTime s, LocalDateTime e) {
        Appointment a = new Appointment();
        a.setId(id);
        a.setCustomerName("Ali Khan");
        a.setCustomerEmail("ali@example.com");
        a.setStartTime(s);
        a.setEndTime(e);
        return a;
    }

    @Test
    void create_savesAppointment_whenNoConflict() {
        when(repository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment saved = service.create(appointment(null, start, end));

        assertEquals(AppointmentStatus.BOOKED, saved.getStatus());
        verify(repository).save(any(Appointment.class));
    }

    @Test
    void create_throwsConflict_whenSlotOverlaps() {
        when(repository.existsConflict(eq(AppointmentStatus.BOOKED), any(), any(), eq(-1L)))
                .thenReturn(true);

        assertThrows(AppointmentConflictException.class,
                () -> service.create(appointment(null, start, end)));
        verify(repository, never()).save(any());
    }

    @Test
    void create_throwsIllegalArgument_whenEndBeforeStart() {
        assertThrows(IllegalArgumentException.class,
                () -> service.create(appointment(null, end, start)));
        verify(repository, never()).save(any());
    }

    @Test
    void create_throwsIllegalArgument_whenEndEqualsStart() {
        assertThrows(IllegalArgumentException.class,
                () -> service.create(appointment(null, start, start)));
    }

    @Test
    void findById_returnsAppointment_whenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(appointment(1L, start, end)));

        assertEquals(1L, service.findById(1L).getId());
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AppointmentNotFoundException.class, () -> service.findById(999L));
    }

    @Test
    void findAll_withoutStatus_returnsEverything() {
        when(repository.findAll()).thenReturn(List.of(appointment(1L, start, end)));

        assertEquals(1, service.findAll(null).size());
        verify(repository, never()).findByStatus(any());
    }

    @Test
    void findAll_withStatus_filtersByStatus() {
        when(repository.findByStatus(AppointmentStatus.CANCELLED)).thenReturn(List.of());

        assertTrue(service.findAll(AppointmentStatus.CANCELLED).isEmpty());
        verify(repository, never()).findAll();
    }

    @Test
    void update_changesFields_whenNoConflict() {
        Appointment existing = appointment(1L, start, end);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment changes = appointment(null, start.plusHours(2), end.plusHours(2));
        changes.setCustomerName("Sara Ahmed");

        Appointment result = service.update(1L, changes);

        assertEquals("Sara Ahmed", result.getCustomerName());
        assertEquals(start.plusHours(2), result.getStartTime());
    }

    @Test
    void update_throwsConflict_whenNewSlotOverlaps() {
        when(repository.findById(1L)).thenReturn(Optional.of(appointment(1L, start, end)));
        when(repository.existsConflict(eq(AppointmentStatus.BOOKED), any(), any(), eq(1L)))
                .thenReturn(true);

        assertThrows(AppointmentConflictException.class,
                () -> service.update(1L, appointment(null, start, end)));
        verify(repository, never()).save(any());
    }

    @Test
    void cancel_setsStatusToCancelled() {
        when(repository.findById(1L)).thenReturn(Optional.of(appointment(1L, start, end)));
        when(repository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(AppointmentStatus.CANCELLED, service.cancel(1L).getStatus());
    }

    @Test
    void cancel_throwsNotFound_whenMissing() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AppointmentNotFoundException.class, () -> service.cancel(999L));
    }
}