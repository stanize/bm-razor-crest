package com.transact.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/transact-dashboard")
public class DashboardMainController {

    @GetMapping
    public String dashboard(Model model, HttpServletRequest request) {

        return "maindashboard";
    }

}
