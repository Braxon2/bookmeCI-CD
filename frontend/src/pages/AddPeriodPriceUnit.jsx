import { Calendar, DatePickerInput } from "@mantine/dates";
import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import dayjs from "dayjs";
import usePost from "../hooks/usePost";
import { useFetch } from "../hooks/useFetch";
import "./styles/AddPeriodPriceUnit.css";

const currencyFormatter = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "EUR",
  minimumFractionDigits: 2,
});

const formatDate = (date) => dayjs(date).format("MMM D, YYYY");
const toApiDate = (date) => (date ? dayjs(date).format("YYYY-MM-DD") : null);

const AddPeriodPriceUnit = () => {
  const apiURL = import.meta.env.VITE_API_URL || "";
  const { unitId } = useParams();
  const navigate = useNavigate();
  const [dates, setDates] = useState([null, null]);
  const [price, setPrice] = useState("");
  const [season, setSeason] = useState("");
  const [createdPrices, setCreatedPrices] = useState([]);
  const [formError, setFormError] = useState("");
  const [success, setSuccess] = useState("");

  const { data: unit } = useFetch(
    unitId ? `${apiURL}/api/units/${unitId}/info` : null,
  );
  const {
    data: priceDates,
    loading: pricesLoading,
    error: pricesError,
  } = useFetch(
    unitId ? `${apiURL}/api/units/${unitId}/period-prices` : null,
  );
  const { isLoading, error: postError, post } = usePost();

  const prices = [...(priceDates || []), ...createdPrices].sort((a, b) =>
    String(a.startDate).localeCompare(String(b.startDate)),
  );

  const priceForDay = (date) => {
    const currentDate = dayjs(date);
    return [...prices].reverse().find((period) => {
      const startDate = dayjs(period.startDate);
      const endDate = dayjs(period.endDate);
      return (
        (currentDate.isAfter(startDate, "day") || currentDate.isSame(startDate, "day")) &&
        (currentDate.isBefore(endDate, "day") || currentDate.isSame(endDate, "day"))
      );
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setFormError("");
    setSuccess("");

    if (!dates[0] || !dates[1]) {
      setFormError("Choose both a start date and an end date.");
      return;
    }
    if (!season.trim()) {
      setFormError("Give this pricing period a season or rate name.");
      return;
    }
    if (!price || Number(price) <= 0) {
      setFormError("Enter a price greater than zero.");
      return;
    }

    const result = await post(`${apiURL}/api/units/${unitId}/add-price`, {
      pricePerNight: Number(price),
      startDate: toApiDate(dates[0]),
      endDate: toApiDate(dates[1]),
      season: season.trim(),
    });

    if (result) {
      setCreatedPrices((current) => [...current, result]);
      setSuccess(`${season.trim()} pricing was added successfully.`);
      setDates([null, null]);
      setPrice("");
      setSeason("");
    }
  };

  return (
    <main className="pricing-page">
      <div className="pricing-shell">
        <button className="pricing-back" type="button" onClick={() => navigate(-1)}>
          ← Back to units
        </button>

        <header className="pricing-header">
          <div>
            <span>Owner workspace</span>
            <h1>Set unit pricing</h1>
            <p>
              {unit?.name
                ? `Create seasonal nightly rates for ${unit.name}.`
                : "Create seasonal nightly rates for this bookable unit."}
            </p>
          </div>
          <div className="pricing-header-symbol" aria-hidden="true">€</div>
        </header>

        <div className="pricing-workspace">
          <form className="pricing-card pricing-form" onSubmit={handleSubmit}>
            <div className="pricing-card-heading">
              <div><span>New rate</span><h2>Pricing details</h2></div>
              <strong>Per night</strong>
            </div>

            <div className="pricing-form-body">
              <DatePickerInput
                className="pricing-date-picker"
                label="Stay dates"
                description="Select the first and last date this rate applies to."
                valueFormat="MMM D, YYYY"
                type="range"
                placeholder="Choose a date range"
                value={dates}
                minDate={new Date()}
                onChange={setDates}
                clearable
              />

              <div className="pricing-input-grid">
                <label className="pricing-field">
                  <span>Season or rate name</span>
                  <input
                    type="text"
                    value={season}
                    onChange={(event) => setSeason(event.target.value)}
                    placeholder="e.g. Summer season"
                  />
                  <small>Helps you identify this rate later.</small>
                </label>
                <label className="pricing-field">
                  <span>Nightly price</span>
                  <div className="pricing-money-input">
                    <i>€</i>
                    <input
                      type="number"
                      min="0.01"
                      step="0.01"
                      value={price}
                      onChange={(event) => setPrice(event.target.value)}
                      placeholder="0.00"
                    />
                  </div>
                  <small>Amount charged for one night.</small>
                </label>
              </div>

              <div className="pricing-selection-summary">
                <span>Date range</span>
                <strong>
                  {dates[0] && dates[1]
                    ? `${formatDate(dates[0])} — ${formatDate(dates[1])}`
                    : "No dates selected"}
                </strong>
              </div>

              <button className="pricing-submit" type="submit" disabled={isLoading}>
                {isLoading ? "Saving rate..." : "Add nightly rate"}
              </button>
              {(formError || postError) && <p className="pricing-message is-error" role="alert">{formError || postError}</p>}
              {success && <p className="pricing-message is-success" role="status">✓ {success}</p>}
            </div>
          </form>

          <section className="pricing-card pricing-calendar-card">
            <div className="pricing-card-heading">
              <div><span>Calendar</span><h2>Rate overview</h2></div>
              <strong>{prices.length} {prices.length === 1 ? "period" : "periods"}</strong>
            </div>
            <div className="pricing-calendar-wrap">
              <Calendar
                static
                renderDay={(date) => {
                  const matchedPrice = priceForDay(date);
                  return (
                    <div className={`pricing-calendar-day ${matchedPrice ? "is-priced" : ""}`}>
                      <span>{dayjs(date).date()}</span>
                      {matchedPrice && <small>€{Number(matchedPrice.pricePerNight).toFixed(0)}</small>}
                    </div>
                  );
                }}
              />
            </div>
            <div className="pricing-calendar-note"><span aria-hidden="true" /> Dates with an active nightly rate</div>
          </section>
        </div>

        <section className="pricing-periods-card">
          <div className="pricing-periods-heading"><div><span>Configured rates</span><h2>Pricing periods</h2></div></div>
          {pricesLoading && <div className="pricing-empty">Loading pricing periods...</div>}
          {!pricesLoading && pricesError && <div className="pricing-empty is-error">{pricesError}</div>}
          {!pricesLoading && !pricesError && prices.length === 0 && (
            <div className="pricing-empty"><strong>No rates configured yet</strong><p>Add the first nightly rate using the form above.</p></div>
          )}
          {!pricesLoading && prices.length > 0 && (
            <div className="pricing-period-list">
              {prices.map((period, index) => (
                <article key={period.id ?? `${period.startDate}-${index}`}>
                  <div className="pricing-period-icon" aria-hidden="true">{String(index + 1).padStart(2, "0")}</div>
                  <div className="pricing-period-copy"><strong>{period.season || "Standard rate"}</strong><span>{formatDate(period.startDate)} — {formatDate(period.endDate)}</span></div>
                  <div className="pricing-period-amount"><strong>{currencyFormatter.format(period.pricePerNight)}</strong><span>per night</span></div>
                </article>
              ))}
            </div>
          )}
        </section>
      </div>
    </main>
  );
};

export default AddPeriodPriceUnit;
