package com.transact.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
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
        if ("Running".equals(status)) cssClass = "status-running";
        else if ("Initializing".equals(status)) cssClass = "status-initializing";
        else if ("Stopped".equals(status)) cssClass = "status-stopped";
        else cssClass = "status-error";

        model.addAttribute("jbossClass", cssClass);
        model.addAttribute("buttonDisabled", "Initializing".equals(status));
        model.addAttribute("jbossLog", getLogAndClear(request));

        return  "fragments/jboss-fragment"; // Load fragment only
    }

    @PostMapping("/jboss/restart")
    public String restartJboss(HttpServletRequest request) {
        String output = executeCommand("sudo /bin/systemctl restart jboss");
        System.out.println(output);
        request.getSession().setAttribute("jbossLog", "JBoss restart initiated.\n\n" + output);
        return "redirect:/content/jboss";
    }

    private String getLogAndClear(HttpServletRequest request) {
        Object log = request.getSession().getAttribute("jbossLog");
        request.getSession().removeAttribute("jbossLog");
        return log != null ? log.toString() : "";
    }

    private String checkJbossStatus() {
        return (isServiceActive() && isSocketOpen()) ? "Running"
                : isServiceActive() ? "Initializing"
                : "Stopped";
    }

    private boolean isServiceActive() {
        try {
            Process process = new ProcessBuilder("systemctl", "is-active", JBossService)
                    .redirectErrorStream(true).start();
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
        System.out.println(command);
        StringBuilder output = new StringBuilder();
        try {
            Process process = new ProcessBuilder("bash", "-c", command).start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) output.append(line).append("\n");
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            output.append("Error: ").append(e.getMessage());
        }
        return output.toString();
    }
}
