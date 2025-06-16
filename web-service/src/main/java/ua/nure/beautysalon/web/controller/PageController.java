package ua.nure.beautysalon.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PageController {

    @GetMapping("/")
    public String index(Model model, Authentication authentication) {
        if (authentication != null) {
            String role = authentication.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority())
                    .findFirst()
                    .orElse("ROLE_MASTER"); // Default role if none found
            model.addAttribute("role", role);
        }
        return "schedule";
    }

    @GetMapping("/clients")
    public String clients(Model model) {
        return "clients";
    }

    @GetMapping("/masters")
    public String masters(Model model) {
        return "masters";
    }

    @GetMapping("/facilities")
    public String facilities(Model model) {
        return "facilities";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}