package com.dusanbranovic.bookme.integrations.contollers;

import com.dusanbranovic.bookme.models.Addon;
import com.dusanbranovic.bookme.repository.AddonRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AddonControllerIT extends AbstractControllerIT {

    @Autowired private AddonRepository addonRepository;

    @Test
    void adminCanCreateAndReadAddons() throws Exception {
        String name = "Airport transfer " + UUID.randomUUID();

        mockMvc.perform(post("/api/addons")
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name));

        assertTrue(addonRepository.findByName(name).isPresent());
        mockMvc.perform(get("/api/addons").with(asGuest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == '" + name + "')]").exists());
    }

    @Test
    void duplicateAddonReturnsConflict() throws Exception {
        String name = "Breakfast " + UUID.randomUUID();
        addonRepository.save(new Addon(name));

        mockMvc.perform(post("/api/addons")
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void ownerCannotCreateAnAddon() throws Exception {
        mockMvc.perform(post("/api/addons")
                        .with(asOwner())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Forbidden addon\"}"))
                .andExpect(status().isForbidden());
    }
}
