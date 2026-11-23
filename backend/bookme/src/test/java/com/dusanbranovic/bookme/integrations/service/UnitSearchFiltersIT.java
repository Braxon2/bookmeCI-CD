package com.dusanbranovic.bookme.integrations.service;

import com.dusanbranovic.bookme.ai.service.UnitSearchFilterService;
import com.dusanbranovic.bookme.ai.dto.request.UnitSearchCriteria;
import com.dusanbranovic.bookme.ai.dto.response.UnitSearchResponse;
import com.dusanbranovic.bookme.ai.service.UnitSearchAiClient;
import com.dusanbranovic.bookme.ai.tools.UnitSearchTools;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dusanbranovic.bookme.dto.responses.BookableUnitCardDTO;
import com.dusanbranovic.bookme.service.BookableUnitService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.dusanbranovic.bookme.models.*;
import com.dusanbranovic.bookme.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Actual PostgreSQL business logic; enabled Gemini is configured but never called. All data rolls back. */
@SpringBootTest(properties = {"spring.ai.model.chat=google-genai", "spring.ai.google.genai.api-key=test-only-no-network-key",
        "spring.ai.google.genai.chat.model=gemini-3.1-flash-lite"})
@ActiveProfiles("test")
@Transactional
class UnitSearchFiltersIT {
    @Autowired UnitSearchFilterService filters;
    @Autowired BookableUnitService search;
    @Autowired BookableUnitRepository units;
    @Autowired PropertyRepository properties;
    @Autowired PropertyTypeRepository types;
    @Autowired UserRepository users;
    @Autowired PeriodPriceRepository prices;
    @Autowired BookingRepository bookings;
    @Autowired FasiliityRepository facilities;
    @Autowired PropertyFascilityRepository propertyFacilities;
    @Autowired ChatModel model;
    @Autowired WebApplicationContext webContext;
    @MockitoBean UnitSearchAiClient aiClient;
    private final LocalDate start = LocalDate.of(2026, 12, 10), end = start.plusDays(5);
    private String city, facilityName;
    private BookableUnit unit;
    private User guest;

    @BeforeEach void seed() {
        String suffix = UUID.randomUUID().toString();
        city = "AI City " + suffix;
        facilityName = "Parking " + suffix;
        var owner = users.save(new User(UserType.OWNER, "owner-" + suffix + "@test.local", "Test", "Owner", "unused", ""));
        guest = users.save(new User(UserType.USER, "guest-" + suffix + "@test.local", "Test", "Guest", "unused", ""));
        var type = types.save(new PropertyType("AI hotel " + suffix));
        var property = properties.save(new Property(owner, type, "AI test property", "Public description", "Serbia", city, "Test Street", "Rules", "Info"));
        var facility = facilities.save(new Fascillity(facilityName));
        propertyFacilities.save(new PropertyFacility(property, facility));
        unit = units.save(new BookableUnit(property, 4, 40, 1, 2, 1, 3, 2, "AI suite"));
        var base = prices.save(new PeriodPrice(unit, 40, start.minusDays(1), end.plusDays(1), "Base"));
        var seasonal = prices.save(new PeriodPrice(unit, 90, start.plusDays(2), start.plusDays(3), "Seasonal override"));
        unit.setPeriodPriceList(List.of(base, seasonal));
        units.flush();
    }

    private Page<BookableUnitCardDTO> search(double maximum, int adults, int children, int page) {
        var criteria = filters.resolve(new UnitSearchCriteria(city, "Serbia", start, end, adults, children, maximum, List.of(facilityName), null));
        return search.searchUnits(criteria.city(), criteria.country(), criteria.adults(), criteria.kids(),
                criteria.startDate(), criteria.endDate(), criteria.maxPrice(), criteria.propertyFacilities(), criteria.unitFacilities(), PageRequest.of(page, 1));
    }

