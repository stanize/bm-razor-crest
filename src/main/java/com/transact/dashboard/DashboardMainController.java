package com.transact.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardMainController {

    @GetMapping("/dashboard-home")
    public String dashboardHome() {
        return "dashboard-home";
    }

    @GetMapping("/button-action")
    public String handleButtonClick(@RequestParam("name") String buttonName, Model model) {
        model.addAttribute("responseMessage", "Button clicked: " + buttonName);
        return "dashboard-home";
    }
}
