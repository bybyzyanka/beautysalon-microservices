package ua.nure.beautysalon.master.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FacilityDTO {
    private Long id;
    private String name;
    private Double price;
}