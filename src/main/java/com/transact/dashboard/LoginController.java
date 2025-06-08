package com.transact.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("transact-dashboard-login")
    public String login() {
        return "login"; // returns login.html
    }

    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "logout-success"; // maps to logout-success.html
    }

}
