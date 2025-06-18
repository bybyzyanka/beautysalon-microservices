package ua.nure.beautysalon.schedule.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "scheduled_facilities")
public class ScheduledFacility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Appointment date is required")
    @Column(nullable = false)
    private LocalDateTime date;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    @Max(value = 1440, message = "Duration cannot exceed 24 hours (1440 minutes)")
    @Column(nullable = false)
    private Long duration;

    @NotNull(message = "Facility is required")
    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @NotNull(message = "Client is required")
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @NotNull(message = "Master is required")
    @Column(name = "master_id", nullable = false)
    private Long masterId;
}