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
        return "dashboard-home";  // returns home.html
    }

    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "logout-success"; // maps to logout-success.html
    }

}
