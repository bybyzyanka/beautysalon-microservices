package ua.nure.beautysalon.schedule.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ua.nure.beautysalon.schedule.dto.ClientDTO;

@FeignClient(name = "client-service")
public interface ClientServiceClient {
    
    @GetMapping("/api/client/{id}")
    ClientDTO getClientById(@PathVariable("id") Long id);
}