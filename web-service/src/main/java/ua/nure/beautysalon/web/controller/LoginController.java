package ua.nure.beautysalon.web.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ua.nure.beautysalon.web.dto.LoginRequest;
import ua.nure.beautysalon.web.dto.LoginResponse;
import ua.nure.beautysalon.web.service.AuthService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                           @RequestParam(required = false) String logout,
                           @RequestParam(required = false) String redirect,
                           Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        
        // Store redirect URL for after successful login
        if (redirect != null) {
            model.addAttribute("redirect", redirect);
        }
        
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                       @RequestParam String password,
                       @RequestParam(required = false) String redirect,
                       HttpServletResponse response,
                       Model model) {
        try {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail(email);
            loginRequest.setPassword(password);
            
            LoginResponse loginResponse = authService.login(loginRequest);
            
            // Set JWT token as HTTP-only cookie
            Cookie jwtCookie = new Cookie("JWT_TOKEN", loginResponse.getToken());
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false); // Set to true in production with HTTPS
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60); // 24 hours
            response.addCookie(jwtCookie);
            
            log.info("User {} logged in successfully with role {}", email, loginResponse.getRole());
            
            // Redirect to the original requested page or home
            if (redirect != null && !redirect.isEmpty() && !redirect.equals("/login")) {
                return "redirect:" + redirect;
            }
            
            return "redirect:/";
            
        } catch (Exception e) {
            log.warn("Login failed for user {}: {}", email, e.getMessage());
            model.addAttribute("error", "Invalid email or password");
            model.addAttribute("email", email);
            
            if (redirect != null) {
                model.addAttribute("redirect", redirect);
            }
            
            return "login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        // Clear JWT cookie
        Cookie jwtCookie = new Cookie("JWT_TOKEN", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Delete cookie
        response.addCookie(jwtCookie);
        
        log.info("User logged out successfully");
        return "redirect:/login?logout=true";
    }
}