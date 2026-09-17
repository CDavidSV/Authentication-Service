package dev.cdavidsv.auth.core.service;

import java.util.Optional;

public interface IpGeolocationService {
    record GeolocationResult(String country, String region, String city) { }

    Optional<GeolocationResult> getIpGeolocation(String ipAddress);
}
