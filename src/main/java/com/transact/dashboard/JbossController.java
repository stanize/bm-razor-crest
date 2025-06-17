package com.transact.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

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
}
