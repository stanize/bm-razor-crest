// DashboardController.java
package com.transact.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Controller
@RequestMapping("/transact-dashboard")
public class DashboardController {

    private final String JBossService = "jboss";
    private final String JBossHost = "localhost";
    private final int JBossPort = 8080;
    private static final String TSM_URL = "http://localhost:8080/TAFJRestServices/resources/ofs";
    private static final String TSM_AUTH_HEADER = "Basic dGFmai5hZG1pbjpBWElAZ3RwcXJYNC==";


    @GetMapping
    public String dashboard(Model model, HttpServletRequest request) {
        String status = checkJbossStatus();
        model.addAttribute("jbossStatus", status);

        // Retrieve log from session
        Object log = request.getSession().getAttribute("jbossLog");
        if (log != null) {
            model.addAttribute("jbossLog", log.toString());
            request.getSession().removeAttribute("jbossLog");
        } else {
            model.addAttribute("jbossLog", "");
        }

        String tsmStatus = getTsmStatus();
        model.addAttribute("tsmStatus", tsmStatus);


        return "dashboard";
    }

    @PostMapping("/start-jboss")
    public String startJboss(Model model, HttpServletRequest request) {
        String output = executeCommand("sudo systemctl start " + JBossService);
        request.getSession().setAttribute("jbossLog", "Starting JBoss...\n" + output);
        return "redirect:/transact-dashboard";
    }

    @PostMapping("/stop-jboss")
    public String stopJboss(Model model, HttpServletRequest request) {
        String output = executeCommand("sudo systemctl stop " + JBossService);
        request.getSession().setAttribute("jbossLog", "Stopping JBoss...\n" + output);
        return "redirect:/transact-dashboard";
    }

    private String checkJbossStatus() {
        boolean isActive = isServiceActive();
        boolean socketOpen = isSocketOpen();

        if (isActive && socketOpen) {
            return "Running";
        } else if (isActive) {
            return "Initializing";
        } else {
            return "Stopped";
        }
    }

    private boolean isServiceActive() {
        try {
            Process process = new ProcessBuilder("systemctl", "is-active", JBossService)
                    .redirectErrorStream(true)
                    .start();
            process.waitFor();

            String output = new String(process.getInputStream().readAllBytes()).trim();
            return "active".equals(output);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isSocketOpen() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(JBossHost, JBossPort), 1000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", command);
            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            output.append("Error: ").append(e.getMessage());
        }
        return output.toString();
    }

private String getTsmStatus() {
    try {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", TSM_AUTH_HEADER);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setCacheControl("no-cache");

        String jsonBody = "{\"ofsRequest\":\"TSA.SERVICE,/S/PROCESS,MB.OFFICER/123123,TSM \"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(TSM_URL, requestEntity, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            String body = responseEntity.getBody();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(body);

            String ofsResponse = root.path("ofsResponse").asText();

            return extractServiceControl(ofsResponse);
        } else {
            return "error: bad response";
        }
    } catch (Exception e) {
        e.printStackTrace();
        return "error";
    }
}

    private String extractServiceControl(String ofsResponse) {
    if (ofsResponse == null) return null;

    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("SERVICE\\.CONTROL:([^,]*)");
    java.util.regex.Matcher matcher = pattern.matcher(ofsResponse);

    if (matcher.find()) {
        return matcher.group(1).trim();
    }
    return null;
}

    
}

