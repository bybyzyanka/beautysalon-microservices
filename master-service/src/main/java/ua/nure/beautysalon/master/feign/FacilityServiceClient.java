package ua.nure.beautysalon.master.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ua.nure.beautysalon.master.dto.FacilityDTO;

import java.util.List;

@FeignClient(name = "facility-service")
public interface FacilityServiceClient {
    
    @GetMapping("/api/facility/{id}")
    FacilityDTO getFacilityById(@PathVariable("id") Long id);
    
    @GetMapping("/api/facility/batch")
    List<FacilityDTO> getFacilitiesByIds(@RequestParam("ids") List<Long> ids);
}