package ua.nure.beautysalon.facility.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nure.beautysalon.facility.dto.FacilityDTO;
import ua.nure.beautysalon.facility.entity.Facility;
import ua.nure.beautysalon.facility.feign.ScheduleServiceClient;
import ua.nure.beautysalon.facility.mapper.FacilityMapper;
import ua.nure.beautysalon.facility.repository.FacilityRepository;
import ua.nure.beautysalon.facility.service.FacilityService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;
    private final FacilityMapper facilityMapper;
    private final ScheduleServiceClient scheduleServiceClient;

    @Override
    public FacilityDTO addFacility(FacilityDTO facilityDTO) {
        if (facilityRepository.existsByNameIgnoreCase(facilityDTO.getName())) {
            throw new IllegalArgumentException("Facility with this name already exists");
        }

        Facility facilityEntity = facilityMapper.toEntity(facilityDTO);
        Facility savedFacility = facilityRepository.save(facilityEntity);
        return facilityMapper.toDTO(savedFacility);
    }

    @Override
    public void deleteFacilityById(Long id) {
        try {
            scheduleServiceClient.deleteByFacilityId(id);
        } catch (Exception e) {
            // Log error but continue with deletion
        }
        facilityRepository.deleteById(id);
    }

    @Override
    public Optional<FacilityDTO> updateFacility(Long id, FacilityDTO facilityDTO) {
        return facilityRepository.findById(id).map(existingFacility -> {
            existingFacility.setName(facilityDTO.getName());
            existingFacility.setPrice(facilityDTO.getPrice());

            facilityRepository.save(existingFacility);
            return facilityMapper.toDTO(existingFacility);
        });
    }

    @Override
    public Optional<FacilityDTO> getFacilityById(Long id) {
        return facilityRepository.findById(id)
                .map(facilityMapper::toDTO);
    }

    @Override
    public Page<FacilityDTO> getAllFacilities(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return facilityRepository.findAll(pageable)
                .map(facilityMapper::toDTO);
    }

    @Override
    public Page<FacilityDTO> searchFacilitiesByName(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return facilityRepository.findByNameContainingIgnoreCase(searchTerm, pageable)
                .map(facilityMapper::toDTO);
    }

    @Override
    public List<FacilityDTO> getFacilitiesByIds(List<Long> ids) {
        return facilityRepository.findAllById(ids).stream()
                .map(facilityMapper::toDTO)
                .collect(Collectors.toList());
    }
}