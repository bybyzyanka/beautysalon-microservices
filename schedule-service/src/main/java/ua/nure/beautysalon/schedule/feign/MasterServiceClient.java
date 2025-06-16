package ua.nure.beautysalon.schedule.feign;

import ua.nure.beautysalon.schedule.dto.MasterDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "master-service")
public interface MasterServiceClient {
    
    @GetMapping("/api/master/{id}")
    MasterDTO getMasterById(@PathVariable("id") Long id);
}