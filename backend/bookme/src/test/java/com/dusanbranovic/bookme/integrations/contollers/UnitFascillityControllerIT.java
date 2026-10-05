package com.dusanbranovic.bookme.integrations.contollers;

import com.dusanbranovic.bookme.repository.UnitFascilityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UnitFascillityControllerIT extends AbstractControllerIT {

    @Autowired private UnitFascilityRepository unitFascilityRepository;

    @Test
    void adminCanCreateAndReadUnitFacilities() throws Exception {
        String name = "Coffee machine " + UUID.randomUUID();

        mockMvc.perform(post("/api/unit-fascilities")
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name));

        assertTrue(unitFascilityRepository.findByName(name).isPresent());
        mockMvc.perform(get("/api/unit-fascilities").with(asGuest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == '" + name + "')]").exists());
    }

    @Test
    void regularUserCannotCreateUnitFacility() throws Exception {
        mockMvc.perform(post("/api/unit-fascilities")
                        .with(asGuest())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Private sauna\"}"))
                .andExpect(status().isForbidden());
    }
}
