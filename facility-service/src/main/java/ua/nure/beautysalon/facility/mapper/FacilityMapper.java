package ua.nure.beautysalon.facility.mapper;

import org.springframework.stereotype.Component;
import ua.nure.beautysalon.facility.dto.FacilityDTO;
import ua.nure.beautysalon.facility.entity.Facility;

@Component
public class FacilityMapper {

    public FacilityDTO toDTO(Facility facility) {
        FacilityDTO facilityDTO = new FacilityDTO();
        facilityDTO.setId(facility.getId());
        facilityDTO.setName(facility.getName());
        facilityDTO.setPrice(facility.getPrice());
        return facilityDTO;
    }

    public Facility toEntity(FacilityDTO facilityDTO) {
        Facility facility = new Facility();
        facility.setId(facilityDTO.getId());
        facility.setName(facilityDTO.getName());
        facility.setPrice(facilityDTO.getPrice());
        return facility;
    }
}