package dev.cdavidsv.auth.core.service;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Service;

@Service
public class UserAgentServiceImpl implements UserAgentService {
    private final UserAgentAnalyzer analyzer;

    public UserAgentServiceImpl() {
        this.analyzer = UserAgentAnalyzer
                .newBuilder()
                .hideMatcherLoadStats()
                .withCache(10000)
                .build();
    }

    /**
     * @param userAgentString the user agent string to parse
     * @return UserAgentInfo object containing parsed user agent information
     */
    @Override
    public UserAgentInfo parseUserAgent(String userAgentString) {
        UserAgent parsed = analyzer.parse(userAgentString);

        return new UserAgentInfo(
            getValueHelper(parsed, UserAgent.DEVICE_CLASS, "Unknown Device"),
            getValueHelper(parsed, UserAgent.DEVICE_NAME, "Unknown Device Name"),
            getValueHelper(parsed, UserAgent.DEVICE_BRAND, "Unknown Brand"),
            getValueHelper(parsed, UserAgent.OPERATING_SYSTEM_NAME, "Unknown OS"),
            getValueHelper(parsed, UserAgent.OPERATING_SYSTEM_VERSION, "Unknown OS Version"),
            getValueHelper(parsed, UserAgent.AGENT_NAME, "Unknown Agent Name"),
            getValueHelper(parsed, UserAgent.AGENT_VERSION, "Unknown Agent Version")
        );
    }

    private String getValueHelper(UserAgent parsed, String field, String defaultValue) {
        String value = parsed.getValue(field);
        return (value == null || value.isEmpty() || value.equals("??")) ? defaultValue : value;
    }
}
