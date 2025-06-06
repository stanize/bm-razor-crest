package com.transact.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class TransactAuthenticationProvider implements AuthenticationProvider {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper(); // for JSON parsing

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        boolean isAuthenticated = authenticateWithTransact(username, password);

        if (isAuthenticated) {
            return new UsernamePasswordAuthenticationToken(
                    username, password, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        } else {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    private boolean authenticateWithTransact(String username, String password) {
        try {
            String url = "http://localhost:8080/TAFJRestServices/resources/ofs";

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setCacheControl("no-cache");

            // Basic Auth header (static admin user/pass)
            String authStr = "tafj.admin:AXI@gtpqrY4"; // <- Change if different
            String base64Creds = Base64.getEncoder().encodeToString(authStr.getBytes());
            headers.set("Authorization", "Basic " + base64Creds);

            // Dynamic OFS Request with username/password
            String ofsRequest = String.format(
                    "ENQUIRY.SELECT,,%s/%s,%%USER,SIGN.ON.NAME:=%s",
                    username,
                    password,
                    username
            );

            Map<String, String> requestBody = Collections.singletonMap("ofsRequest", ofsRequest);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            // POST Request
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                if (responseBody != null) {
                    Map<?, ?> jsonMap = objectMapper.readValue(responseBody, Map.class);
                    String ofsResponse = (String) jsonMap.get("ofsResponse");

                    if (ofsResponse != null) {
                        // Check for negative codes (-1, -2, -3, etc.)
                        if (containsNegativeCode(ofsResponse)) {
                            return false; // login fail
                        }
                        // Check if user data is present
                        if (ofsResponse.contains("@ID::User ID") && ofsResponse.contains("SIGN.ON.NAME")) {
                            return true; // login success
                        }
                    }
                }
            }

            return false;

        } catch (Exception e) {
            // Log error for debugging (Optional)
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to check for -1, -2, -3, etc.
    private boolean containsNegativeCode(String response) {
        return response.matches(".*\\-\\d+.*");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
