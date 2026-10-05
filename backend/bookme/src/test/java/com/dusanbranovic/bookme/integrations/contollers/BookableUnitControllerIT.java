package com.dusanbranovic.bookme.integrations.contollers;

import com.dusanbranovic.bookme.models.Addon;
import com.dusanbranovic.bookme.models.UnitFascillity;
import com.dusanbranovic.bookme.repository.AddonMappingRepository;
import com.dusanbranovic.bookme.repository.AddonRepository;
import com.dusanbranovic.bookme.repository.PeriodPriceRepository;
import com.dusanbranovic.bookme.repository.UnitFascilityRepository;
import com.dusanbranovic.bookme.repository.UnitFascillityMappingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookableUnitControllerIT extends AbstractControllerIT {

    @Autowired private PeriodPriceRepository periodPriceRepository;
    @Autowired private AddonRepository addonRepository;
    @Autowired private AddonMappingRepository addonMappingRepository;
    @Autowired private UnitFascilityRepository unitFascilityRepository;
    @Autowired private UnitFascillityMappingRepository mappingRepository;

    @Test
    void ownerCanAddAndReadUnitPeriodPrice() throws Exception {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(7);
        String body = """
                {"pricePerNight":125.5,"startDate":"%s","endDate":"%s","season":"Summer"}
                """.formatted(start, end);

        mockMvc.perform(post("/api/units/{id}/add-price", unit.getPublicId())
                        .with(asOwner())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pricePerNight").value(125.5))
                .andExpect(jsonPath("$.season").value("Summer"));

        assertEquals(1, periodPriceRepository.findAll().stream()
                .filter(price -> price.getBookableUnit().getId().equals(unit.getId()))
                .count());
        entityManager.flush();
        entityManager.clear();
        mockMvc.perform(get("/api/units/{id}/period-prices", unit.getPublicId()).with(asGuest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pricePerNight").value(125.5));
    }

    @Test
    void ownerCanAttachAndRemoveAnAddon() throws Exception {
        Addon addon = addonRepository.save(new Addon("Late checkout " + UUID.randomUUID()));

        mockMvc.perform(post("/api/units/{unitId}/addons", unit.getPublicId())
                        .with(asOwner())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + addon.getId() + ",\"name\":\"" + addon.getName() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(addon.getName()))
                .andExpect(jsonPath("$.perNight").value(false));

        assertTrue(addonMappingRepository.findActiveByAddonAndUnit(unit.getPublicId(), addon.getId()).isPresent());
        mockMvc.perform(delete("/api/units/{unitId}/addons/{addonId}", unit.getPublicId(), addon.getId())
                        .with(asOwner()))
                .andExpect(status().isNoContent());
        assertTrue(addonMappingRepository.findActiveByAddonAndUnit(unit.getPublicId(), addon.getId()).isEmpty());
    }

    @Test
    void ownerCanAssignFacilitiesToAUnit() throws Exception {
        UnitFascillity facility = unitFascilityRepository.save(
                new UnitFascillity("Sea view " + UUID.randomUUID())
        );

        mockMvc.perform(post("/api/units/{unitId}/add-unit-facilities", unit.getPublicId())
                        .with(asOwner())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"facilityIds\":[" + facility.getId() + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unitId").value(unit.getPublicId().toString()))
                .andExpect(jsonPath("$.facilities[0].id").value(facility.getId()));

        assertTrue(mappingRepository.findAll().stream().anyMatch(mapping ->
                mapping.getBookableUnit().getId().equals(unit.getId())
                        && mapping.getUnitFascillity().getId().equals(facility.getId())
        ));
    }

    @Test
    void regularUserCannotAddUnitPrice() throws Exception {
        mockMvc.perform(post("/api/units/{id}/add-price", unit.getPublicId())
                        .with(asGuest())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
