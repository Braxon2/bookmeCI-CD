package com.dusanbranovic.bookme.ai.service;

import com.dusanbranovic.bookme.ai.dto.request.UnitSearchRequest;
import com.dusanbranovic.bookme.ai.dto.response.FacilityCatalog;
import com.dusanbranovic.bookme.ai.dto.response.UnitSearchResponse;
import com.dusanbranovic.bookme.ai.exception.UnitSearchException;
import com.dusanbranovic.bookme.ai.tools.UnitSearchTools;
import com.dusanbranovic.bookme.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.List;

class UnitSearchServiceTest {
    private final UnitSearchAiClient client = mock(UnitSearchAiClient.class);
    private final UnitSearchFilterService filters = mock(UnitSearchFilterService.class);
    private final RateLimitingService rateLimits = mock(RateLimitingService.class);
    private final Bucket bucket = mock(Bucket.class);
    private final UnitSearchService service = new UnitSearchService(client, filters, rateLimits);

    @Test void independentRequestsHaveFreshToolsAndNoConversationState() {
        when(rateLimits.resolveBucket("ai-unit-search:alice")).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);
        var catalog = new FacilityCatalog(List.of("Parking"), List.of(), false);
        when(filters.catalog()).thenReturn(catalog);
        when(client.interpret(any(), eq(catalog), any())).thenReturn(new UnitSearchResponse(null, "Add dates."));
        var request = new UnitSearchRequest("Parking", null);
        service.interpret("alice", request);
        service.interpret("alice", request);
        var tools = ArgumentCaptor.forClass(UnitSearchTools.class);
        verify(client, times(2)).interpret(eq(request), eq(catalog), tools.capture());
        assertNotSame(tools.getAllValues().get(0), tools.getAllValues().get(1));
        assertNull(tools.getValue().filters());
    }

    @Test void rateLimitStopsBeforeCatalogOrModelAccess() {
        when(rateLimits.resolveBucket("ai-unit-search:alice")).thenReturn(bucket);
        assertEquals(429, assertThrows(UnitSearchException.class,
                () -> service.interpret("alice", new UnitSearchRequest("Parking", null))).status().value());
        verifyNoInteractions(filters, client);
    }
}
