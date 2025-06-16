package ua.nure.beautysalon.schedule.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nure.beautysalon.schedule.dto.ScheduledFacilityDTO;
import ua.nure.beautysalon.schedule.entity.ScheduledFacility;
import ua.nure.beautysalon.schedule.mapper.ScheduledFacilityMapper;
import ua.nure.beautysalon.schedule.repository.ScheduleRepository;
import ua.nure.beautysalon.schedule.service.ScheduleService;

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
        if (willOverlapWithMasterSchedule(scheduledFacilityDTO)) {
            throw new IllegalArgumentException("Scheduled facility overlaps with master's schedule");
        }

        ScheduledFacility entity = scheduledFacilityMapper.toEntity(scheduledFacilityDTO);
        ScheduledFacility saved = scheduleRepository.save(entity);
        return scheduledFacilityMapper.toDTO(saved);
    }

    @Override
    public List<ScheduledFacilityDTO> getSchedule(LocalDateTime date) {
        return findAllScheduledFacilities(date).stream()
                .sorted(Comparator.comparing(ScheduledFacilityDTO::getDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduledFacilityDTO> findAllScheduledFacilities(LocalDateTime day) {
        return scheduleRepository.findAll().stream()
                .map(scheduledFacilityMapper::toDTO)
                .filter(scheduledFacilityDTO -> scheduledFacilityDTO.getDate().toLocalDate().isEqual(day.toLocalDate()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean willOverlapWithMasterSchedule(ScheduledFacilityDTO scheduledFacilityDTO) {
        List<ScheduledFacilityDTO> scheduledFacilities = findAllScheduledFacilities(scheduledFacilityDTO.getDate());
        return scheduledFacilities.stream()
                .anyMatch(scheduledFacility ->
                        !Objects.equals(scheduledFacility.getId(), scheduledFacilityDTO.getId())
                        && scheduledFacility.getMaster().getId().equals(scheduledFacilityDTO.getMaster().getId())
                        && scheduledFacility.getDate().isBefore(scheduledFacilityDTO.getDate()
                                .plusMinutes(scheduledFacilityDTO.getDuration()))
                        && scheduledFacility.getDate().plusMinutes(scheduledFacility.getDuration())
                            .isAfter(scheduledFacilityDTO.getDate()));
    }

    @Override
    public List<ScheduledFacilityDTO> findAllScheduledFacilitiesByClientId(Long clientId) {
        return scheduleRepository.findAllByClientId(clientId).stream()
                .map(scheduledFacilityMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteScheduledFacility(Long id) {
        scheduleRepository.deleteById(id);
    }

    @Override
    public Optional<ScheduledFacilityDTO> getFacilityById(Long id) {
        return scheduleRepository.findById(id)
                .map(scheduledFacilityMapper::toDTO);
    }

    @Override
    public Optional<ScheduledFacilityDTO> updateScheduledFacility(Long id, ScheduledFacilityDTO scheduledFacilityDTO) {
        if (willOverlapWithMasterSchedule(scheduledFacilityDTO)) {
            throw new IllegalArgumentException("Scheduled facility overlaps with master's schedule");
        }

        return scheduleRepository.findById(id).map(existingScheduledFacility -> {
            existingScheduledFacility.setDate(scheduledFacilityDTO.getDate());
            existingScheduledFacility.setDuration(scheduledFacilityDTO.getDuration());

            ScheduledFacility scheduledFacilityEntity = scheduledFacilityMapper.toEntity(scheduledFacilityDTO);
            existingScheduledFacility.setClientId(scheduledFacilityEntity.getClientId());
            existingScheduledFacility.setMasterId(scheduledFacilityEntity.getMasterId());
            existingScheduledFacility.setFacilityId(scheduledFacilityEntity.getFacilityId());

            scheduleRepository.save(existingScheduledFacility);
            return scheduledFacilityMapper.toDTO(existingScheduledFacility);
        });
    }

    @Override
    public void deleteByClientId(Long clientId) {
        scheduleRepository.deleteByClientId(clientId);
    }

    @Override
    public void deleteByMasterId(Long masterId) {
        scheduleRepository.deleteByMasterId(masterId);
    }

    @Override
    public void deleteByFacilityId(Long facilityId) {
        scheduleRepository.deleteByFacilityId(facilityId);
    }
}
