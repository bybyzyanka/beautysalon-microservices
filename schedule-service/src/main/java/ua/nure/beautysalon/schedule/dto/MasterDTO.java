package ua.nure.beautysalon.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MasterDTO {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private List<FacilityDTO> facilities;
}