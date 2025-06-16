package ua.nure.beautysalon.master.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ua.nure.beautysalon.master.dto.FacilityDTO;
import ua.nure.beautysalon.master.dto.MasterDTO;
import ua.nure.beautysalon.master.dto.UserMasterDTO;
import ua.nure.beautysalon.master.entity.Master;
import ua.nure.beautysalon.master.feign.FacilityServiceClient;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MasterMapper {

    private final FacilityServiceClient facilityServiceClient;

    public MasterDTO toDTO(Master master) {
        MasterDTO masterDTO = new MasterDTO();
        masterDTO.setId(master.getId());
        masterDTO.setName(master.getName());
        masterDTO.setEmail(master.getEmail());
        masterDTO.setPhone(master.getPhone());
        
        try {
            List<FacilityDTO> facilities = facilityServiceClient.getFacilitiesByIds(master.getFacilityIds());
            masterDTO.setFacilities(facilities);
        } catch (Exception e) {
            masterDTO.setFacilities(List.of());
        }

        return masterDTO;
    }

    public Master toEntity(UserMasterDTO userMasterDTO) {
        Master master = new Master();
        master.setId(userMasterDTO.getId());
        master.setName(userMasterDTO.getName());
        master.setEmail(userMasterDTO.getEmail());
        master.setPhone(userMasterDTO.getPhone());
        
        if (userMasterDTO.getFacilities() != null) {
            master.setFacilityIds(userMasterDTO.getFacilities().stream()
                    .map(FacilityDTO::getId)
                    .collect(Collectors.toList()));
        }

        return master;
    }
}