package com.dusanbranovic.bookme.integrations.contollers;

import com.dusanbranovic.bookme.repository.FasiliityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FascilityControllerIT extends AbstractControllerIT {

    @Autowired private FasiliityRepository fascilityRepository;

    @Test
    void adminCanCreateAndReadPropertyFacilities() throws Exception {
        String name = "Rooftop garden " + UUID.randomUUID();

        mockMvc.perform(post("/api/fascilities")
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name));

        assertTrue(fascilityRepository.findByName(name).isPresent());
        mockMvc.perform(get("/api/fascilities").with(asOwner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == '" + name + "')]").exists());
    }

    @Test
    void invalidFacilityBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/fascilities")
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
