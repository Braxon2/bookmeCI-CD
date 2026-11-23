package com.dusanbranovic.bookme.ai.controller;

import com.dusanbranovic.bookme.ai.dto.response.UnitSearchResponse;
import com.dusanbranovic.bookme.ai.exception.*;
import com.dusanbranovic.bookme.ai.service.UnitSearchService;
import com.dusanbranovic.bookme.config.*;
import com.google.genai.errors.ApiException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.net.SocketTimeoutException;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UnitSearchController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, UnitSearchExceptionHandler.class})
class UnitSearchControllerTest {
    @Autowired WebApplicationContext context;
    private MockMvc mvc;
    @MockitoBean UnitSearchService service;
    @MockitoBean JwtService jwt;
    @MockitoBean UserDetailsService users;
    @MockitoBean RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test void existingBrowsingRolesCanInterpretAStatelessSearch() throws Exception {
        when(service.interpret(anyString(), any())).thenReturn(new UnitSearchResponse(null, "Add dates to your description."));
        for (String role : List.of("ADMIN", "OWNER", "USER")) {
            mvc.perform(post("/api/ai/unit-search").with(user("person").authorities(() -> role))
                            .contentType("application/json").content("{\"message\":\"parking\"}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.clarification").value("Add dates to your description."))
                    .andExpect(jsonPath("$.conversationId").doesNotExist());
        }
        verify(service, times(3)).interpret(eq("person"), any());
    }

    @Test void noAuthenticationOrWrongRoleCannotReachTheModel() throws Exception {
        mvc.perform(post("/api/ai/unit-search").contentType("application/json").content("{\"message\":\"hello\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/ai/unit-search").with(user("person").authorities(() -> "OTHER"))
                        .contentType("application/json").content("{\"message\":\"hello\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test void existingJwtFilterAcceptsTheBearerToken() throws Exception {
        var details = org.springframework.security.core.userdetails.User.withUsername("jwt-user").password("unused").authorities("USER").build();
        when(jwt.extractEmail("test-token")).thenReturn("jwt-user");
        when(users.loadUserByUsername("jwt-user")).thenReturn(details);
        when(jwt.isTokenValid("test-token", details)).thenReturn(true);
        when(service.interpret(eq("jwt-user"), any())).thenReturn(new UnitSearchResponse(null, "Add dates."));
        mvc.perform(post("/api/ai/unit-search").header("Authorization", "Bearer test-token")
                        .contentType("application/json").content("{\"message\":\"parking\"}"))
                .andExpect(status().isOk());
        verify(jwt).isTokenValid("test-token", details);
    }

    @Test void invalidMessageAndInvalidOptionalContextReturn400BeforeAi() throws Exception {
        for (String body : List.of("{}", "{\"message\":\" \"}", "{\"message\":\"" + "x".repeat(2001) + "\"}",
                "{\"message\":\"parking\",\"context\":{\"adults\":-2}}", "{\"message\":\"parking\",\"context\":{\"checkIn\":\"not-a-date\"}}", "{")) {
            mvc.perform(post("/api/ai/unit-search").with(user("person").authorities(() -> "USER"))
                            .contentType("application/json").content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("AI_INVALID_REQUEST"));
        }
        verifyNoInteractions(service);
    }

    @Test void providerErrorsAreNon200AndDoNotLeakBodies() throws Exception {
        var errors = List.of(new ApiException(429, "QUOTA", "SECRET"), new ApiException(503, "UNAVAILABLE", "SECRET"),
                new RuntimeException(new SocketTimeoutException("SECRET")), new IllegalStateException("SECRET"), UnitSearchException.invalid("Invalid dates"));
        int[] statuses = {429, 502, 504, 500, 400};
        for (int i = 0; i < errors.size(); i++) {
            doThrow(new RuntimeException(errors.get(i))).when(service).interpret(anyString(), any());
            mvc.perform(post("/api/ai/unit-search").with(user("person").authorities(() -> "USER"))
                            .contentType("application/json").content("{\"message\":\"parking\"}"))
                    .andExpect(status().is(statuses[i]))
                    .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("SECRET"))));
        }
    }
}
