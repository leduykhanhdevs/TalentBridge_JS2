package vn.talentbridge.adapter.out.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Shared Gemini transport adapter. API credentials are sent only in the request header. */
@Component
public class GeminiApiClient {

    private final String apiKey;
    private final String apiBaseUrl;
    private final RestClient restClient;

    @Autowired
    public GeminiApiClient(
            @Value("${talentbridge.gemini.api-key:}") String apiKey,
            @Value("${talentbridge.gemini.api-base-url:https://generativelanguage.googleapis.com/v1beta}") String apiBaseUrl,
            @Value("${talentbridge.gemini.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${talentbridge.gemini.read-timeout-ms:8000}") int readTimeoutMs
    ) {
        this(apiKey, apiBaseUrl, connectTimeoutMs, readTimeoutMs, null);
    }

    public GeminiApiClient(
            String apiKey,
            String apiBaseUrl,
            int connectTimeoutMs,
            int readTimeoutMs,
            RestClient restClient
    ) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.apiBaseUrl = apiBaseUrl == null ? "" : apiBaseUrl.replaceAll("/+$", "");
        if (restClient != null) {
            this.restClient = restClient;
        } else {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(Duration.ofMillis(Math.max(1, connectTimeoutMs)));
            requestFactory.setReadTimeout(Duration.ofMillis(Math.max(1, readTimeoutMs)));
            this.restClient = RestClient.builder().requestFactory(requestFactory).build();
        }
    }

    public boolean isConfigured() {
        return !apiKey.isBlank() && !apiKey.startsWith("YOUR_");
    }

    public String generateContent(String model, Object requestPayload) {
        if (!isConfigured()) {
            throw new IllegalStateException("Gemini API key is not configured");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Gemini model is not configured");
        }

        String encodedModel = UriUtils.encodePathSegment(model.trim(), StandardCharsets.UTF_8);
        URI endpoint = URI.create(apiBaseUrl + "/models/" + encodedModel + ":generateContent");
        byte[] responseBytes = restClient.post()
                .uri(endpoint)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestPayload)
                .retrieve()
                .body(byte[].class);

        if (responseBytes == null || responseBytes.length == 0) {
            throw new IllegalStateException("Gemini returned an empty response");
        }
        return new String(responseBytes, StandardCharsets.UTF_8);
    }
}
