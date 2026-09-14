package com.example.promptdb.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurable base URL and timeouts for the upstream prompt_db API.
 * Set via the {@code app.api.base-url} property (or the {@code API_BASE_URL} environment variable).
 */
@ConfigurationProperties(prefix = "app.api")
public class ApiProperties {

    /** Base URL of the prompt_db API, e.g. http://localhost:8081 */
    private String baseUrl = "http://localhost:8081";

    private Duration connectTimeout = Duration.ofSeconds(5);

    private Duration readTimeout = Duration.ofSeconds(15);

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }
}
