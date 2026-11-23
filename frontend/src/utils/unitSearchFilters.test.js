import test from "node:test";
import assert from "node:assert/strict";
import { filtersToSearchParams } from "./unitSearchFilters.js";

const filters = {
  city: "Belgrade", country: "Serbia", startDate: "2026-12-10", endDate: "2026-12-15",
  adults: 2, kids: 1, maxPrice: 300, propertyFacilities: [7, 9], unitFacilities: [12],
};

test("smart filters use the existing search parameters and reset paging", () => {
  const params = filtersToSearchParams(filters);
  assert.equal(params.get("startDate"), "2026-12-10");
  assert.equal(params.get("endDate"), "2026-12-15");
  assert.equal(params.get("adults"), "2");
  assert.equal(params.get("kids"), "1");
  assert.equal(params.get("maxPrice"), "300");
  assert.equal(params.get("propertyFacilities"), "7,9");
  assert.equal(params.get("unitFacilities"), "12");
  assert.equal(params.get("page"), "0");
  assert.equal(params.has("message"), false);
});

test("removed amenities and budget do not survive in a new search URL", () => {
  const params = filtersToSearchParams({ ...filters, maxPrice: null, propertyFacilities: [], unitFacilities: [] });
  assert.equal(params.has("maxPrice"), false);
  assert.equal(params.has("propertyFacilities"), false);
  assert.equal(params.has("unitFacilities"), false);
  assert.equal(params.get("city"), "Belgrade");
});

test("a zero total budget is retained rather than treated as missing", () => {
  assert.equal(filtersToSearchParams({ ...filters, maxPrice: 0 }).get("maxPrice"), "0");
});
