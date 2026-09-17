package dev.cdavidsv.auth.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.JsonNodeException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@Service
public class IpGeolocationServiceImpl implements IpGeolocationService {
    private final Logger logger = LoggerFactory.getLogger(IpGeolocationServiceImpl.class);
    private final String API_BASE_URL = "http://ip-api.com/json/";

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient;


    public IpGeolocationServiceImpl(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * @param ipAddress IPv4 address to geolocate.
     * @return GeolocationResult containing country, region, and city for the given IP address. Returns null if geolocation is not available.
     */
    @Override
    public Optional<GeolocationResult> getIpGeolocation(String ipAddress) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + ipAddress))
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                JsonNode jsonNode = mapper.readTree(responseBody);

                if (jsonNode.path("status").asString().equals("fail")) {
                    logger.warn("Failed to geolocate IP address: {}. Reason: {}", ipAddress, jsonNode.path("message").asString());
                    return Optional.empty();
                }

                String country = jsonNode.path("country").asString();
                String region = jsonNode.path("regionName").asString();
                String city = jsonNode.path("city").asString();

                return Optional.of(new GeolocationResult(country, region, city));
            }
        } catch (JsonNodeException e) {
            logger.error("Error while parsing geolocation response for IP address: {}", ipAddress, e);
        } catch (IOException | InterruptedException e) {
            logger.error("Error while geolocating IP address: {}", ipAddress, e);
        }
        return Optional.empty();
    }
}
