package com.ricoh.DocuWareSoapWebService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.docuware.GetFileCabinetInfoRequest;
import org.example.docuware.GetFileCabinetInfoResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Endpoint
public class DocuWareEndpoint {

    private static final String NAMESPACE_URI = "http://www.example.org/docuware";
    private static final Logger logger = LoggerFactory.getLogger(DocuWareEndpoint.class);

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getFileCabinetInfoRequest")
    @ResponsePayload
    public GetFileCabinetInfoResponse getFileCabinetInfo(@RequestPayload GetFileCabinetInfoRequest request) {
    	
        GetFileCabinetInfoResponse response = new GetFileCabinetInfoResponse();
        
        try {
            // Extract data from the request
            String requestUrl = request.getRequestUrl();
            String username = request.getDwUsername();
            String password = request.getDwPassword();
            String fileCabinetId = request.getDwFileCabinetId();
            String searchDialogId = request.getDwSearchDialogId();
            String platform = request.getPlatform();
            String requestBody = request.getRequestBody();
            
            
            System.out.println(requestUrl + "\n" + username + "\n" + password + "\n" + fileCabinetId + "\n" + searchDialogId + "\n" + platform + "\n" + requestBody);
            
            // Log the request URL
            logger.debug("Request URL: {}", requestUrl);
            
            // Validate and fix the request URL if necessary
            /*if (!requestUrl.startsWith("http://") && !requestUrl.startsWith("https://")) {
                requestUrl = "http://" + requestUrl;
            }*/
            
            // Obtain IdentityService URL
            String identityServiceUrl = getIdentityServiceUrl(requestUrl , platform);
            if (identityServiceUrl == null) {
                response.setXmlResponse("Error: Unable to get IdentityService URL");
                return response;
            }
            
            // Obtain Token Endpoint
            String tokenEndpoint = getTokenEndpoint(identityServiceUrl);
            if (tokenEndpoint == null) {
                response.setXmlResponse("Error: Unable to get Token Endpoint");
                return response;
            }
            
            // Get Access Token
            String accessToken = getAccessToken(tokenEndpoint, username, password);
            if (accessToken == null) {
                response.setXmlResponse("Error: Unable to get Access Token");
                return response;
            }
            
            
            // Obtain File Cabinet Info
            String fileCabinetInfoResponse = getFileCabinetInfoResponse(requestUrl,platform, fileCabinetId, searchDialogId , accessToken ,requestBody);
            
            response.setXmlResponse(fileCabinetInfoResponse);
            
        } catch (Exception e) {
            logger.error("Exception occurred while processing the request", e);
            response.setXmlResponse("Error: " + e.getMessage());
        }
        
        return response;
    }
    
    private String getIdentityServiceUrl(String dwUrl , String platform) throws IOException, InterruptedException, URISyntaxException, ParseException {
    	
    	// Ensure the dwUrl starts with http:// or https://
        if (!dwUrl.startsWith("http://") && !dwUrl.startsWith("https://")) {
            dwUrl = "http://" + dwUrl;
        }

        //String url = dwUrl + "/" + platform + "/Home/IdentityServiceInfo";
        String url = "https://vamokuhlekhumalo.docuware.cloud/DocuWare/Platform/Home/IdentityServiceInfo";
        // Log the identity service URL
        logger.debug("Identity Service URL: {}", url);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Accept", "application/json")
                .GET()
                .build();
        
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        JSONParser parser = new JSONParser();
        JSONObject jsonResponse = (JSONObject) parser.parse(response.body());
        String identityServiceUrl = (String) jsonResponse.get("IdentityServiceUrl");
        
        System.out.println("url: " + url);
        System.out.println("Identity service url: " + identityServiceUrl);
        System.out.println("Identity service url: " + response.statusCode());
        
        return identityServiceUrl;
    }
    
    private String getTokenEndpoint(String identityServiceUrl) throws Exception {
        String url = identityServiceUrl + "/.well-known/openid-configuration";
        
        // Log the token endpoint URL
        logger.debug("Token Endpoint URL: {}", url);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Accept", "application/json")
                .GET()
                .build();
        
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        JSONParser parser = new JSONParser();
        JSONObject jsonResponse = (JSONObject) parser.parse(response.body());
        String tokenEndpoint = (String) jsonResponse.get("token_endpoint");
        
        System.out.println("Token endpoint: " + tokenEndpoint);
        
        return tokenEndpoint;
    }
    
    private String getAccessToken(String tokenEndpoint, String username, String password) throws Exception {
    	
    	Map<String, String> formParameters = Map.of(
    	        "grant_type", "password",
    	        "scope", "docuware.platform",
    	        "client_id", "docuware.platform.net.client",
    	        "username", username,
    	        "password", password
    	    );

    	    // Convert the map to URL-encoded form data
    	    String requestBody = formParameters.entrySet().stream()
    	        .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
    	        .collect(Collectors.joining("&"));
        
        // Log the access token request
        logger.debug("Access Token Request to: {}", tokenEndpoint);
        logger.debug("Request Body: {}", requestBody);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(tokenEndpoint))
                .header("Accept", "*/*")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        JSONParser parser = new JSONParser();
        JSONObject jsonResponse = (JSONObject) parser.parse(response.body());
        String access_token = (String) jsonResponse.get("access_token");
                
        System.out.println("Access token: " + access_token);
        
        return access_token;
    }
    
    private String getFileCabinetInfoResponse(String dwUrl, String platform, String dwFileCabinetId, String searchDialogId, String token, String requestBody) throws Exception {

        // Ensure the dwUrl starts with http:// or https://
        if (!dwUrl.startsWith("http://") && !dwUrl.startsWith("https://")) {
            dwUrl = "http://" + dwUrl;
        }

        String url = dwUrl + "/" + platform + "/FileCabinets/" + dwFileCabinetId + "/Query/DialogExpression?Count=5000&DialogId=" + searchDialogId;
        System.out.println("File cabinet url: " + url);
        logger.debug("File Cabinet Request URL: {}", url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        logger.debug("Full response status code: {}", response.statusCode());

        StringBuilder xmlBuilder = new StringBuilder();

        if (response.statusCode() == 200) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.body());

            if (rootNode.has("Items") && rootNode.get("Items").isArray()) {
                for (JsonNode itemNode : rootNode.get("Items")) {
                    if (itemNode.has("Fields") && itemNode.get("Fields").isArray()) {
                        for (JsonNode fieldNode : itemNode.get("Fields")) {
                            String fieldName = fieldNode.path("FieldName").asText();
                            String fieldValue = fieldNode.path("Item").asText();

                            // Append <fieldname>fieldValue</fieldname> to the XML string
                            xmlBuilder.append("<")
                                      .append(fieldName)
                                      .append(">")
                                      .append(fieldValue)
                                      .append("</")
                                      .append(fieldName)
                                      .append(">");
                        }
                    } else {
                        logger.warn("Fields array is missing or not an array in the response");
                    }
                }
            } else {
                logger.warn("Items array is missing or not an array in the response");
            }
        } else {
            logger.error("Failed to get File Cabinet Info, status code: {}", response.statusCode());
            return "<error>Failed to retrieve data</error>";
        }

        return xmlBuilder.toString();
    }


}