    @Test void enabledGoogleStarterIsWiredWithoutAnApiRequest() {
        assertInstanceOf(GoogleGenAiChatModel.class, model);
        assertEquals("gemini-3.1-flash-lite", model.getOptions().getModel());
    }

    @Test void searchUsesSeasonalTotalPriceRatherThanNightlyPriceAndFiltersFacilitiesAndCapacity() {
        // Three nights at 40 + two nights at 90 = 300. Check-out is not charged.
        assertTrue(search(299, 2, 0, 0).isEmpty());
        var found = search(300, 2, 1, 0).getContent();
        assertEquals(1, found.size());
        assertEquals(unit.getPublicId(), found.getFirst().unitId());
        assertEquals(300.0, found.getFirst().totalPriceForStay());
        assertTrue(search(1000, 4, 0, 0).isEmpty());
        assertTrue(search(1000, 2, 3, 0).isEmpty());
    }

    @Test void overlapsCancellationAndAdjacentCheckoutUseExistingInventoryRules() {
        assertEquals(1, search(1000, 2, 0, 0).getTotalElements());
        Booking overlap = bookings.saveAndFlush(new Booking(unit, guest, 300.0, start.minusDays(1), start.atStartOfDay(), end.atStartOfDay(), BookingStatus.CONFIRMED));
        assertTrue(search(1000, 2, 0, 0).isEmpty());
        assertEquals(1, search.searchUnits(city, "Serbia", 2, 0, end, end.plusDays(1), null, List.of(), List.of(), PageRequest.of(0, 20)).getTotalElements());
        overlap.setStatus(BookingStatus.CANCELLED);
        bookings.saveAndFlush(overlap);
        assertEquals(1, search(300, 2, 0, 0).getTotalElements());
    }

    @Test void allMatchesRemainAccessibleThroughNormalPagination() {
        var other = units.save(new BookableUnit(unit.getProperty(), 4, 40, 1, 2, 1, 3, 2, "Second suite"));
        var rate = prices.save(new PeriodPrice(other, 40, start, end, "Base"));
        other.setPeriodPriceList(List.of(rate));
        units.flush();
        var first = search(1000, 2, 0, 0);
        var second = search(1000, 2, 0, 1);
        assertEquals(2, first.getTotalElements());
        assertEquals(2, first.getTotalPages());
        assertEquals(1, second.getContent().size());
        assertNotEquals(first.getContent().getFirst().unitId(), second.getContent().getFirst().unitId());
    }

    @Test void statelessHttpFiltersFeedRealUnitResultsWithoutAnotherAiCall() throws Exception {
        when(aiClient.interpret(any(), any(), any())).thenAnswer(invocation -> {
            UnitSearchTools tool = invocation.getArgument(2);
            return new UnitSearchResponse(tool.applyUnitSearchFilters(new UnitSearchCriteria(
                    city, "Serbia", start, end, 2, 0, 300.0, List.of(facilityName), null)), null);
        });
        var mvc = MockMvcBuilders.webAppContextSetup(webContext)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity()).build();
        var response = mvc.perform(post("/api/ai/unit-search").with(user(guest))
                        .contentType("application/json").content("{\"message\":\"Sa parkingom, do 300 ukupno\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.filters.city").value(city))
                .andExpect(jsonPath("$.conversationId").doesNotExist()).andReturn().getResponse();
        var resolved = new ObjectMapper().readTree(response.getContentAsString()).get("filters");
        String facilityId = resolved.get("propertyFacilities").get(0).asText();
        mvc.perform(get("/api/units/search").with(user(guest)).param("city", resolved.get("city").asText())
                        .param("country", "Serbia").param("startDate", start.toString()).param("endDate", end.toString())
                        .param("adults", "2").param("kids", "0").param("maxPrice", "300").param("propertyFacilities", facilityId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].unitId").value(unit.getPublicId().toString()))
                .andExpect(jsonPath("$.content[0].totalPriceForStay").value(300.0));
        verify(aiClient).interpret(any(), any(), any());
    }
}
