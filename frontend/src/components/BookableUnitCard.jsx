import { useNavigate } from "react-router-dom";
import notFound from "../assets/images/Image-not-found.png";
import "./styles/BookableUnit.css";

const currencyFormatter = new Intl.NumberFormat("en-GB", {
  style: "currency",
  currency: "EUR",
  minimumFractionDigits: 2,
});

const BookableUnitCard = ({ bookableUnit, checkIn, checkOut }) => {
  const navigate = useNavigate();
  const nights = checkIn && checkOut
    ? Math.max(1, Math.round((new Date(`${checkOut}T00:00:00`) - new Date(`${checkIn}T00:00:00`)) / 86400000))
    : 1;
  const total = Number(bookableUnit.totalPriceForStay) || 0;
  const address = [bookableUnit.address, bookableUnit.city, bookableUnit.country].filter(Boolean).join(", ");

  const goToUnitPage = () => {
    navigate(`/units/${bookableUnit.unitId}?startDate=${checkIn}&endDate=${checkOut}`);
  };

  return (
    <article className="search-unit-card">
      <div className="search-unit-image">
        <img src={bookableUnit.imageUrl || notFound} alt={`${bookableUnit.unitName} at ${bookableUnit.propertyName}`}
          onError={(event) => { event.currentTarget.src = notFound; }} />
        <span>{nights} {nights === 1 ? "night" : "nights"}</span>
      </div>
      <div className="search-unit-content">
        <div className="search-unit-main">
          <span className="search-unit-property">{bookableUnit.propertyName}</span>
          <h3>{bookableUnit.unitName}</h3>
          <p className="search-unit-address">{address}</p>
          <div className="search-unit-details">
            {bookableUnit.doubleBeds > 0 && <span>{bookableUnit.doubleBeds} double {bookableUnit.doubleBeds === 1 ? "bed" : "beds"}</span>}
            {bookableUnit.singleBeds > 0 && <span>{bookableUnit.singleBeds} single {bookableUnit.singleBeds === 1 ? "bed" : "beds"}</span>}
          </div>
          <p className="search-unit-availability"><span aria-hidden="true">✓</span> Available for your dates</p>
        </div>
        <div className="search-unit-price">
          <span>Total stay</span>
          <strong>{currencyFormatter.format(total)}</strong>
          <small>{currencyFormatter.format(total / nights)} per night</small>
          <button type="button" onClick={goToUnitPage}>View details <span aria-hidden="true">→</span></button>
        </div>
      </div>
    </article>
  );
};

export default BookableUnitCard;
