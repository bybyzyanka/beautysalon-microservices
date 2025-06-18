package ua.nure.beautysalon.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nure.beautysalon.master.dto.MasterDTO;
import ua.nure.beautysalon.master.entity.Master;
import ua.nure.beautysalon.master.feign.ScheduleServiceClient;
import ua.nure.beautysalon.master.mapper.MasterMapper;
import ua.nure.beautysalon.master.repository.MasterRepository;
import ua.nure.beautysalon.master.service.MasterService;

import jakarta.validation.ConstraintViolationException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class MasterServiceImpl implements MasterService {

    private final MasterRepository masterRepository;
    private final MasterMapper masterMapper;
    private final ScheduleServiceClient scheduleServiceClient;

    @Override
    public MasterDTO addMaster(MasterDTO masterDTO) {
        try {
            if (masterRepository.existsByPhoneOrEmail(masterDTO.getPhone(), masterDTO.getEmail())) {
                throw new IllegalArgumentException("A master with this email or phone number already exists");
            }

            Master masterEntity = masterMapper.toEntity(masterDTO);
            Master savedMaster = masterRepository.save(masterEntity);
            return masterMapper.toDTO(savedMaster);

        } catch (ConstraintViolationException ex) {
            // Extract the first constraint violation message
            String message = ex.getConstraintViolations()
                    .stream()
                    .findFirst()
                    .map(violation -> violation.getMessage())
                    .orElse("Validation failed");
            throw new IllegalArgumentException(message);

        } catch (DataIntegrityViolationException ex) {
            // Handle database constraint violations (unique constraints, etc.)
            if (ex.getMessage().contains("email")) {
                throw new IllegalArgumentException("A master with this email already exists");
            } else if (ex.getMessage().contains("phone")) {
                throw new IllegalArgumentException("A master with this phone number already exists");
            } else {
                throw new IllegalArgumentException("A master with this information already exists");
            }
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Feign")) {
                throw new IllegalArgumentException("Unable to verify facility information. Please check if the selected facilities exist.");
            }
            throw new IllegalArgumentException("Failed to create master: " + ex.getMessage());
        }
    }

    @Override
    public void deleteMasterById(Long id) {
        try {
            if (!masterRepository.existsById(id)) {
                throw new IllegalArgumentException("Master not found with id: " + id);
            }

            scheduleServiceClient.deleteByMasterId(id);
        } catch (Exception e) {
            // Log error but continue with deletion if it's a Feign client issue
            if (e.getMessage() != null && !e.getMessage().contains("Master not found")) {
                // Continue with deletion even if schedule service is unavailable
            } else {
                throw e; // Re-throw if it's our "not found" exception
            }
        }

        masterRepository.deleteById(id);
    }

    @Override
    public Optional<MasterDTO> updateMaster(Long id, MasterDTO masterDTO) {
        return masterRepository.findById(id).map(existingMaster -> {
            try {
                existingMaster.setName(masterDTO.getName());
                existingMaster.setEmail(masterDTO.getEmail());
                existingMaster.setPhone(masterDTO.getPhone());
                existingMaster.setFacilityIds(masterMapper.toEntity(masterDTO).getFacilityIds());

                masterRepository.save(existingMaster);
                return masterMapper.toDTO(existingMaster);

            } catch (ConstraintViolationException ex) {
                String message = ex.getConstraintViolations()
                        .stream()
                        .findFirst()
                        .map(violation -> violation.getMessage())
                        .orElse("Validation failed");
                throw new IllegalArgumentException(message);

            } catch (DataIntegrityViolationException ex) {
                if (ex.getMessage().contains("email")) {
                    throw new IllegalArgumentException("A master with this email already exists");
                } else if (ex.getMessage().contains("phone")) {
                    throw new IllegalArgumentException("A master with this phone number already exists");
                } else {
                    throw new IllegalArgumentException("A master with this information already exists");
                }
            } catch (Exception ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("Feign")) {
                    throw new IllegalArgumentException("Unable to verify facility information. Please check if the selected facilities exist.");
                }
                throw new IllegalArgumentException("Failed to update master: " + ex.getMessage());
            }
        });
    }

    @Override
    public Optional<MasterDTO> getMasterById(Long id) {
        try {
            return masterRepository.findById(id)
                    .map(masterMapper::toDTO);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Feign")) {
                throw new IllegalArgumentException("Unable to load facility information for this master");
            }
            throw new IllegalArgumentException("Failed to retrieve master: " + ex.getMessage());
        }
    }

    @Override
    public Page<MasterDTO> getAllMasters(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return masterRepository.findAll(pageable)
                    .map(master -> {
                        try {
                            return masterMapper.toDTO(master);
                        } catch (Exception ex) {
                            // If facility service is down, return master without facilities
                            MasterDTO dto = new MasterDTO();
                            dto.setId(master.getId());
                            dto.setName(master.getName());
                            dto.setEmail(master.getEmail());
                            dto.setPhone(master.getPhone());
                            dto.setFacilities(java.util.List.of());
                            return dto;
                        }
                    });
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to retrieve masters: " + ex.getMessage());
        }
    }

    @Override
    public Page<MasterDTO> searchMastersByNameOrEmail(String searchTerm, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return masterRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(searchTerm, searchTerm, pageable)
                    .map(master -> {
                        try {
                            return masterMapper.toDTO(master);
                        } catch (Exception ex) {
                            // If facility service is down, return master without facilities
                            MasterDTO dto = new MasterDTO();
                            dto.setId(master.getId());
                            dto.setName(master.getName());
                            dto.setEmail(master.getEmail());
                            dto.setPhone(master.getPhone());
                            dto.setFacilities(java.util.List.of());
                            return dto;
                        }
                    });
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to search masters: " + ex.getMessage());
        }
    }
}