import { DatePickerInput } from "@mantine/dates";
import { useState } from "react";
import { createSearchParams, useNavigate } from "react-router-dom";
import GuestDropdown from "../components/GuestDropdown";
import heroImage from "../assets/images/prop1.jpeg";
import "./styles/Search.css";

const Search = () => {
  const navigate = useNavigate();
  const [dates, setDates] = useState([null, null]);
  const [city, setCity] = useState("");
  const [country, setCountry] = useState("");
  const [adults, setAdults] = useState(1);
  const [kids, setKids] = useState(0);
  const [validationError, setValidationError] = useState("");

  const handleSubmit = (event) => {
    event.preventDefault();
    if (!city.trim() || !country.trim() || !dates[0] || !dates[1]) {
      setValidationError("Add a destination and travel dates to start searching.");
      return;
    }

    setValidationError("");
    navigate({
      pathname: "/search-with-filter",
      search: createSearchParams({
        city: city.trim(),
        country: country.trim(),
        startDate: dates[0],
        endDate: dates[1],
        adults,
        kids,
      }).toString(),
    });
  };

  return (
    <main className="simple-search-page">
      <section className="simple-search-shell">
        <div className="simple-search-copy">
          <span className="simple-search-eyebrow">Find your next stay</span>
          <h1>Good trips start with the right place.</h1>
          <p>
            Search homes, apartments, and hotels with the space and amenities
            that fit your plans.
          </p>

          <form className="simple-search-form" onSubmit={handleSubmit}>
            <div className="simple-search-fields">
              <label className="simple-search-field">
                <span>City</span>
                <input
                  type="text"
                  name="city"
                  placeholder="e.g. Belgrade"
                  value={city}
                  onChange={(event) => setCity(event.target.value)}
                />
              </label>

              <label className="simple-search-field">
                <span>Country</span>
                <input
                  type="text"
                  name="country"
                  placeholder="e.g. Serbia"
                  value={country}
                  onChange={(event) => setCountry(event.target.value)}
                />
              </label>

              <label className="simple-search-field simple-search-dates">
                <span>Check-in — Check-out</span>
                <DatePickerInput
                  className="simple-search-datepicker"
                  valueFormat="DD MMM YYYY"
                  type="range"
                  placeholder="Choose your dates"
                  value={dates}
                  onChange={setDates}
                  minDate={new Date()}
                />
              </label>

              <div className="simple-search-field simple-search-guests">
                <span>Guests</span>
                <GuestDropdown
                  adults={adults}
                  setAdults={setAdults}
                  kids={kids}
                  setKids={setKids}
                />
              </div>
            </div>

            {validationError && (
              <p className="simple-search-error" role="alert">
                {validationError}
              </p>
            )}

            <button className="simple-search-button" type="submit">
              Search available stays <span aria-hidden="true">→</span>
            </button>
          </form>

          <div className="simple-search-trust">
            <span><strong>Flexible</strong> search options</span>
            <span><strong>Clear</strong> total pricing</span>
            <span><strong>Secure</strong> booking</span>
          </div>
        </div>

        <figure className="simple-search-visual">
          <img src={heroImage} alt="Welcoming holiday accommodation" />
          <figcaption>
            <span>Ready when you are</span>
            <strong>Discover stays made for real trips.</strong>
          </figcaption>
        </figure>
      </section>
    </main>
  );
};

export default Search;
