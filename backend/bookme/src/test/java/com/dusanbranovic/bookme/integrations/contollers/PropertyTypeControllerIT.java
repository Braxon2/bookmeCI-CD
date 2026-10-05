package com.dusanbranovic.bookme.integrations.contollers;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PropertyTypeControllerIT extends AbstractControllerIT {

    @Test
    void adminCanCreateAndReadPropertyTypes() throws Exception {
        String name = "Eco lodge " + UUID.randomUUID();

        mockMvc.perform(post("/api/property-type")
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name));

        assertTrue(propertyTypeRepository.findByName(name).isPresent());
        mockMvc.perform(get("/api/property-type").with(asGuest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == '" + name + "')]").exists());
    }

    @Test
    void unauthenticatedRequestIsForbidden() throws Exception {
        mockMvc.perform(get("/api/property-type"))
                .andExpect(status().isForbidden());
    }
}
