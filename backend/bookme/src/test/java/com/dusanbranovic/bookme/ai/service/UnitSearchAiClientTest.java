package com.dusanbranovic.bookme.ai.service;

import com.dusanbranovic.bookme.ai.dto.request.*;
import com.dusanbranovic.bookme.ai.dto.response.*;
import com.dusanbranovic.bookme.ai.exception.UnitSearchException;
import com.dusanbranovic.bookme.ai.tools.UnitSearchTools;
import org.junit.jupiter.api.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UnitSearchAiClientTest {
    private final ChatModel model = mock(ChatModel.class);
    private final UnitSearchFilterService filterService = mock(UnitSearchFilterService.class);
    private final FacilityCatalog catalog = new FacilityCatalog(List.of("Parking"), List.of("Wi-Fi"), false);

    @BeforeEach void setup() { when(model.getOptions()).thenReturn(ToolCallingChatOptions.builder().build()); }

    @SuppressWarnings("unchecked")
    private UnitSearchAiClient client() throws Exception {
        ObjectProvider<ChatClient.Builder> builders = mock(ObjectProvider.class);
        when(builders.getIfAvailable()).thenAnswer(ignored -> ChatClient.builder(model));
        return new UnitSearchAiClient(builders, new ClassPathResource("ai/unit-search-prompt.st"));
    }

    @Test void appliesOnlyJavaValidatedFiltersWithOneProviderCall() throws Exception {
        var filters = new UnitSearchFilters("Belgrade", "Serbia", LocalDate.of(2026,12,10), LocalDate.of(2026,12,15), 2, 0, 300.0, List.of(7L), List.of());
        when(filterService.resolve(any())).thenReturn(filters);
        when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(new Generation(AssistantMessage.builder().content("")
                .toolCalls(List.of(new AssistantMessage.ToolCall("call-1", "function", "applyUnitSearchFilters", """
                        {"criteria":{"city":"Belgrade","country":"Serbia","checkIn":"2026-12-10","checkOut":"2026-12-15","adults":2,"children":0,"maxTotalPrice":300,"propertyFacilities":["Parking"]}}
                        """))).build()))));
        var result = client().interpret(new UnitSearchRequest("Find units with parkng", null), catalog, new UnitSearchTools(filterService));
        assertEquals(filters, result.filters());
        assertNull(result.clarification());
        verify(filterService).resolve(argThat(criteria -> criteria.adults() == 2 && criteria.propertyFacilities().equals(List.of("Parking"))));
        verify(model).call(any(Prompt.class));
    }

    @Test void missingInformationReturnsAnInstructionWithoutApplyingAnything() throws Exception {
        when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("Dodajte datume i ponovite pretragu.")))));
        var result = client().interpret(new UnitSearchRequest("Smeštaj u Beogradu", null), catalog, new UnitSearchTools(filterService));
        assertNull(result.filters());
        assertTrue(result.clarification().startsWith("Dodajte"));
        verifyNoInteractions(filterService);
        verify(model).call(any(Prompt.class));
    }

    @Test void currentFormValuesAreIncludedInTheSingleRequest() throws Exception {
        var context = new UnitSearchContext("Belgrade", "Serbia", LocalDate.of(2026,12,10), LocalDate.of(2026,12,15), 2, 1, null, List.of(), List.of());
        when(model.call(any(Prompt.class))).thenAnswer(invocation -> {
            Prompt prompt = invocation.getArgument(0);
            String input = prompt.getUserMessage().getText();
            assertTrue(input.contains("Belgrade"));
            assertTrue(input.contains("2026-12-10"));
            assertTrue(input.contains("Wi-Fi"));
            return new ChatResponse(List.of(new Generation(new AssistantMessage("Choose a facility."))));
        });
        client().interpret(new UnitSearchRequest("with wifi", context), catalog, new UnitSearchTools(filterService));
        verify(model).call(any(Prompt.class));
    }

    @Test void invalidToolArgumentsAbortInsteadOfReturningSuccess() throws Exception {
        when(filterService.resolve(any())).thenThrow(UnitSearchException.invalid("Invalid dates"));
        when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(new Generation(AssistantMessage.builder().content("")
                .toolCalls(List.of(new AssistantMessage.ToolCall("call-1", "function", "applyUnitSearchFilters", "{\"criteria\":{}}"))).build()))));
        assertThrows(RuntimeException.class, () -> client().interpret(new UnitSearchRequest("search", null), catalog, new UnitSearchTools(filterService)));
        verify(model).call(any(Prompt.class));
    }
}
