import { DatePickerInput } from "@mantine/dates";
import { useEffect, useState } from "react";
import { useFetch } from "../hooks/useFetch";
import "./styles/Search.css";
import GuestDropdown from "../components/GuestDropdown";
import { createSearchParams, useNavigate } from "react-router-dom";

const Search = () => {
  const navigate = useNavigate();

  const [value, setValue] = useState([null, null]);

  const [city, setCity] = useState("");
  const [country, setCountry] = useState("");
  const [adults, setAdults] = useState(1);
  const [kids, setKids] = useState(0);

  const [maxPrice, setMaxPrice] = useState("");
  const [selectedPropFacs, setSelectedPropFacs] = useState([]);
  const [selectedUnitFacs, setSelectedUnitFacs] = useState([]);
  const [units, setUnits] = useState(null);

  const handleDateChange = (dates) => {
    setValue(dates);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const params = {
      city,
      country,
      startDate: value[0],
      endDate: value[1],
      adults,
      kids,
    };
    navigate({
      pathname: "/search-with-filter",
      search: `${createSearchParams(params)}`,
    });
  };

  return (
    <div className="search-container">
      <form className="search-form" onSubmit={handleSubmit}>
        <input
          type="text"
          name="city"
          id="city"
          placeholder="Search for city"
          value={city}
          onChange={(e) => setCity(e.target.value)}
        />

        <input
          type="text"
          name="country"
          id="country"
          placeholder="Search for country"
          value={country}
          onChange={(e) => setCountry(e.target.value)}
        />

        <DatePickerInput
          className="datepicker"
          valueFormat="YYYY MMMM DD"
          type="range"
          placeholder="Choose a date range"
          value={value}
          onChange={handleDateChange}
        />

        <GuestDropdown
          adults={adults}
          setAdults={setAdults}
          kids={kids}
          setKids={setKids}
        />

        <button>Search</button>
      </form>
    </div>
  );
};

export default Search;
