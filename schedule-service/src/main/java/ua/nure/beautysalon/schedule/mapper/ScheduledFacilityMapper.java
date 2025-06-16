package ua.nure.beautysalon.schedule.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ua.nure.beautysalon.schedule.dto.ScheduledFacilityDTO;
import ua.nure.beautysalon.schedule.entity.ScheduledFacility;
import ua.nure.beautysalon.schedule.feign.ClientServiceClient;
import ua.nure.beautysalon.schedule.feign.FacilityServiceClient;
import ua.nure.beautysalon.schedule.feign.MasterServiceClient;

@Component
@RequiredArgsConstructor
public class ScheduledFacilityMapper {

    private final ClientServiceClient clientServiceClient;
    private final MasterServiceClient masterServiceClient;
    private final FacilityServiceClient facilityServiceClient;

    public ScheduledFacilityDTO toDTO(ScheduledFacility scheduledFacility) {
        ScheduledFacilityDTO scheduledFacilityDTO = new ScheduledFacilityDTO();
        scheduledFacilityDTO.setId(scheduledFacility.getId());
        scheduledFacilityDTO.setDate(scheduledFacility.getDate());
        scheduledFacilityDTO.setDuration(scheduledFacility.getDuration());
        
        try {
            scheduledFacilityDTO.setClient(clientServiceClient.getClientById(scheduledFacility.getClientId()));
        } catch (Exception e) {
            // Handle error - could set null or default value
        }
        
        try {
            scheduledFacilityDTO.setMaster(masterServiceClient.getMasterById(scheduledFacility.getMasterId()));
        } catch (Exception e) {
            // Handle error - could set null or default value
        }
        
        try {
            scheduledFacilityDTO.setFacility(facilityServiceClient.getFacilityById(scheduledFacility.getFacilityId()));
        } catch (Exception e) {
            // Handle error - could set null or default value
        }

        return scheduledFacilityDTO;
    }

    public ScheduledFacility toEntity(ScheduledFacilityDTO scheduledFacilityDTO) {
        ScheduledFacility scheduledFacility = new ScheduledFacility();
        scheduledFacility.setId(scheduledFacilityDTO.getId());
        scheduledFacility.setDate(scheduledFacilityDTO.getDate());
        scheduledFacility.setDuration(scheduledFacilityDTO.getDuration());
        
        if (scheduledFacilityDTO.getClient() != null) {
            scheduledFacility.setClientId(scheduledFacilityDTO.getClient().getId());
        }
        
        if (scheduledFacilityDTO.getMaster() != null) {
            scheduledFacility.setMasterId(scheduledFacilityDTO.getMaster().getId());
        }
        
        if (scheduledFacilityDTO.getFacility() != null) {
            scheduledFacility.setFacilityId(scheduledFacilityDTO.getFacility().getId());
        }

        return scheduledFacility;
    }
}