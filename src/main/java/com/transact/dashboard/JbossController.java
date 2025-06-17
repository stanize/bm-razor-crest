package com.transact.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("/content")
public class JbossController {

    private final String JBossService = "jboss";
    private final String JBossHost = "localhost";
    private final int JBossPort = 8080;

    @GetMapping("/jboss")
    public String jbossPage(Model model) {
        String status = checkJbossStatus();
        model.addAttribute("jbossStatus", status);

        String cssClass;
        if ("Running".equals(status)) {
            cssClass = "status-running";
        } else if ("Initializing".equals(status)) {
            cssClass = "status-initializing";
        } else if ("Stopped".equals(status)) {
            cssClass = "status-stopped";
        } else {
            cssClass = "status-error";
        }

        model.addAttribute("jbossClass", cssClass);
        model.addAttribute("buttonDisabled", "Initializing".equals(status));

        return "fragments/jboss-fragment";
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
    @PostMapping("/jboss/restart")
    public String restartJboss(Model model) {
        executeCommand("sudo systemctl restart jboss");

        // Wait briefly for JBoss to come back up
        try {
            Thread.sleep(2000); // 2 seconds (optional, gives the socket time to open)
        } catch (InterruptedException ignored) {}

        // Redirect back to refresh the status
        return "redirect:/content/jboss";
    }

    private String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", command);
            Process process = builder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            output.append("Error: ").append(e.getMessage());
        }
        return output.toString();
    }
}
