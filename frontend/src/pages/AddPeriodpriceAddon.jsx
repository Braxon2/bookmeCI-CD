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

const AddPeriodPriceAddon = () => {
  const apiURL = import.meta.env.VITE_API_URL || "";
  const { unitId, addonId } = useParams();
  const navigate = useNavigate();
  const [dates, setDates] = useState([null, null]);
  const [price, setPrice] = useState("");
  const [createdPrices, setCreatedPrices] = useState([]);
  const [billingOverride, setBillingOverride] = useState(null);
  const [isToggling, setIsToggling] = useState(false);
  const [formError, setFormError] = useState("");
  const [billingError, setBillingError] = useState("");
  const [success, setSuccess] = useState("");

  const { data: unit } = useFetch(
    unitId ? `${apiURL}/api/units/${unitId}/info` : null,
  );
  const {
    data: priceDates,
    loading: pricesLoading,
    error: pricesError,
  } = useFetch(
    unitId && addonId
      ? `${apiURL}/api/units/${unitId}/addons/${addonId}/period-prices`
      : null,
  );
  const { isLoading, error: postError, post } = usePost();

  const addon = unit?.addonList?.find((item) => String(item.id) === String(addonId));
  const detectedBillingType = Boolean(priceDates?.[0]?.isPerNight);
  const perNight = billingOverride ?? detectedBillingType;
  const prices = [...(priceDates || []), ...createdPrices].sort((a, b) =>
    String(a.startDate).localeCompare(String(b.startDate)),
  );
  const getPrice = (period) => Number(period.pricePerNight ?? period.price ?? 0);

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

  const handleBillingChange = async (nextValue) => {
    if (nextValue === perNight || isToggling) return;
    setIsToggling(true);
    setBillingError("");
    try {
      const response = await fetch(
        `${apiURL}/api/units/${unitId}/addons/${addonId}/billing-type`,
        {
          method: "PATCH",
          headers: {
            Authorization: `Bearer ${localStorage.getItem("jwtToken")}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ isPerNight: nextValue }),
        },
      );
      const result = await response.json().catch(() => null);
      if (!response.ok) {
        throw new Error(result?.message || "The billing type could not be updated.");
      }
      setBillingOverride(nextValue);
    } catch (requestError) {
      setBillingError(requestError.message);
    } finally {
      setIsToggling(false);
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setFormError("");
    setSuccess("");

    if (!dates[0] || !dates[1]) {
      setFormError("Choose both a start date and an end date.");
      return;
    }
    if (!price || Number(price) <= 0) {
      setFormError("Enter a price greater than zero.");
      return;
    }

    const result = await post(
      `${apiURL}/api/units/${unitId}/addons/${addonId}/add-price`,
      {
        price: Number(price),
        startDate: toApiDate(dates[0]),
        endDate: toApiDate(dates[1]),
      },
    );

    if (result) {
      setCreatedPrices((current) => [
        ...current,
        { ...result, pricePerNight: result.price, isPerNight: perNight },
      ]);
      setSuccess("Add-on pricing was added successfully.");
      setDates([null, null]);
      setPrice("");
    }
  };

  return (
    <main className="pricing-page is-addon-pricing">
      <div className="pricing-shell">
        <button className="pricing-back" type="button" onClick={() => navigate(-1)}>
          ← Back to add-ons
        </button>

        <header className="pricing-header">
          <div>
            <span>Owner workspace</span>
            <h1>Set add-on pricing</h1>
            <p>
              {addon?.name && unit?.name
                ? `Configure when ${addon.name} is available for ${unit.name} and how guests are charged.`
                : "Configure when this add-on is available and how guests are charged."}
            </p>
          </div>
          <div className="pricing-header-symbol" aria-hidden="true">+</div>
        </header>

        <div className="pricing-workspace">
          <form className="pricing-card pricing-form" onSubmit={handleSubmit}>
            <div className="pricing-card-heading">
              <div><span>New rate</span><h2>Add-on details</h2></div>
              <strong>{perNight ? "Per night" : "Flat rate"}</strong>
            </div>

            <div className="pricing-form-body">
              <fieldset className="pricing-billing-fieldset">
                <legend>Billing type</legend>
                <p>Choose whether guests pay once or for every night of their stay.</p>
                <div className="pricing-billing-options">
                  <button className={!perNight ? "is-selected" : ""} type="button" disabled={isToggling} onClick={() => handleBillingChange(false)}>
                    <span>Flat rate</span><small>Charged once per stay</small>
                  </button>
                  <button className={perNight ? "is-selected" : ""} type="button" disabled={isToggling} onClick={() => handleBillingChange(true)}>
                    <span>Per night</span><small>Multiplied by nights</small>
                  </button>
                </div>
                {isToggling && <small className="pricing-saving-label">Updating billing type...</small>}
                {billingError && <p className="pricing-inline-error" role="alert">{billingError}</p>}
              </fieldset>

              <DatePickerInput
                className="pricing-date-picker"
                label="Available dates"
                description="Select the first and last date this add-on price applies to."
                valueFormat="MMM D, YYYY"
                type="range"
                placeholder="Choose a date range"
                value={dates}
                minDate={new Date()}
                onChange={setDates}
                clearable
              />

              <label className="pricing-field">
                <span>{perNight ? "Price per night" : "Price per stay"}</span>
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
                <small>{perNight ? "This amount is charged for every booked night." : "This amount is charged once for the entire stay."}</small>
              </label>

              <div className="pricing-selection-summary">
                <span>Date range</span>
                <strong>
                  {dates[0] && dates[1]
                    ? `${formatDate(dates[0])} — ${formatDate(dates[1])}`
                    : "No dates selected"}
                </strong>
              </div>

              <button className="pricing-submit" type="submit" disabled={isLoading}>
                {isLoading ? "Saving price..." : "Add add-on price"}
              </button>
              {(formError || postError) && <p className="pricing-message is-error" role="alert">{formError || postError}</p>}
              {success && <p className="pricing-message is-success" role="status">✓ {success}</p>}
            </div>
          </form>

          <section className="pricing-card pricing-calendar-card">
            <div className="pricing-card-heading">
              <div><span>Calendar</span><h2>Price overview</h2></div>
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
                      {matchedPrice && <small>€{getPrice(matchedPrice).toFixed(0)}</small>}
                    </div>
                  );
                }}
              />
            </div>
            <div className="pricing-calendar-note"><span aria-hidden="true" /> Dates with an active add-on price</div>
          </section>
        </div>

        <section className="pricing-periods-card">
          <div className="pricing-periods-heading"><div><span>Configured prices</span><h2>Availability periods</h2></div></div>
          {pricesLoading && <div className="pricing-empty">Loading pricing periods...</div>}
          {!pricesLoading && pricesError && <div className="pricing-empty is-error">{pricesError}</div>}
          {!pricesLoading && !pricesError && prices.length === 0 && (
            <div className="pricing-empty"><strong>No prices configured yet</strong><p>Add the first add-on price using the form above.</p></div>
          )}
          {!pricesLoading && prices.length > 0 && (
            <div className="pricing-period-list">
              {prices.map((period, index) => (
                <article key={period.id ?? `${period.startDate}-${index}`}>
                  <div className="pricing-period-icon" aria-hidden="true">{String(index + 1).padStart(2, "0")}</div>
                  <div className="pricing-period-copy"><strong>{addon?.name || "Add-on price"}</strong><span>{formatDate(period.startDate)} — {formatDate(period.endDate)}</span></div>
                  <div className="pricing-period-amount"><strong>{currencyFormatter.format(getPrice(period))}</strong><span>{period.isPerNight ?? perNight ? "per night" : "per stay"}</span></div>
                </article>
              ))}
            </div>
          )}
        </section>
      </div>
    </main>
  );
};

export default AddPeriodPriceAddon;
