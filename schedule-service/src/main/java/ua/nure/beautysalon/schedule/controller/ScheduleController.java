package ua.nure.beautysalon.schedule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.nure.beautysalon.schedule.dto.ScheduledFacilityDTO;
import ua.nure.beautysalon.schedule.service.ScheduleService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule")
@Tag(name = "Schedule", description = "Schedule management APIs")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    @Operation(summary = "Get schedule for date", description = "Retrieve schedule for a specific date")
    public List<ScheduledFacilityDTO> getSchedule(@RequestBody LocalDateTime date) {
        return scheduleService.getSchedule(date);
    }

    @PostMapping("/schedule-facility")
    @Operation(summary = "Schedule facility", description = "Schedule a new facility appointment")
    public ResponseEntity<ScheduledFacilityDTO> scheduleFacility(@RequestBody ScheduledFacilityDTO scheduledFacilityDTO) {
        ScheduledFacilityDTO result = scheduleService.scheduleFacility(scheduledFacilityDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get client schedules", description = "Get all scheduled facilities for a specific client")
    public List<ScheduledFacilityDTO> findAllScheduledFacilitiesByClientId(@PathVariable Long clientId) {
        return scheduleService.findAllScheduledFacilitiesByClientId(clientId);
    }

    @PutMapping("/edit/{id}")
    @Operation(summary = "Update scheduled facility", description = "Update an existing scheduled facility")
    public ResponseEntity<ScheduledFacilityDTO> editScheduledFacility(@PathVariable Long id, @RequestBody ScheduledFacilityDTO scheduledFacilityDTO) {
        return scheduleService.updateScheduledFacility(id, scheduledFacilityDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete scheduled facility", description = "Delete a scheduled facility by ID")
    public void deleteScheduledFacility(@PathVariable Long id) {
        scheduleService.deleteScheduledFacility(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get scheduled facility", description = "Get a scheduled facility by ID")
    public ResponseEntity<ScheduledFacilityDTO> getScheduledFacilityById(@PathVariable Long id) {
        return scheduleService.getFacilityById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoints for cascade deletions from other services
    @DeleteMapping("/client/{clientId}")
    @Operation(summary = "Delete by client ID", description = "Delete all schedules for a specific client")
    public void deleteByClientId(@PathVariable Long clientId) {
        scheduleService.deleteByClientId(clientId);
    }

    @DeleteMapping("/master/{masterId}")
    @Operation(summary = "Delete by master ID", description = "Delete all schedules for a specific master")
    public void deleteByMasterId(@PathVariable Long masterId) {
        scheduleService.deleteByMasterId(masterId);
    }

    @DeleteMapping("/facility/{facilityId}")
    @Operation(summary = "Delete by facility ID", description = "Delete all schedules for a specific facility")
    public void deleteByFacilityId(@PathVariable Long facilityId) {
        scheduleService.deleteByFacilityId(facilityId);
    }
}
