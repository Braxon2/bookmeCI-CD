import { useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import imageFallback from "../assets/images/Image-not-found.png";
import { useFetch } from "../hooks/useFetch";
import "./styles/DetailedBookableUnit.css";

const priceFormat = new Intl.NumberFormat("en-GB", {
  style: "currency",
  currency: "EUR",
  minimumFractionDigits: 2,
});
const dateFormat = new Intl.DateTimeFormat("en-GB", {
  day: "numeric",
  month: "short",
  year: "numeric",
});
const formatPrice = (price) => priceFormat.format(Number(price) || 0);
const formatDate = (date) =>
  date ? dateFormat.format(new Date(`${date}T00:00:00`)) : "Not selected";

const DetailIcon = ({ type }) => {
  const icons = {
    guests: <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8ZM22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" />,
    area: <path d="M3 9V3h6M21 9V3h-6M3 15v6h6M21 15v6h-6M8 12h8" />,
    bed: <path d="M3 7v11M21 18V11a2 2 0 0 0-2-2H8a2 2 0 0 0-2 2v7M3 14h18M6 9V6h5a2 2 0 0 1 2 2v1" />,
  };
  return (
    <svg aria-hidden="true" className="unit-detail-icon" fill="none" viewBox="0 0 24 24"
      stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      {icons[type]}
    </svg>
  );
};

const DetailedBookableUnit = () => {
  const apiURL = import.meta.env.VITE_API_URL || "";
  const { unitId } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const startDate = searchParams.get("startDate");
  const endDate = searchParams.get("endDate");

  const [isBooking, setIsBooking] = useState(false);
  const [bookingError, setBookingError] = useState(null);
  const [chosenAddons, setChosenAddons] = useState([]);
  const [activeImageIndex, setActiveImageIndex] = useState(null);

  const { data: unit, loading, error: unitError } = useFetch(
    unitId && startDate && endDate
      ? `${apiURL}/api/units/${unitId}?startDate=${startDate}&endDate=${endDate}`
      : null,
  );
  const { data: addons } = useFetch(
    unitId && startDate && endDate
      ? `${apiURL}/api/units/${unitId}/addons?startDate=${startDate}&endDate=${endDate}`
      : null,
  );

  const nights = (() => {
    if (!startDate || !endDate) return 0;
    const difference = new Date(`${endDate}T00:00:00`) - new Date(`${startDate}T00:00:00`);
    return Math.max(1, Math.round(difference / 86400000));
  })();

  const addonsTotal = chosenAddons.reduce((sum, id) => {
    const addon = addons?.find((item) => item.addonID === id);
    return addon ? sum + addon.price * (addon.perNight ? nights : 1) : sum;
  }, 0);
  const stayPrice = Number(unit?.totalPriceForStay) || 0;
  const totalPrice = stayPrice + addonsTotal;
  const nightlyPrice = nights ? stayPrice / nights : 0;

  const toggleAddon = (id) => {
    setChosenAddons((current) =>
      current.includes(id) ? current.filter((item) => item !== id) : [...current, id],
    );
  };

  const handleBooking = async () => {
    setIsBooking(true);
    setBookingError(null);
    try {
      const response = await fetch(`${apiURL}/api/units/${unitId}/book`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("jwtToken")}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          start_date: startDate,
          end_date: endDate,
          addons: chosenAddons.map((id) => ({ id })),
        }),
      });
      const json = await response.json();
      if (!response.ok) throw new Error(json.message || `Booking failed (${response.status})`);
      setChosenAddons([]);
      navigate("/profile");
    } catch (error) {
      setBookingError(error.message);
    } finally {
      setIsBooking(false);
    }
  };

  if (!startDate || !endDate) {
    return (
      <main className="unit-detail-page"><div className="unit-state-card">
        <h1>Select dates to view this unit</h1>
        <p>A check-in and check-out date are required to calculate availability and price.</p>
        <button type="button" onClick={() => navigate("/search-with-filter")}>Return to search</button>
      </div></main>
    );
  }
  if (loading) {
    return (
      <main className="unit-detail-page"><div className="unit-loading" role="status">
        <span className="unit-loading-spinner" /><p>Preparing your stay...</p>
      </div></main>
    );
  }
  if (unitError || !unit) {
    return (
      <main className="unit-detail-page"><div className="unit-state-card">
        <h1>We couldn&apos;t load this unit</h1><p>{unitError || "The unit may no longer be available."}</p>
        <button type="button" onClick={() => navigate(-1)}>Go back</button>
      </div></main>
    );
  }

  const {
    propertyDTO: property, name: unitName, maxCapacity, maxAdultCapacity,
    maxKidsCapacity, squareMeters, singleBeds, doubleBeds,
    unitFascilityDTO: unitFacilities = [],
  } = unit;
  const sortedImages = [...(unit.images || [])]
    .filter((image) => image?.url)
    .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0));
  const galleryImages = sortedImages.length ? sortedImages : [{ id: "fallback", url: imageFallback }];
  const visibleImages = galleryImages.slice(0, 5);
  const propertyFacilities = property?.fascilitiesDTO || [];
  const location = [property?.address, property?.city, property?.country].filter(Boolean).join(", ");

  const moveLightbox = (direction) => setActiveImageIndex((current) =>
    current === null ? null : (current + direction + galleryImages.length) % galleryImages.length,
  );
  const handleLightboxKeyDown = (event) => {
    if (event.key === "Escape") setActiveImageIndex(null);
    if (event.key === "ArrowLeft") moveLightbox(-1);
    if (event.key === "ArrowRight") moveLightbox(1);
  };

  return (
    <main className="unit-detail-page">
      <div className="unit-detail-shell">
        <button className="unit-back-link" type="button" onClick={() => navigate(-1)}>
          <span aria-hidden="true">←</span> Back to results
        </button>

        <header className="unit-heading">
          <div>
            <div className="unit-eyebrow-row">
              <span className="unit-type-badge">{property?.propertyTypeDTO?.name || "Accommodation"}</span>
              <span className="unit-property-name">{property?.name}</span>
            </div>
            <h1>{unitName}</h1>
            <p className="unit-location"><span aria-hidden="true">●</span>{location}</p>
          </div>
          <div className="unit-heading-price">
            <span>from</span><strong>{formatPrice(nightlyPrice)}</strong><span>per night</span>
          </div>
        </header>

        <section className={`unit-gallery unit-gallery-count-${visibleImages.length}`} aria-label="Unit photos">
          {visibleImages.map((image, index) => (
            <button className="unit-gallery-item" key={image.id ?? image.url} type="button"
              onClick={() => setActiveImageIndex(index)}
              aria-label={`Open photo ${index + 1} of ${galleryImages.length}`}>
              <img src={image.url} alt={`${unitName} — photo ${index + 1}`}
                onError={(event) => { event.currentTarget.src = imageFallback; }} />
              {index === visibleImages.length - 1 && galleryImages.length > 5 && (
                <span className="unit-gallery-more">+{galleryImages.length - 5} more photos</span>
              )}
            </button>
          ))}
        </section>

        <div className="unit-content-layout">
          <div className="unit-content-main">
            <section className="unit-section">
              <div className="unit-section-heading"><span>At a glance</span><h2>Comfortable space for your stay</h2></div>
              <div className="unit-stat-grid">
                <div className="unit-stat-card"><DetailIcon type="guests" /><div>
                  <strong>{maxCapacity} guests</strong>
                  <span>Up to {maxAdultCapacity} adults{maxKidsCapacity > 0 ? ` and ${maxKidsCapacity} children` : ""}</span>
                </div></div>
                <div className="unit-stat-card"><DetailIcon type="area" /><div>
                  <strong>{squareMeters} m²</strong><span>Entire unit size</span>
                </div></div>
                <div className="unit-stat-card"><DetailIcon type="bed" /><div>
                  <strong>{singleBeds + doubleBeds} beds</strong>
                  <span>{doubleBeds > 0 ? `${doubleBeds} double` : ""}{doubleBeds > 0 && singleBeds > 0 ? " · " : ""}{singleBeds > 0 ? `${singleBeds} single` : ""}</span>
                </div></div>
              </div>
            </section>

            <section className="unit-section">
              <div className="unit-section-heading"><span>About</span><h2>{property?.name}</h2></div>
              <p className="unit-description">{property?.description || "No property description is available yet."}</p>
            </section>

            {(unitFacilities.length > 0 || propertyFacilities.length > 0) && (
              <section className="unit-section">
                <div className="unit-section-heading"><span>Amenities</span><h2>What this place offers</h2></div>
                <div className="unit-facilities-columns">
                  {unitFacilities.length > 0 && <div><h3>Inside your unit</h3><ul className="unit-facility-list">
                    {unitFacilities.map((facility) => <li key={`unit-${facility.id}`}><span aria-hidden="true">✓</span>{facility.name}</li>)}
                  </ul></div>}
                  {propertyFacilities.length > 0 && <div><h3>At the property</h3><ul className="unit-facility-list">
                    {propertyFacilities.map((facility) => <li key={`property-${facility.id}`}><span aria-hidden="true">✓</span>{facility.name}</li>)}
                  </ul></div>}
                </div>
              </section>
            )}

            {(property?.houseRules || property?.importantInfo) && (
              <section className="unit-section">
                <div className="unit-section-heading"><span>Before you book</span><h2>Good to know</h2></div>
                <div className="unit-notes-grid">
                  {property?.houseRules && <article><h3>House rules</h3><p>{property.houseRules}</p></article>}
                  {property?.importantInfo && <article><h3>Important information</h3><p>{property.importantInfo}</p></article>}
                </div>
              </section>
            )}
          </div>

          <aside className="unit-booking-card" aria-label="Booking summary">
            <div className="unit-booking-title">
              <div><span>Total for your stay</span><strong>{formatPrice(totalPrice)}</strong></div>
              <span className="unit-night-count">{nights} {nights === 1 ? "night" : "nights"}</span>
            </div>
            <div className="unit-date-grid">
              <div><span>Check-in</span><strong>{formatDate(startDate)}</strong></div>
              <div><span>Check-out</span><strong>{formatDate(endDate)}</strong></div>
            </div>

            {addons?.length > 0 && <div className="unit-addon-section">
              <div className="unit-addon-heading"><h3>Enhance your stay</h3><span>Optional</span></div>
              <div className="unit-addon-list">{addons.map((addon) => {
                const selected = chosenAddons.includes(addon.addonID);
                return <button className={`unit-addon-option ${selected ? "is-selected" : ""}`}
                  key={addon.addonID} type="button" aria-pressed={selected}
                  onClick={() => toggleAddon(addon.addonID)}>
                  <span className="unit-addon-check" aria-hidden="true">{selected ? "✓" : "+"}</span>
                  <span className="unit-addon-copy"><strong>{addon.name}</strong>
                    <small>{addon.perNight ? "Charged per night" : "One-time charge"}</small></span>
                  <strong className="unit-addon-price">{formatPrice(addon.price)}</strong>
                </button>;
              })}</div>
            </div>}

            <div className="unit-price-breakdown">
              <div><span>{formatPrice(nightlyPrice)} × {nights} {nights === 1 ? "night" : "nights"}</span><strong>{formatPrice(stayPrice)}</strong></div>
              {addonsTotal > 0 && <div><span>Selected extras</span><strong>{formatPrice(addonsTotal)}</strong></div>}
              <div className="unit-price-total"><span>Total</span><strong>{formatPrice(totalPrice)}</strong></div>
            </div>
            <button className="unit-booking-button" type="button" onClick={handleBooking} disabled={isBooking}>
              {isBooking ? "Confirming..." : "Reserve this unit"}
            </button>
            <p className="unit-booking-note">Your booking details will be available in your profile.</p>
            {bookingError && <p className="unit-booking-error" role="alert">{bookingError}</p>}
          </aside>
        </div>
      </div>

      {activeImageIndex !== null && <div className="unit-lightbox" role="dialog" aria-modal="true"
        aria-label={`${unitName} photo gallery`} tabIndex="-1" autoFocus onKeyDown={handleLightboxKeyDown}>
        <button className="unit-lightbox-close" type="button" aria-label="Close photo gallery"
          onClick={() => setActiveImageIndex(null)}>×</button>
        {galleryImages.length > 1 && <button className="unit-lightbox-arrow is-previous" type="button"
          aria-label="Previous photo" onClick={() => moveLightbox(-1)}>‹</button>}
        <img src={galleryImages[activeImageIndex].url} alt={`${unitName} — photo ${activeImageIndex + 1}`}
          onError={(event) => { event.currentTarget.src = imageFallback; }} />
        {galleryImages.length > 1 && <button className="unit-lightbox-arrow is-next" type="button"
          aria-label="Next photo" onClick={() => moveLightbox(1)}>›</button>}
        <span className="unit-lightbox-count">{activeImageIndex + 1} / {galleryImages.length}</span>
      </div>}
    </main>
  );
};

export default DetailedBookableUnit;
