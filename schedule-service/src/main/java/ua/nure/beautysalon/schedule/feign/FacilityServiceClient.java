package ua.nure.beautysalon.schedule.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ua.nure.beautysalon.schedule.dto.FacilityDTO;

@FeignClient(name = "facility-service")
public interface FacilityServiceClient {
    
    @GetMapping("/api/facility/{id}")
    FacilityDTO getFacilityById(@PathVariable("id") Long id);
}