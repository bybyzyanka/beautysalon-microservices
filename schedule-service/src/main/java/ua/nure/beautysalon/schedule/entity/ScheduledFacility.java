package ua.nure.beautysalon.schedule.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Date cannot be null")
    @Column(nullable = false)
    private LocalDateTime date;

    @NotNull(message = "Duration cannot be null")
    @Column(nullable = false)
    private Long duration;

    @NotNull(message = "Facility cannot be null")
    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @NotNull(message = "Client cannot be null")
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @NotNull(message = "Master cannot be null")
    @Column(name = "master_id", nullable = false)
    private Long masterId;
}