package ua.nure.beautysalon.master.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    
    @PostMapping("/api/auth/signup")
    void signup(@RequestParam("email") String email, 
               @RequestParam("password") String password, 
               @RequestParam("role") String role);
    
    @PutMapping("/api/auth/password")
    void updatePassword(@RequestParam("email") String email, 
                       @RequestParam("password") String password);
}
