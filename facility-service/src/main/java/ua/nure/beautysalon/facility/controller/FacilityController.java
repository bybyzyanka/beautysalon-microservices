package ua.nure.beautysalon.facility.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.nure.beautysalon.facility.dto.FacilityDTO;
import ua.nure.beautysalon.facility.service.FacilityService;

import java.util.List;

@RestController
@RequestMapping("/api/facility")
@RequiredArgsConstructor
@Tag(name = "Facility", description = "Facility management APIs")
public class FacilityController {

    private final FacilityService facilityService;

    @GetMapping
    @Operation(summary = "Get all facilities", description = "Retrieve paginated list of facilities with optional search")
    public Page<FacilityDTO> getAllFacilities(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (search != null && !search.isEmpty()) {
            return facilityService.searchFacilitiesByName(search, page, size);
        }

        return facilityService.getAllFacilities(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get facility by ID", description = "Retrieve a specific facility by its ID")
    public ResponseEntity<FacilityDTO> getFacilityById(@PathVariable Long id) {
        return facilityService.getFacilityById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/batch")
    @Operation(summary = "Get facilities by IDs", description = "Retrieve multiple facilities by their IDs")
    public List<FacilityDTO> getFacilitiesByIds(@RequestParam List<Long> ids) {
        return facilityService.getFacilitiesByIds(ids);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete facility", description = "Delete a facility by its ID")
    public ResponseEntity<Void> deleteFacility(@PathVariable Long id) {
        facilityService.deleteFacilityById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update facility", description = "Update an existing facility")
    public ResponseEntity<FacilityDTO> updateFacility(@PathVariable Long id, @RequestBody FacilityDTO facilityDTO) {
        return facilityService.updateFacility(id, facilityDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Add facility", description = "Create a new facility")
    public ResponseEntity<FacilityDTO> addFacility(@RequestBody FacilityDTO facilityDTO) {
        FacilityDTO createdFacility = facilityService.addFacility(facilityDTO);
        return ResponseEntity.ok(createdFacility);
    }
}
