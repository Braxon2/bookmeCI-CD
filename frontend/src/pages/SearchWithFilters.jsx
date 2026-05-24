import { DatePickerInput } from "@mantine/dates";
import { useEffect, useState } from "react";
import { useFetch } from "../hooks/useFetch";
import BookableUnitCard from "../components/BookableUnitCard";
import GuestDropdown from "../components/GuestDropdown";
import { useSearchParams } from "react-router-dom";
import "./styles/SearchWithFilters.css";

const SearchWithfilter = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const [city, setCity] = useState(searchParams.get("city") || "");
  const [country, setCountry] = useState(searchParams.get("country") || "");
  const [adults, setAdults] = useState(
    parseInt(searchParams.get("adults")) || 1,
  );
  const [kids, setKids] = useState(parseInt(searchParams.get("kids")) || 0);

  const initialStartDate = searchParams.get("startDate") || null;
  const initialEndDate = searchParams.get("endDate") || null;
  const [value, setValue] = useState([initialStartDate, initialEndDate]);

  const [maxPrice, setMaxPrice] = useState("");
  const [selectedPropFacs, setSelectedPropFacs] = useState([]);
  const [selectedUnitFacs, setSelectedUnitFacs] = useState([]);
  const [units, setUnits] = useState([]);

  const { data: propFacilities } = useFetch(
    "http://localhost:8080/api/fascilities",
  );
  const { data: unitFacilities } = useFetch(
    "http://localhost:8080/api/unit-fascilities",
  );

  useEffect(() => {
    fetchUnits();
  }, [searchParams]);

  const handlePropFacToggle = (id) => {
    setSelectedPropFacs((prev) =>
      prev.includes(id) ? prev.filter((facId) => facId !== id) : [...prev, id],
    );
  };

  const handleUnitFacToggle = (id) => {
    setSelectedUnitFacs((prev) =>
      prev.includes(id) ? prev.filter((facId) => facId !== id) : [...prev, id],
    );
  };

  const handleDateChange = (dates) => {
    setValue(dates);
  };

  const fetchUnits = async () => {
    const qCity = searchParams.get("city") || "";
    const qCountry = searchParams.get("country") || "";
    const qAdults = searchParams.get("adults") || 1;
    const qKids = searchParams.get("kids") || 0;
    const qStart = searchParams.get("startDate") || "";
    const qEnd = searchParams.get("endDate") || "";
    const qMaxPrice = searchParams.get("maxPrice") || "";
    const qPropFacs = searchParams.get("propertyFacilities") || "";
    const qUnitFacs = searchParams.get("unitFacilities") || "";

    let url = `http://localhost:8080/api/units/search?city=${qCity}&country=${qCountry}&adults=${qAdults}&kids=${qKids}`;

    if (qStart) url += `&startDate=${qStart}`;
    if (qEnd) url += `&endDate=${qEnd}`;
    if (qMaxPrice) url += `&maxPrice=${qMaxPrice}`;
    if (qPropFacs) url += `&propertyFacilities=${qPropFacs}`;
    if (qUnitFacs) url += `&unitFacilities=${qUnitFacs}`;

    try {
      const token = localStorage.getItem("jwtToken");
      const response = await fetch(url, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      const result = await response.json();
      setUnits(result);
    } catch (err) {
      console.error(err);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const params = new URLSearchParams();
    if (city) params.set("city", city);
    if (country) params.set("country", country);
    params.set("adults", adults);
    params.set("kids", kids);

    if (value[0]) params.set("startDate", value[0]);
    if (value[1]) params.set("endDate", value[1]);

    if (maxPrice) params.set("maxPrice", maxPrice);
    if (selectedPropFacs.length > 0)
      params.set("propertyFacilities", selectedPropFacs.join(","));
    if (selectedUnitFacs.length > 0)
      params.set("unitFacilities", selectedUnitFacs.join(","));

    setSearchParams(params);
  };

  return (
    <div className="page-wrapper">
      <div className="layout-container">
        <aside className="sidebar">
          <form onSubmit={handleSubmit} className="search-form">
            <div className="search-box">
              <h3>Search</h3>

              <div className="input-group">
                <label>Destination / City</label>
                <input
                  type="text"
                  placeholder="Where are you going?"
                  value={city}
                  onChange={(e) => setCity(e.target.value)}
                />
              </div>

              <div className="input-group">
                <label>Country</label>
                <input
                  type="text"
                  placeholder="Country"
                  value={country}
                  onChange={(e) => setCountry(e.target.value)}
                />
              </div>

              <div className="input-group">
                <label>Check-in / Check-out</label>
                <DatePickerInput
                  valueFormat="YYYY MMMM DD"
                  type="range"
                  placeholder="Choose dates"
                  value={value}
                  onChange={handleDateChange}
                  className="date-picker-custom"
                />
              </div>

              <div className="input-group">
                <label>Guests</label>
                <GuestDropdown
                  adults={adults}
                  setAdults={setAdults}
                  kids={kids}
                  setKids={setKids}
                />
              </div>

              <div className="input-group">
                <label>Max Price</label>
                <input
                  type="number"
                  placeholder="Set max budget"
                  value={maxPrice}
                  onChange={(e) => setMaxPrice(e.target.value)}
                  min="0"
                  step="0.01"
                />
              </div>

              <button type="submit" className="search-btn">
                Search
              </button>
            </div>

            <div className="filter-box">
              <h3>Filter by:</h3>
              <div className="filter-section">
                <p className="filter-title">Property Facilities</p>
                <div className="checkbox-list">
                  {propFacilities?.map((fac) => (
                    <label key={`prop-${fac.id}`} className="checkbox-label">
                      <input
                        type="checkbox"
                        checked={selectedPropFacs.includes(fac.id)}
                        onChange={() => handlePropFacToggle(fac.id)}
                      />
                      <span className="checkmark"></span>
                      {fac.name}
                    </label>
                  ))}
                </div>
              </div>

              <div className="filter-section">
                <p className="filter-title">Unit Facilities</p>
                <div className="checkbox-list">
                  {unitFacilities?.map((fac) => (
                    <label key={`unit-${fac.id}`} className="checkbox-label">
                      <input
                        type="checkbox"
                        checked={selectedUnitFacs.includes(fac.id)}
                        onChange={() => handleUnitFacToggle(fac.id)}
                      />
                      <span className="checkmark"></span>
                      {fac.name}
                    </label>
                  ))}
                </div>
              </div>
            </div>
          </form>
        </aside>

        <main className="results-content">
          <div className="results-header">
            <h2>
              {units
                ? `${units.length} properties found`
                : "Searching properties..."}
            </h2>
          </div>

          <div className="units-list">
            {units?.map((unit) => (
              <BookableUnitCard
                key={unit.unitId}
                bookableUnit={unit}
                checkIn={value[0]}
                checkOut={value[1]}
              />
            ))}
          </div>
        </main>
      </div>
    </div>
  );
};

export default SearchWithfilter;
