package ua.nure.beautysalon.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.beautysalon.schedule.entity.ScheduledFacility;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<ScheduledFacility, Long> {
    void deleteByClientId(Long clientId);
    void deleteByMasterId(Long masterId);
    void deleteByFacilityId(Long facilityId);
    List<ScheduledFacility> findAllByClientId(Long clientId);
}