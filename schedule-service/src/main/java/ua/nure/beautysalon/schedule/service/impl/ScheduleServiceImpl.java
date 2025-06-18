package ua.nure.beautysalon.schedule.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nure.beautysalon.schedule.dto.ScheduledFacilityDTO;
import ua.nure.beautysalon.schedule.entity.ScheduledFacility;
import ua.nure.beautysalon.schedule.mapper.ScheduledFacilityMapper;
import ua.nure.beautysalon.schedule.repository.ScheduleRepository;
import ua.nure.beautysalon.schedule.service.ScheduleService;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduledFacilityMapper scheduledFacilityMapper;

    @Override
    public ScheduledFacilityDTO scheduleFacility(ScheduledFacilityDTO scheduledFacilityDTO) {
        try {
            // Validate required fields
            validateScheduledFacility(scheduledFacilityDTO);

            if (willOverlapWithMasterSchedule(scheduledFacilityDTO)) {
                throw new IllegalArgumentException("This time slot conflicts with another appointment for the same master");
            }

            ScheduledFacility entity = scheduledFacilityMapper.toEntity(scheduledFacilityDTO);
            ScheduledFacility saved = scheduleRepository.save(entity);
            return scheduledFacilityMapper.toDTO(saved);

        } catch (ConstraintViolationException ex) {
            String message = ex.getConstraintViolations()
                    .stream()
                    .findFirst()
                    .map(violation -> violation.getMessage())
                    .orElse("Validation failed");
            throw new IllegalArgumentException(message);

        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Failed to save appointment due to data conflict");

        } catch (IllegalArgumentException ex) {
            throw ex; // Re-throw our custom validation messages

        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Feign")) {
                throw new IllegalArgumentException("Unable to verify client, master, or facility information. Please ensure all selections are valid.");
            }
            throw new IllegalArgumentException("Failed to schedule appointment: " + ex.getMessage());
        }
    }

    @Override
    public List<ScheduledFacilityDTO> getSchedule(LocalDateTime date) {
        try {
            return findAllScheduledFacilities(date).stream()
                    .sorted(Comparator.comparing(ScheduledFacilityDTO::getDate))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to retrieve schedule: " + ex.getMessage());
        }
    }

    @Override
    public List<ScheduledFacilityDTO> findAllScheduledFacilities(LocalDateTime day) {
        try {
            return scheduleRepository.findAll().stream()
                    .map(scheduledFacility -> {
                        try {
                            return scheduledFacilityMapper.toDTO(scheduledFacility);
                        } catch (Exception ex) {
                            // If external service is down, return minimal information
                            ScheduledFacilityDTO dto = new ScheduledFacilityDTO();
                            dto.setId(scheduledFacility.getId());
                            dto.setDate(scheduledFacility.getDate());
                            dto.setDuration(scheduledFacility.getDuration());
                            return dto;
                        }
                    })
                    .filter(scheduledFacilityDTO ->
                            scheduledFacilityDTO.getDate() != null &&
                                    scheduledFacilityDTO.getDate().toLocalDate().isEqual(day.toLocalDate()))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to retrieve scheduled facilities: " + ex.getMessage());
        }
    }

    @Override
    public boolean willOverlapWithMasterSchedule(ScheduledFacilityDTO scheduledFacilityDTO) {
        try {
            if (scheduledFacilityDTO.getMaster() == null || scheduledFacilityDTO.getDate() == null || scheduledFacilityDTO.getDuration() == null) {
                return false; // Cannot check overlap without required data
            }

            List<ScheduledFacilityDTO> scheduledFacilities = findAllScheduledFacilities(scheduledFacilityDTO.getDate());
            return scheduledFacilities.stream()
                    .anyMatch(scheduledFacility -> {
                        if (scheduledFacility.getMaster() == null) {
                            return false; // Skip if master info is missing
                        }

                        return !Objects.equals(scheduledFacility.getId(), scheduledFacilityDTO.getId())
                                && scheduledFacility.getMaster().getId().equals(scheduledFacilityDTO.getMaster().getId())
                                && scheduledFacility.getDate().isBefore(scheduledFacilityDTO.getDate()
                                .plusMinutes(scheduledFacilityDTO.getDuration()))
                                && scheduledFacility.getDate().plusMinutes(scheduledFacility.getDuration())
                                .isAfter(scheduledFacilityDTO.getDate());
                    });
        } catch (Exception ex) {
            // If we can't check for overlap, assume there might be one for safety
            return true;
        }
    }

    @Override
    public List<ScheduledFacilityDTO> findAllScheduledFacilitiesByClientId(Long clientId) {
        try {
            return scheduleRepository.findAllByClientId(clientId).stream()
                    .map(scheduledFacility -> {
                        try {
                            return scheduledFacilityMapper.toDTO(scheduledFacility);
                        } catch (Exception ex) {
                            // If external service is down, return minimal information
                            ScheduledFacilityDTO dto = new ScheduledFacilityDTO();
                            dto.setId(scheduledFacility.getId());
                            dto.setDate(scheduledFacility.getDate());
                            dto.setDuration(scheduledFacility.getDuration());
                            return dto;
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to retrieve appointments for client: " + ex.getMessage());
        }
    }

    @Override
    public void deleteScheduledFacility(Long id) {
        try {
            if (!scheduleRepository.existsById(id)) {
                throw new IllegalArgumentException("Appointment not found with id: " + id);
            }

            scheduleRepository.deleteById(id);
        } catch (IllegalArgumentException ex) {
            throw ex; // Re-throw our custom messages
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to delete appointment: " + ex.getMessage());
        }
    }

    @Override
    public Optional<ScheduledFacilityDTO> getFacilityById(Long id) {
        try {
            return scheduleRepository.findById(id)
                    .map(scheduledFacility -> {
                        try {
                            return scheduledFacilityMapper.toDTO(scheduledFacility);
                        } catch (Exception ex) {
                            // If external service is down, return minimal information
                            ScheduledFacilityDTO dto = new ScheduledFacilityDTO();
                            dto.setId(scheduledFacility.getId());
                            dto.setDate(scheduledFacility.getDate());
                            dto.setDuration(scheduledFacility.getDuration());
                            return dto;
                        }
                    });
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to retrieve appointment: " + ex.getMessage());
        }
    }

    @Override
    public Optional<ScheduledFacilityDTO> updateScheduledFacility(Long id, ScheduledFacilityDTO scheduledFacilityDTO) {
        return scheduleRepository.findById(id).map(existingScheduledFacility -> {
            try {
                // Validate the updated data
                validateScheduledFacility(scheduledFacilityDTO);

                // Set the ID for overlap checking
                scheduledFacilityDTO.setId(id);

                if (willOverlapWithMasterSchedule(scheduledFacilityDTO)) {
                    throw new IllegalArgumentException("This time slot conflicts with another appointment for the same master");
                }

                existingScheduledFacility.setDate(scheduledFacilityDTO.getDate());
                existingScheduledFacility.setDuration(scheduledFacilityDTO.getDuration());

                ScheduledFacility scheduledFacilityEntity = scheduledFacilityMapper.toEntity(scheduledFacilityDTO);
                existingScheduledFacility.setClientId(scheduledFacilityEntity.getClientId());
                existingScheduledFacility.setMasterId(scheduledFacilityEntity.getMasterId());
                existingScheduledFacility.setFacilityId(scheduledFacilityEntity.getFacilityId());

                scheduleRepository.save(existingScheduledFacility);
                return scheduledFacilityMapper.toDTO(existingScheduledFacility);

            } catch (ConstraintViolationException ex) {
                String message = ex.getConstraintViolations()
                        .stream()
                        .findFirst()
                        .map(violation -> violation.getMessage())
                        .orElse("Validation failed");
                throw new IllegalArgumentException(message);

            } catch (IllegalArgumentException ex) {
                throw ex; // Re-throw our custom validation messages

            } catch (Exception ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("Feign")) {
                    throw new IllegalArgumentException("Unable to verify client, master, or facility information. Please ensure all selections are valid.");
                }
                throw new IllegalArgumentException("Failed to update appointment: " + ex.getMessage());
            }
        });
    }

    @Override
    public void deleteByClientId(Long clientId) {
        try {
            scheduleRepository.deleteByClientId(clientId);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to delete appointments for client: " + ex.getMessage());
        }
    }

    @Override
    public void deleteByMasterId(Long masterId) {
        try {
            scheduleRepository.deleteByMasterId(masterId);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to delete appointments for master: " + ex.getMessage());
        }
    }

    @Override
    public void deleteByFacilityId(Long facilityId) {
        try {
            scheduleRepository.deleteByFacilityId(facilityId);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to delete appointments for facility: " + ex.getMessage());
        }
    }

    private void validateScheduledFacility(ScheduledFacilityDTO dto) {
        if (dto.getDate() == null) {
            throw new IllegalArgumentException("Date is required");
        }

        if (dto.getDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment date must be in the future");
        }

        if (dto.getDuration() == null) {
            throw new IllegalArgumentException("Duration is required");
        }

        if (dto.getDuration() <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }

        if (dto.getDuration() > 1440) {
            throw new IllegalArgumentException("Duration cannot exceed 24 hours (1440 minutes)");
        }

        if (dto.getClient() == null || dto.getClient().getId() == null) {
            throw new IllegalArgumentException("Client is required");
        }

        if (dto.getMaster() == null || dto.getMaster().getId() == null) {
            throw new IllegalArgumentException("Master is required");
        }

        if (dto.getFacility() == null || dto.getFacility().getId() == null) {
            throw new IllegalArgumentException("Facility is required");
        }
    }
}