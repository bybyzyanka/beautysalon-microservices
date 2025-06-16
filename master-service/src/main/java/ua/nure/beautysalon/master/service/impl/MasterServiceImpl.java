package ua.nure.beautysalon.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nure.beautysalon.master.dto.MasterDTO;
import ua.nure.beautysalon.master.dto.UserMasterDTO;
import ua.nure.beautysalon.master.entity.Master;
import ua.nure.beautysalon.master.feign.ScheduleServiceClient;
import ua.nure.beautysalon.master.feign.UserServiceClient;
import ua.nure.beautysalon.master.mapper.MasterMapper;
import ua.nure.beautysalon.master.repository.MasterRepository;
import ua.nure.beautysalon.master.service.MasterService;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class MasterServiceImpl implements MasterService {

    private final MasterRepository masterRepository;
    private final MasterMapper masterMapper;
    private final UserServiceClient userServiceClient;
    private final ScheduleServiceClient scheduleServiceClient;

    @Override
    public MasterDTO addMaster(UserMasterDTO masterDTO) {
        if (masterRepository.existsByPhoneOrEmail(masterDTO.getPhone(), masterDTO.getEmail())) {
            throw new IllegalArgumentException("Master with this email or phone number already exists");
        }

        try {
            userServiceClient.signup(masterDTO.getEmail(), masterDTO.getPassword(), "MASTER");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user account for master");
        }

        Master masterEntity = masterMapper.toEntity(masterDTO);
        Master savedMaster = masterRepository.save(masterEntity);
        return masterMapper.toDTO(savedMaster);
    }

    @Override
    public void deleteMasterById(Long id) {
        try {
            scheduleServiceClient.deleteByMasterId(id);
        } catch (Exception e) {
            // Log error but continue with deletion
        }
        masterRepository.deleteById(id);
    }

    @Override
    public Optional<MasterDTO> updateMaster(Long id, UserMasterDTO masterDTO) {
        return masterRepository.findById(id).map(existingMaster -> {
            try {
                userServiceClient.updatePassword(existingMaster.getEmail(), masterDTO.getPassword());
            } catch (Exception e) {
                // Log error but continue with update
            }
            
            existingMaster.setName(masterDTO.getName());
            existingMaster.setEmail(masterDTO.getEmail());
            existingMaster.setPhone(masterDTO.getPhone());
            existingMaster.setFacilityIds(masterMapper.toEntity(masterDTO).getFacilityIds());

            masterRepository.save(existingMaster);
            return masterMapper.toDTO(existingMaster);
        });
    }

    @Override
    public Optional<MasterDTO> getMasterById(Long id) {
        return masterRepository.findById(id)
                .map(masterMapper::toDTO);
    }

    @Override
    public Page<MasterDTO> getAllMasters(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return masterRepository.findAll(pageable)
                .map(masterMapper::toDTO);
    }

    @Override
    public Page<MasterDTO> searchMastersByNameOrEmail(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return masterRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(searchTerm, searchTerm, pageable)
                .map(masterMapper::toDTO);
    }
}
