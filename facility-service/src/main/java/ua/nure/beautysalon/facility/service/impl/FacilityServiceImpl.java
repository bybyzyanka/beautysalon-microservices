package ua.nure.beautysalon.facility.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

import jakarta.validation.ConstraintViolationException;
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
        try {
            // Validate price
            if (facilityDTO.getPrice() == null) {
                throw new IllegalArgumentException("Price is required");
            }
            if (facilityDTO.getPrice() <= 0) {
                throw new IllegalArgumentException("Price must be positive");
            }
            if (facilityDTO.getPrice() > 999999.99) {
                throw new IllegalArgumentException("Price cannot exceed 999,999.99");
            }

            if (facilityRepository.existsByNameIgnoreCase(facilityDTO.getName())) {
                throw new IllegalArgumentException("A facility with this name already exists");
            }

            Facility facilityEntity = facilityMapper.toEntity(facilityDTO);
            Facility savedFacility = facilityRepository.save(facilityEntity);
            return facilityMapper.toDTO(savedFacility);

        } catch (ConstraintViolationException ex) {
            // Extract the first constraint violation message
            String message = ex.getConstraintViolations()
                    .stream()
                    .findFirst()
                    .map(violation -> violation.getMessage())
                    .orElse("Validation failed");
            throw new IllegalArgumentException(message);

        } catch (DataIntegrityViolationException ex) {
            // Handle database constraint violations
            if (ex.getMessage().toLowerCase().contains("name")) {
                throw new IllegalArgumentException("A facility with this name already exists");
            } else {
                throw new IllegalArgumentException("A facility with this information already exists");
            }
        } catch (IllegalArgumentException ex) {
            // Re-throw our custom validation messages
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to create facility: " + ex.getMessage());
        }
    }

    @Override
    public void deleteFacilityById(Long id) {
        try {
            if (!facilityRepository.existsById(id)) {
                throw new IllegalArgumentException("Facility not found with id: " + id);
            }

            scheduleServiceClient.deleteByFacilityId(id);
        } catch (Exception e) {
            // Log error but continue with deletion if it's a Feign client issue
            if (e.getMessage() != null && !e.getMessage().contains("Facility not found")) {
                // Continue with deletion even if schedule service is unavailable
            } else {
                throw e; // Re-throw if it's our "not found" exception
            }
        }

        facilityRepository.deleteById(id);
    }

    @Override
    public Optional<FacilityDTO> updateFacility(Long id, FacilityDTO facilityDTO) {
        return facilityRepository.findById(id).map(existingFacility -> {
            try {
                // Validate price
                if (facilityDTO.getPrice() == null) {
                    throw new IllegalArgumentException("Price is required");
                }
                if (facilityDTO.getPrice() <= 0) {
                    throw new IllegalArgumentException("Price must be positive");
                }
                if (facilityDTO.getPrice() > 999999.99) {
                    throw new IllegalArgumentException("Price cannot exceed 999,999.99");
                }

                // Check if name already exists (excluding current facility)
                if (!existingFacility.getName().equalsIgnoreCase(facilityDTO.getName()) &&
                        facilityRepository.existsByNameIgnoreCase(facilityDTO.getName())) {
                    throw new IllegalArgumentException("A facility with this name already exists");
                }

                existingFacility.setName(facilityDTO.getName());
                existingFacility.setPrice(facilityDTO.getPrice());

                facilityRepository.save(existingFacility);
                return facilityMapper.toDTO(existingFacility);

            } catch (ConstraintViolationException ex) {
                String message = ex.getConstraintViolations()
                        .stream()
                        .findFirst()
                        .map(violation -> violation.getMessage())
                        .orElse("Validation failed");
                throw new IllegalArgumentException(message);

            } catch (DataIntegrityViolationException ex) {
                if (ex.getMessage().toLowerCase().contains("name")) {
                    throw new IllegalArgumentException("A facility with this name already exists");
                } else {
                    throw new IllegalArgumentException("A facility with this information already exists");
                }
            } catch (IllegalArgumentException ex) {
                // Re-throw our custom validation messages
                throw ex;
            } catch (Exception ex) {
                throw new IllegalArgumentException("Failed to update facility: " + ex.getMessage());
            }
        });
    }

    @Override
    public Optional<FacilityDTO> getFacilityById(Long id) {
        try {
            return facilityRepository.findById(id)
                    .map(facilityMapper::toDTO);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to retrieve facility: " + ex.getMessage());
        }
    }

    @Override
    public Page<FacilityDTO> getAllFacilities(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return facilityRepository.findAll(pageable)
                    .map(facilityMapper::toDTO);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to retrieve facilities: " + ex.getMessage());
        }
    }

    @Override
    public Page<FacilityDTO> searchFacilitiesByName(String searchTerm, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return facilityRepository.findByNameContainingIgnoreCase(searchTerm, pageable)
                    .map(facilityMapper::toDTO);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to search facilities: " + ex.getMessage());
        }
    }

    @Override
    public List<FacilityDTO> getFacilitiesByIds(List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return List.of();
            }

            return facilityRepository.findAllById(ids).stream()
                    .map(facilityMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to retrieve facilities by IDs: " + ex.getMessage());
        }
    }
}