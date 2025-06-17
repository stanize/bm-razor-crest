package com.transact.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.Socket;
import java.net.InetSocketAddress;

@Controller
@RequestMapping("/jboss")
public class JbossController {

    @PostMapping("/restart")
    public ResponseEntity<String> restartJboss() {
        String output = executeCommand("sudo systemctl restart jboss");
        return ResponseEntity.ok("JBoss restart initiated.\n\n" + output);
    }

    @PostMapping("/status")
    public ResponseEntity<String> checkStatus() {
        String service = "jboss";

        String status = "Stopped";
        try {
            Process process = new ProcessBuilder("systemctl", "is-active", service).start();
            process.waitFor();
            String state = new String(process.getInputStream().readAllBytes()).trim();

            boolean isSocketOpen = false;
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("localhost", 8080), 1000);
                isSocketOpen = true;
            } catch (IOException ignored) {}

            if ("active".equals(state) && isSocketOpen) {
                status = "Running";
            } else if ("active".equals(state)) {
                status = "Initializing";
            }
        } catch (Exception e) {
            status = "Error";
        }

        return ResponseEntity.ok(status);
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
