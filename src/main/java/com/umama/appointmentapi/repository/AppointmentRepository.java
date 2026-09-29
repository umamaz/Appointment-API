package com.umama.appointmentapi.repository;

import com.umama.appointmentapi.model.Appointment;
import com.umama.appointmentapi.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByStatus(AppointmentStatus status);

    @Query("""
            SELECT COUNT(a) > 0 FROM Appointment a
            WHERE a.status = :status
              AND a.startTime < :endTime
              AND a.endTime > :startTime
              AND a.id <> :excludeId
            """)
    boolean existsConflict(@Param("status") AppointmentStatus status,
                           @Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime,
                           @Param("excludeId") Long excludeId);
}