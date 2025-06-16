package ua.nure.beautysalon.master.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "schedule-service")
public interface ScheduleServiceClient {
    
    @DeleteMapping("/api/schedule/master/{masterId}")
    void deleteByMasterId(@PathVariable("masterId") Long masterId);
}