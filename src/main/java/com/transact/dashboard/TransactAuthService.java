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

            String ofsRequest = String.format("ENQUIRY.SELECT,,%s/%s,%%USER,SIGN.ON.NAME:=%s", username, password, username);
            String jsonBody = String.format("{\"ofsRequest\":\"%s\"}", ofsRequest);

            System.out.println("========= OFS REQUEST =========");
            System.out.println("POST URL: " + TRANSACT_URL);
            System.out.println("POST Headers: " + headers);
            System.out.println("POST Body: " + jsonBody);
            System.out.println("================================");

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(TRANSACT_URL, requestEntity, String.class);

//            System.out.println("========= OFS RESPONSE =========");
//            System.out.println("HTTP Status: " + responseEntity.getStatusCode());
//            System.out.println("Response Body: " + responseEntity.getBody());
//            System.out.println("=================================");


            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                String body = responseEntity.getBody();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(body);

                String ofsResponse = root.path("ofsResponse").asText();

//                System.out.println("========= Parsed OFS Response =========");
//                System.out.println(ofsResponse);
//                System.out.println("=======================================");

// Check if response has any negative indicator
                if (ofsResponse.contains("//-")) {
                    return "FAILED";
                } else {
                    return "SUCCESS";
                }
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
