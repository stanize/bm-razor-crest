package com.transact.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TransactAuthService {

    private static final String TRANSACT_AUTH_HEADER = "Basic dGFmai5hZG1pbjpBWElAZ3RwcXJYNC=="; // <-- your real auth header
    private static final String TRANSACT_URL = "http://localhost:8080/TAFJRestServices/resources/ofs"; // <-- your real TSM URL

    public String authenticateUser(String username, String password) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", TRANSACT_AUTH_HEADER);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setCacheControl("no-cache");

            String ofsRequest = String.format("TSA.SERVICE,/S/PROCESS,%s/%s,TSM ", username, password);
            String jsonBody = String.format("{\"ofsRequest\":\"%s\"}", ofsRequest);

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(TRANSACT_URL, requestEntity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                String body = responseEntity.getBody();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(body);

                String ofsResponse = root.path("ofsResponse").asText();

                ofsResponse = extractServiceControl(ofsResponse);

                if (ofsResponse == null) return "UNKNOWN";

                int equalsIndex = ofsResponse.indexOf('=');
                if (equalsIndex != -1 && equalsIndex + 1 < ofsResponse.length()) {
                    return ofsResponse.substring(equalsIndex + 1).trim();
                }
                return "UNKNOWN";
            } else {
                return "error: bad response";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    private String extractServiceControl(String ofsResponse) {
        // Assuming you're parsing TSA.SERVICE response here
        // Implement your extract logic as per your response format
        return ofsResponse;
    }
}
