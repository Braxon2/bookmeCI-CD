package com.dusanbranovic.bookme.ai.tools;

import com.dusanbranovic.bookme.ai.dto.request.UnitSearchCriteria;
import com.dusanbranovic.bookme.ai.dto.response.UnitSearchFilters;
import com.dusanbranovic.bookme.ai.exception.UnitSearchException;
import com.dusanbranovic.bookme.ai.service.UnitSearchFilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class UnitSearchTools {
    private static final Logger log = LoggerFactory.getLogger(UnitSearchTools.class);
    private final UnitSearchFilterService filterService;
    private UnitSearchFilters filters;

    public UnitSearchTools(UnitSearchFilterService filterService) { this.filterService = filterService; }

    @Tool(description = "Apply accommodation search filters only when all requested requirements can be represented. Requires city, country and valid explicit dates. Use exact facility NAMES from the supplied catalog, never invent IDs. All facilities are AND filters. Does not search inventory or claim availability; the existing BookMe search runs afterward.", returnDirect = true)
    public UnitSearchFilters applyUnitSearchFilters(@ToolParam(description = "Complete criteria combining current form values with the user's explicit changes") UnitSearchCriteria criteria) {
        if (filters != null) throw UnitSearchException.invalid("Only one filter selection is allowed per request.");
        long start = System.nanoTime();
        try {
            filters = filterService.resolve(criteria);
            return filters;
        } finally {
            log.info("AI tool=applyUnitSearchFilters durationMs={} outcome={}",
                    (System.nanoTime() - start) / 1_000_000, filters == null ? "rejected" : "applied");
        }
    }

    public UnitSearchFilters filters() { return filters; }
}
