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

@Controller
@RequestMapping("/transact-dashboard")
public class DashboardController {

    private final String JBossService = "jboss";
    private final String JBossHost = "localhost";
    private final int JBossPort = 8080;

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
}

