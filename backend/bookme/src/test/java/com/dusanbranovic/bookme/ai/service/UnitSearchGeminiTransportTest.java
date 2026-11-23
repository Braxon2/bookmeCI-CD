package com.dusanbranovic.bookme.ai.service;

import com.dusanbranovic.bookme.ai.dto.request.UnitSearchRequest;
import com.dusanbranovic.bookme.ai.dto.response.*;
import com.dusanbranovic.bookme.ai.tools.UnitSearchTools;
import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UnitSearchGeminiTransportTest {
    @SuppressWarnings("unchecked")
    @Test void realGoogleSdkInvokesOnlyTheFilterToolWithOneHttpRequest() throws Exception {
        var requests = new CopyOnWriteArrayList<String>();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requests.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] body = """
                    {"candidates":[{"content":{"role":"model","parts":[{"functionCall":{"name":"applyUnitSearchFilters","args":{"criteria":{"city":"Belgrade","country":"Serbia","checkIn":"2026-12-10","checkOut":"2026-12-15","adults":2,"children":0,"maxTotalPrice":300}}},"thoughtSignature":"dGVzdA=="}]},"finishReason":"STOP"}]}
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) { output.write(body); }
        });
        server.start();
        try (Client google = Client.builder().apiKey("local-test-only").vertexAI(false)
                .httpOptions(HttpOptions.builder().baseUrl("http://127.0.0.1:" + server.getAddress().getPort())
                        .timeout(3000).retryOptions(HttpRetryOptions.builder().attempts(1).build()).build()).build()) {
            var model = GoogleGenAiChatModel.builder().genAiClient(google)
                    .options(GoogleGenAiChatOptions.builder().model("gemini-3.1-flash-lite").maxOutputTokens(512).build()).build();
            ObjectProvider<ChatClient.Builder> builders = mock(ObjectProvider.class);
            when(builders.getIfAvailable()).thenAnswer(ignored -> ChatClient.builder(model));
            var filterService = mock(UnitSearchFilterService.class);
            var filters = new UnitSearchFilters("Belgrade", "Serbia", LocalDate.of(2026,12,10), LocalDate.of(2026,12,15), 2, 0, 300.0, List.of(), List.of());
            when(filterService.resolve(any())).thenReturn(filters);
            var client = new UnitSearchAiClient(builders, new ClassPathResource("ai/unit-search-prompt.st"));
            var result = client.interpret(new UnitSearchRequest("Pronađi smeštaj", null), new FacilityCatalog(List.of(), List.of(), false), new UnitSearchTools(filterService));
            assertEquals(filters, result.filters());
            assertEquals(1, requests.size());
            assertTrue(requests.getFirst().contains("applyUnitSearchFilters"));
            assertFalse(requests.getFirst().contains("checkAvailability"));
            assertFalse(requests.getFirst().contains("calculatePrice"));
        } finally { server.stop(0); }
    }
}
