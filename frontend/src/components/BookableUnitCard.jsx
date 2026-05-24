import { useNavigate } from "react-router-dom";

const BookableUnitCard = ({ bookableUnit, checkIn, checkOut }) => {
  const navigate = useNavigate();

  const goToUnitPage = (e) => {
    e.preventDefault();
    navigate(
      `/units/${bookableUnit.unitId}?startDate=${checkIn}&endDate=${checkOut}`,
    );
  };

  return (
    <div className="unit-card" onClick={goToUnitPage}>
      <div className="unit-image">
        <img src={bookableUnit.imageUrl} alt="unit" />
      </div>
      <div className="unit-info">
        <div className="info-field">
          <p>Property name: {bookableUnit.propertyName}</p>
        </div>
        <div className="info-field">
          <h2>{bookableUnit.unitName}</h2>
        </div>
        <div className="info-field">
          <h2>{bookableUnit.totalPriceForStay}</h2>
        </div>

        {bookableUnit.doubleBeds > 0 ? (
          <div className="info-field">
            <p>Double beds: {bookableUnit.doubleBeds}</p>
          </div>
        ) : null}
        {bookableUnit.singleBeds > 0 ? (
          <div className="info-field">
            <p>Single beds: {bookableUnit.singleBeds}</p>
          </div>
        ) : null}
      </div>
    </div>
  );
};

export default BookableUnitCard;
