package com.transact.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

@Controller
@RequestMapping("/content")
public class JbossController {

    private final String JBossService = "jboss";
    private final String JBossHost = "localhost";
    private final int JBossPort = 8080;

    @GetMapping("/jboss")
    public String jbossPage(Model model, HttpServletRequest request) {
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

        Object log = request.getSession().getAttribute("jbossLog");
        model.addAttribute("jbossLog", log != null ? log.toString() : "");
        request.getSession().removeAttribute("jbossLog");

        // Detect if this is a direct browser request (not AJAX)
        String requestedWith = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(requestedWith);

        return isAjax ? "fragments/jboss-fragment" : "redirect:/transact-dashboard#jboss";
    }

    @PostMapping("/jboss/restart")
    public String restartJboss(HttpServletRequest request) {
        String output = executeCommand("sudo /bin/systemctl restart jboss");

        try {
            Thread.sleep(2000); // wait to stabilize
        } catch (InterruptedException ignored) {}

        request.getSession().setAttribute("jbossLog", "JBoss restart initiated.\n\n" + output);

        // 🔁 Redirect to layout with hash so JS loads the fragment correctly
        return "redirect:/transact-dashboard#jboss";
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

    private String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", command);
            builder.redirectErrorStream(true);
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
