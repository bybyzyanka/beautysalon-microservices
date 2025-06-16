package ua.nure.beautysalon.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.nure.beautysalon.master.dto.MasterDTO;
import ua.nure.beautysalon.master.dto.UserMasterDTO;
import ua.nure.beautysalon.master.service.MasterService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/master")
@Tag(name = "Master", description = "Master management APIs")
public class MasterController {
    
    private final MasterService masterService;

    @GetMapping
    @Operation(summary = "Get all masters", description = "Retrieve paginated list of masters with optional search")
    public Page<MasterDTO> getAllMasters(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (search != null && !search.isEmpty()) {
            return masterService.searchMastersByNameOrEmail(search, page, size);
        }

        return masterService.getAllMasters(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get master by ID", description = "Retrieve a specific master by their ID")
    public ResponseEntity<MasterDTO> getMasterById(@PathVariable Long id) {
        return masterService.getMasterById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete master", description = "Delete a master by their ID")
    public ResponseEntity<Void> deleteMaster(@PathVariable Long id) {
        masterService.deleteMasterById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update master", description = "Update an existing master")
    public ResponseEntity<MasterDTO> updateMaster(@PathVariable Long id, @RequestBody UserMasterDTO userMasterDTO) {
        return masterService.updateMaster(id, userMasterDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Add master", description = "Create a new master")
    public ResponseEntity<MasterDTO> addMaster(@RequestBody UserMasterDTO masterDTO) {
        MasterDTO createdMaster = masterService.addMaster(masterDTO);
        return ResponseEntity.ok(createdMaster);
    }
}