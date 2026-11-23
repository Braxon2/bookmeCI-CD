package com.dusanbranovic.bookme.ai.service;

import com.dusanbranovic.bookme.ai.dto.request.UnitSearchRequest;
import com.dusanbranovic.bookme.ai.dto.response.FacilityCatalog;
import com.dusanbranovic.bookme.ai.dto.response.UnitSearchResponse;
import com.dusanbranovic.bookme.ai.exception.UnitSearchException;
import com.dusanbranovic.bookme.ai.tools.UnitSearchTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallLimitExceededException;
import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class UnitSearchAiClient {
    private final ObjectProvider<ChatClient.Builder> builders;
    private final String systemPrompt;

    public UnitSearchAiClient(ObjectProvider<ChatClient.Builder> builders,
                              @Value("classpath:ai/unit-search-prompt.st") Resource prompt) throws IOException {
        this.builders = builders;
        this.systemPrompt = prompt.getContentAsString(StandardCharsets.UTF_8);
    }

    public UnitSearchResponse interpret(UnitSearchRequest request, FacilityCatalog catalog, UnitSearchTools tools) {
        var builder = builders.getIfAvailable();
        if (builder == null) throw new UnitSearchException(HttpStatus.SERVICE_UNAVAILABLE, "AI_DISABLED", "Smart filters are disabled. Configure the provider and API key.");
        var manager = DefaultToolCallingManager.builder().maxTotalToolCalls(1).maxCallsPerTool(1)
                .toolExecutionExceptionProcessor(new DefaultToolExecutionExceptionProcessor(true)).build();
        var client = builder.defaultAdvisors(ToolCallingAdvisor.builder().toolCallingManager(manager).build()).build();
        var response = client.prompt().system(systemPrompt + "\nToday in Europe/Belgrade: " + LocalDate.now(ZoneId.of("Europe/Belgrade")))
                .user("Current form values (may be incomplete): " + request.context()
                        + "\nActual facility catalog: " + catalog + "\nUser's search description: " + request.message())
                .tools(tools).call().chatResponse();
        if (response == null || response.getResult() == null || response.hasToolCalls()) throw incomplete();
        String finish = response.getResult().getMetadata().getFinishReason();
        if ("MAX_TOKENS".equalsIgnoreCase(finish) || ToolCallLimitExceededException.FINISH_REASON.equals(finish)) throw incomplete();
        if (tools.filters() != null) return new UnitSearchResponse(tools.filters(), null);
        String clarification = response.getResult().getOutput().getText();
        if (clarification == null || clarification.isBlank() || clarification.length() > 2000) throw incomplete();
        return new UnitSearchResponse(null, clarification);
    }

    private UnitSearchException incomplete() {
        return new UnitSearchException(HttpStatus.BAD_GATEWAY, "AI_INCOMPLETE_RESPONSE", "Smart filters could not interpret this search. Try a shorter, more specific description.");
    }
}
