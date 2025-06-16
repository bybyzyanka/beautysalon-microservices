package ua.nure.beautysalon.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledFacilityDTO {
    private Long id;
    private LocalDateTime date;
    private Long duration;
    private FacilityDTO facility;
    private ClientDTO client;
    private MasterDTO master;
}
