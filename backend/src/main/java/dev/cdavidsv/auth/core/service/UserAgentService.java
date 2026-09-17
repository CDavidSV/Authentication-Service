package dev.cdavidsv.auth.core.service;

public interface UserAgentService {
    record UserAgentInfo(
            String deviceClass,
            String deviceName,
            String deviceBrand,
            String operatingSystem,
            String operatingSystemVersion,
            String browser,
            String browserVersion
    ) {}

    UserAgentInfo parseUserAgent(String userAgentString);
}
