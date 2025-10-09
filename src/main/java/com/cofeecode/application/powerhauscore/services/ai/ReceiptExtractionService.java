package com.cofeecode.application.powerhauscore.services.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Thin client around a receipt/expense extraction endpoint.
 */
@Service
public class ReceiptExtractionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiptExtractionService.class);

    private final ObjectMapper objectMapper;
    private final Optional<URI> endpoint;
    private final String apiKey;
    private final HttpClient httpClient;

    public ReceiptExtractionService(ObjectMapper objectMapper,
                                    @Value("${ai.receipt-extraction.url:}") String endpoint,
                                    @Value("${ai.receipt-extraction.api-key:}") String apiKey) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.endpoint = (endpoint == null || endpoint.isBlank()) ? Optional.empty() : Optional.of(URI.create(endpoint));
    }

    public boolean isConfigured() {
        return endpoint.isPresent();
    }

    public Optional<ReceiptExtractionResult> extract(Path file) throws IOException {
        if (!isConfigured()) {
            LOGGER.debug("Receipt extraction endpoint not configured, skipping call");
            return Optional.empty();
        }

        String boundary = "----PowerHausReceipt" + System.currentTimeMillis();
        HttpRequest.BodyPublisher bodyPublisher = buildMultipartBody(file, boundary);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(endpoint.get())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .timeout(Duration.ofMinutes(2))
                .POST(bodyPublisher);

        if (apiKey != null && !apiKey.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + apiKey);
        }

        HttpRequest request = requestBuilder.build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                String body = response.body();
                if (body == null || body.isBlank()) {
                    LOGGER.warn("Receipt extraction endpoint returned an empty body");
                    return Optional.empty();
                }
                JsonNode root = objectMapper.readTree(body);
                ReceiptExtractionResult result = ReceiptExtractionResult.fromJson(root);
                return Optional.of(result);
            }

            LOGGER.error("Receipt extraction request failed with status {} and body: {}", statusCode, response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while calling receipt extraction endpoint", e);
        }

        return Optional.empty();
    }

    private HttpRequest.BodyPublisher buildMultipartBody(Path file, String boundary) throws IOException {
        String fileName = file.getFileName().toString();
        String contentType = Files.probeContentType(file);
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        List<byte[]> byteArrays = new ArrayList<>();
        byteArrays.add(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        byteArrays.add(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        byteArrays.add(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        byteArrays.add(Files.readAllBytes(file));
        byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
        byteArrays.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }
}

