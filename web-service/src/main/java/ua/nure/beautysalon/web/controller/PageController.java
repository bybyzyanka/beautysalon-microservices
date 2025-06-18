package ua.nure.beautysalon.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index(Model model) {
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

    @GetMapping("/schedule")
    public String schedule(Model model) {
        return "schedule";
    }
}