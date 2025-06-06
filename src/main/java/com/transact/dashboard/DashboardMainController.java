package com.transact.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import java.util.Collections;
import java.util.Map;

@Controller
public class DashboardMainController {

    @GetMapping("/dashboard-home")
    public String dashboardHome() {
        return "dashboard-home";
    }

    @GetMapping("/button-action")
    public String handleButtonClick(Model model, String name) {
        model.addAttribute("responseMessage", "Button clicked: " + name);
        return "dashboard-home";
    }

    @GetMapping("/trigger-ofs")
    public String triggerOfs(Model model) {
        try {
            String url = "http://localhost:8080/TAFJRestServices/resources/ofs";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setCacheControl("no-cache");

            // ✅ Hardcoded Base64 Authorization header
            headers.set("Authorization", "Basic dGFmai5hZG1pbjpBWElAZ3RwcXJYNC==");

            // Build OFS request
            String ofsRequest = "ENQUIRY.SELECT,,MB.OFFICER/123123,%USER,SIGN.ON.NAME:=MB.OFFICER";

            Map<String, String> requestBody = Collections.singletonMap("ofsRequest", ofsRequest);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(url, entity, String.class);

            model.addAttribute("responseMessage", "OFS API Response:\n" + response);

        } catch (Exception e) {
            model.addAttribute("responseMessage", "Error calling OFS API: " + e.getMessage());
        }

        return "dashboard-home";
    }
}
