package ua.nure.beautysalon.master.service;

import org.springframework.data.domain.Page;
import ua.nure.beautysalon.master.dto.MasterDTO;
import ua.nure.beautysalon.master.dto.UserMasterDTO;

import java.util.Optional;

public interface MasterService {
    MasterDTO addMaster(UserMasterDTO masterDTO);
    void deleteMasterById(Long id);
    Optional<MasterDTO> updateMaster(Long id, UserMasterDTO masterDTO);
    Optional<MasterDTO> getMasterById(Long id);
    Page<MasterDTO> getAllMasters(int page, int size);
    Page<MasterDTO> searchMastersByNameOrEmail(String searchTerm, int page, int size);
}