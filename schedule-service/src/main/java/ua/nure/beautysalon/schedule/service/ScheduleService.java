package ua.nure.beautysalon.schedule.service;

import ua.nure.beautysalon.schedule.dto.ScheduledFacilityDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleService {
    ScheduledFacilityDTO scheduleFacility(ScheduledFacilityDTO scheduledFacilityDTO);
    List<ScheduledFacilityDTO> getSchedule(LocalDateTime date);
    List<ScheduledFacilityDTO> findAllScheduledFacilities(LocalDateTime day);
    boolean willOverlapWithMasterSchedule(ScheduledFacilityDTO scheduledFacilityDTO);
    List<ScheduledFacilityDTO> findAllScheduledFacilitiesByClientId(Long clientId);
    void deleteScheduledFacility(Long id);
    Optional<ScheduledFacilityDTO> getFacilityById(Long id);
    Optional<ScheduledFacilityDTO> updateScheduledFacility(Long id, ScheduledFacilityDTO scheduledFacilityDTO);
    void deleteByClientId(Long clientId);
    void deleteByMasterId(Long masterId);
    void deleteByFacilityId(Long facilityId);
}