import { DatePickerInput } from "@mantine/dates";
import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import BookableUnitCard from "../components/BookableUnitCard";
import GuestDropdown from "../components/GuestDropdown";
import SmartUnitFilters from "../components/SmartUnitFilters";
import { useFetch } from "../hooks/useFetch";
import { filtersToSearchParams } from "../utils/unitSearchFilters";
import "./styles/SearchWithFilters.css";

const parseIds = (value) => value ? value.split(",").map(Number).filter(Boolean) : [];

const SearchWithfilter = () => {
  const apiURL = import.meta.env.VITE_API_URL || "";
  const [searchParams, setSearchParams] = useSearchParams();
  const [city, setCity] = useState(searchParams.get("city") || "");
  const [country, setCountry] = useState(searchParams.get("country") || "");
  const [adults, setAdults] = useState(Number(searchParams.get("adults")) || 1);
  const [kids, setKids] = useState(Number(searchParams.get("kids")) || 0);
  const [dates, setDates] = useState([
    searchParams.get("startDate") || null,
    searchParams.get("endDate") || null,
  ]);
  const [maxPrice, setMaxPrice] = useState(searchParams.get("maxPrice") || "");
  const [selectedPropFacs, setSelectedPropFacs] = useState(parseIds(searchParams.get("propertyFacilities")));
  const [selectedUnitFacs, setSelectedUnitFacs] = useState(parseIds(searchParams.get("unitFacilities")));
  const [units, setUnits] = useState([]);
  const [totalResults, setTotalResults] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isSearching, setIsSearching] = useState(true);
  const [searchError, setSearchError] = useState("");

  const { data: propFacilities } = useFetch(`${apiURL}/api/fascilities`);
  const { data: unitFacilities } = useFetch(`${apiURL}/api/unit-fascilities`);
  const queryString = searchParams.toString();
  const currentPage = Math.max(0, Number(searchParams.get("page")) || 0);

  useEffect(() => {
    const controller = new AbortController();
    const fetchUnits = async () => {
      const params = new URLSearchParams(queryString);
      if (!params.get("startDate") || !params.get("endDate")) {
        setUnits([]);
        setTotalResults(0);
        setTotalPages(0);
        setIsSearching(false);
        setSearchError("Choose check-in and check-out dates to see availability.");
        return;
      }

      setIsSearching(true);
      setSearchError("");
      try {
        const response = await fetch(`${apiURL}/api/units/search?${params.toString()}`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("jwtToken")}`,
            "Content-Type": "application/json",
          },
          signal: controller.signal,
        });
        if (!response.ok) throw new Error("We couldn't complete this search. Please try again.");
        const result = await response.json();
        setUnits(result.content || []);
        setTotalResults(result.totalElements ?? result.content?.length ?? 0);
        setTotalPages(result.totalPages ?? 1);
      } catch (error) {
        if (error.name !== "AbortError") {
          setUnits([]);
          setTotalResults(0);
          setTotalPages(0);
          setSearchError(error.message);
        }
      } finally {
        if (!controller.signal.aborted) setIsSearching(false);
      }
    };
    fetchUnits();
    return () => controller.abort();
  }, [apiURL, queryString]);

  const toggleId = (id, setter) => setter((current) =>
    current.includes(id) ? current.filter((item) => item !== id) : [...current, id],
  );

  const createParams = (includeFilters = true) => {
    const params = new URLSearchParams();
    if (city.trim()) params.set("city", city.trim());
    if (country.trim()) params.set("country", country.trim());
    params.set("adults", adults);
    params.set("kids", kids);
    if (dates[0]) params.set("startDate", dates[0]);
    if (dates[1]) params.set("endDate", dates[1]);
    if (includeFilters && maxPrice) params.set("maxPrice", maxPrice);
    if (includeFilters && selectedPropFacs.length) params.set("propertyFacilities", selectedPropFacs.join(","));
    if (includeFilters && selectedUnitFacs.length) params.set("unitFacilities", selectedUnitFacs.join(","));
    return params;
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    setSearchParams(createParams());
  };

  const clearFilters = () => {
    setMaxPrice("");
    setSelectedPropFacs([]);
    setSelectedUnitFacs([]);
    setSearchParams(createParams(false));
  };

  const applySmartFilters = (filters) => {
    setCity(filters.city);
    setCountry(filters.country);
    setDates([filters.startDate, filters.endDate]);
    setAdults(filters.adults);
    setKids(filters.kids);
    setMaxPrice(filters.maxPrice ?? "");
    setSelectedPropFacs(filters.propertyFacilities || []);
    setSelectedUnitFacs(filters.unitFacilities || []);
    setSearchParams(filtersToSearchParams(filters));
  };

  const changePage = (page) => {
    const params = new URLSearchParams(searchParams);
    params.set("page", page);
    setSearchParams(params);
  };

  const smartContext = {
    city: city.trim() || null,
    country: country.trim() || null,
    checkIn: dates[0] || null,
    checkOut: dates[1] || null,
    adults,
    children: kids,
    maxTotalPrice: maxPrice === "" ? null : Number(maxPrice),
    propertyFacilities: propFacilities?.filter((facility) => selectedPropFacs.includes(facility.id)).map((facility) => facility.name) || [],
    unitFacilities: unitFacilities?.filter((facility) => selectedUnitFacs.includes(facility.id)).map((facility) => facility.name) || [],
  };

  const filterCount = selectedPropFacs.length + selectedUnitFacs.length + (maxPrice ? 1 : 0);
  const destination = [searchParams.get("city"), searchParams.get("country")].filter(Boolean).join(", ");

  return (
    <main className="filtered-search-page">
      <div className="filtered-search-shell">
        <header className="filtered-search-heading">
          <div><span>Available stays</span><h1>{destination || "Search results"}</h1></div>
          <p>Refine your search to find the place that fits your trip.</p>
        </header>

        <form className="filtered-search-bar" onSubmit={handleSubmit}>
          <label><span>City</span><input value={city} onChange={(e) => setCity(e.target.value)} placeholder="City" /></label>
          <label><span>Country</span><input value={country} onChange={(e) => setCountry(e.target.value)} placeholder="Country" /></label>
          <label className="filtered-date-field"><span>Dates</span><DatePickerInput type="range" valueFormat="DD MMM YYYY"
            value={dates} onChange={setDates} placeholder="Choose dates" minDate={new Date()} /></label>
          <div className="filtered-guest-field"><span>Guests</span><GuestDropdown adults={adults} setAdults={setAdults} kids={kids} setKids={setKids} /></div>
          <button type="submit">Update search</button>
        </form>

        <div className="filtered-search-layout">
          <aside className="filter-panel">
            <div className="filter-panel-heading">
              <div><span>Refine results</span><h2>Filters</h2></div>
              {filterCount > 0 && <button type="button" onClick={clearFilters}>Clear {filterCount}</button>}
            </div>

            <SmartUnitFilters apiURL={apiURL} context={smartContext}
              catalogReady={Array.isArray(propFacilities) && Array.isArray(unitFacilities)} onApply={applySmartFilters} />

            <div className="filter-group">
              <label htmlFor="max-price">Maximum total price</label>
              <div className="filter-price-input"><span>€</span><input id="max-price" type="number" min="0" step="1"
                placeholder="Any price" value={maxPrice} onChange={(e) => setMaxPrice(e.target.value)} /></div>
            </div>

            <div className="filter-group">
              <h3>Property amenities</h3>
              <div className="filter-options">{propFacilities?.map((facility) => (
                <label key={`property-${facility.id}`} className="filter-option">
                  <input type="checkbox" checked={selectedPropFacs.includes(facility.id)}
                    onChange={() => toggleId(facility.id, setSelectedPropFacs)} />
                  <span className="filter-check" aria-hidden="true">✓</span><span>{facility.name}</span>
                </label>
              ))}</div>
            </div>

            <div className="filter-group">
              <h3>Inside the unit</h3>
              <div className="filter-options">{unitFacilities?.map((facility) => (
                <label key={`unit-${facility.id}`} className="filter-option">
                  <input type="checkbox" checked={selectedUnitFacs.includes(facility.id)}
                    onChange={() => toggleId(facility.id, setSelectedUnitFacs)} />
                  <span className="filter-check" aria-hidden="true">✓</span><span>{facility.name}</span>
                </label>
              ))}</div>
            </div>
            <button className="filter-apply-button" type="button" onClick={() => setSearchParams(createParams())}>Apply filters</button>
          </aside>

          <section className="filtered-results" aria-live="polite">
            <div className="filtered-results-heading">
              <div><span>{isSearching ? "Searching" : `${totalResults} ${totalResults === 1 ? "stay" : "stays"}`}</span>
                <h2>{isSearching ? "Finding the best matches..." : "Places available for your trip"}</h2></div>
              {filterCount > 0 && <span className="active-filter-badge">{filterCount} active</span>}
            </div>

            {isSearching && <div className="search-results-loading"><span /><p>Checking live availability...</p></div>}
            {!isSearching && searchError && <div className="search-results-state"><h3>Search needs an update</h3><p>{searchError}</p></div>}
            {!isSearching && !searchError && units.length === 0 && <div className="search-results-state">
              <h3>No exact matches yet</h3><p>Try raising your price limit or selecting fewer amenities.</p>
              {filterCount > 0 && <button type="button" onClick={clearFilters}>Clear filters</button>}
            </div>}
            {!isSearching && units.length > 0 && <div className="filtered-units-list">{units.map((unit) => (
              <BookableUnitCard key={unit.unitId} bookableUnit={unit}
                checkIn={searchParams.get("startDate")} checkOut={searchParams.get("endDate")} />
            ))}</div>}
            {!isSearching && !searchError && totalPages > 1 && <nav className="filtered-pagination" aria-label="Search result pages">
              <button type="button" disabled={currentPage === 0} onClick={() => changePage(currentPage - 1)}>Previous</button>
              <span>Page {currentPage + 1} of {totalPages}</span>
              <button type="button" disabled={currentPage >= totalPages - 1} onClick={() => changePage(currentPage + 1)}>Next</button>
            </nav>}
          </section>
        </div>
      </div>
    </main>
  );
};

export default SearchWithfilter;
