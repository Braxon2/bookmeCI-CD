export const filtersToSearchParams = (filters) => {
  const params = new URLSearchParams();
  params.set("city", filters.city);
  params.set("country", filters.country);
  params.set("startDate", filters.startDate);
  params.set("endDate", filters.endDate);
  params.set("adults", filters.adults);
  params.set("kids", filters.kids);
  if (filters.maxPrice != null) params.set("maxPrice", filters.maxPrice);
  if (filters.propertyFacilities?.length) params.set("propertyFacilities", filters.propertyFacilities.join(","));
  if (filters.unitFacilities?.length) params.set("unitFacilities", filters.unitFacilities.join(","));
  params.set("page", "0");
  return params;
};
