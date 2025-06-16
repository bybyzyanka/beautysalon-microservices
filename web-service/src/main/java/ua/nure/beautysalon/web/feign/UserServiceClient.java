package ua.nure.beautysalon.web.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ua.nure.beautysalon.web.dto.UserDTO;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    
    @GetMapping("/api/auth/user/{email}")
    UserDTO findByEmail(@PathVariable("email") String email);
}