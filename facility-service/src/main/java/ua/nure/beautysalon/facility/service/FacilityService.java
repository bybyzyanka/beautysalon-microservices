package ua.nure.beautysalon.facility.service;

import org.springframework.data.domain.Page;
import ua.nure.beautysalon.facility.dto.FacilityDTO;

import java.util.List;
import java.util.Optional;

public interface FacilityService {
    FacilityDTO addFacility(FacilityDTO facilityDTO);
    void deleteFacilityById(Long id);
    Optional<FacilityDTO> updateFacility(Long id, FacilityDTO facilityDTO);
    Optional<FacilityDTO> getFacilityById(Long id);
    Page<FacilityDTO> getAllFacilities(int page, int size);
    Page<FacilityDTO> searchFacilitiesByName(String searchTerm, int page, int size);
    List<FacilityDTO> getFacilitiesByIds(List<Long> ids);
}
