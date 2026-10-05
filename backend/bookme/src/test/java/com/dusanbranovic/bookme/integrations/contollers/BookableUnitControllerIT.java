package com.dusanbranovic.bookme.integrations.contollers;

import com.dusanbranovic.bookme.models.Addon;
import com.dusanbranovic.bookme.models.AddonMapping;
import com.dusanbranovic.bookme.models.PeriodPrice;
import com.dusanbranovic.bookme.models.PeriodPriceAddon;
import com.dusanbranovic.bookme.models.UnitFascillity;
import com.dusanbranovic.bookme.repository.AddonMappingRepository;
import com.dusanbranovic.bookme.repository.AddonRepository;
import com.dusanbranovic.bookme.repository.PeriodPriceRepository;
import com.dusanbranovic.bookme.repository.PeriodPriceAddonRepository;
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
    @Autowired private PeriodPriceAddonRepository periodPriceAddonRepository;
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

    @Test
    void overlappingPricesAreAppliedConsistentlyInSearchAndBooking() throws Exception {
        LocalDate start = LocalDate.of(2026, 10, 9);
        LocalDate end = LocalDate.of(2026, 10, 12);
        String city = "OverlapCity-" + UUID.randomUUID();
        property.setCity(city);
        propertyRepository.save(property);

        PeriodPrice october = periodPriceRepository.save(new PeriodPrice(
                unit, 70, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), "October"
        ));
        PeriodPrice special = periodPriceRepository.save(new PeriodPrice(
                unit, 100, LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 10), "Special"
        ));
        assertTrue(special.getId() > october.getId());

        Addon addon = addonRepository.save(new Addon("Breakfast override " + UUID.randomUUID()));
        AddonMapping mapping = addonMappingRepository.save(
                new AddonMapping(true, unit, addon, LocalDate.of(2026, 10, 1))
        );
        PeriodPriceAddon regularAddonPrice = periodPriceAddonRepository.save(new PeriodPriceAddon(
                mapping, 10, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)
        ));
        PeriodPriceAddon specialAddonPrice = periodPriceAddonRepository.save(new PeriodPriceAddon(
                mapping, 25, LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 10)
        ));
        assertTrue(specialAddonPrice.getId() > regularAddonPrice.getId());

        UUID unitPublicId = unit.getPublicId();
        Long addonId = addon.getId();
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/units/search")
                        .with(asGuest())
                        .param("city", city)
                        .param("country", "Serbia")
                        .param("adults", "2")
                        .param("kids", "0")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].unitId").value(unitPublicId.toString()))
                .andExpect(jsonPath("$.content[0].totalPriceForStay").value(270.0));

        mockMvc.perform(get("/api/units/{unitId}/addons", unitPublicId)
                        .with(asGuest())
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].addonID").value(addonId))
                .andExpect(jsonPath("$[0].price").value(60.0));

        mockMvc.perform(post("/api/units/{unitId}/book", unitPublicId)
                        .with(asGuest())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"start_date":"%s","end_date":"%s","addons":[{"id":%d}]}
                                """.formatted(start, end, addonId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(330.0));
    }
}
