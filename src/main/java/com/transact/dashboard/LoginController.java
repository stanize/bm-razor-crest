package com.transact.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login"; // returns login.html
    }

    @GetMapping("/dashboard-home")
    public String home() {
        return "home";  // returns home.html
    }
}
