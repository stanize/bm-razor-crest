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
    public String home(Model model) {
        model.addAttribute("page", "home");
        model.addAttribute("contentFragment", "dashboard-home");
        return "maindashboard";
    }

    @GetMapping("/jboss")
    public String jboss(Model model) {
        //String status = getJbossStatus(); // implement this
        String status = "To be Implemented"; // implement this
        model.addAttribute("page", "jboss");
        model.addAttribute("jbossStatus", status);
        model.addAttribute("contentFragment", "dashboard-jboss");
        return "maindashboard";
    }

    @GetMapping("/tsm")
    public String tsm(Model model) {
        //String status = getTsmStatus(); // implement this
        String status = "To be Implemented"; // implement this
        model.addAttribute("page", "tsm");
        model.addAttribute("tsmStatus", status);
        model.addAttribute("contentFragment", "dashboard-tsm");
        return "maindashboard";
    }

    @GetMapping("/logs")
    public String logs(Model model) {
        model.addAttribute("page", "logs");
        model.addAttribute("contentFragment", "dashboard-logs");
        return "maindashboard";
    }
}
