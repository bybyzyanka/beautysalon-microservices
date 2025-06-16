package ua.nure.beautysalon.client.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "schedule-service")
public interface ScheduleServiceClient {
    
    @DeleteMapping("/api/schedule/client/{clientId}")
    void deleteByClientId(@PathVariable("clientId") Long clientId);
}