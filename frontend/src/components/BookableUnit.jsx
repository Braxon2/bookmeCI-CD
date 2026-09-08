import { useNavigate } from "react-router-dom";
import notFound from "../assets/images/Image-not-found.png";
import { useFetch } from "../hooks/useFetch";
import "./styles/BookableUnit.css";

const BookableUnit = ({ bookableUnit }) => {
  const apiURL = import.meta.env.VITE_API_URL || "";
  const navigate = useNavigate();
  const unitId = bookableUnit.id;
  const { data: thumbnail } = useFetch(`${apiURL}/api/units/${unitId}/thumbnail`);
  const facilities = bookableUnit.unitFascilityResponseDTOS || [];

  return (
    <article className="owner-unit-card">
      <div className="owner-unit-image"><img src={thumbnail?.url || notFound} alt={bookableUnit.name}
        onError={(e) => { e.currentTarget.src = notFound; }} /><button type="button" onClick={() => navigate(`/units/${unitId}/images`)}>Manage photos</button></div>
      <div className="owner-unit-content">
        <div className="owner-unit-title"><div><span>Bookable unit</span><h2>{bookableUnit.name}</h2></div><strong>{bookableUnit.squareMeters} m²</strong></div>
        <div className="owner-unit-stats"><div><span>Guests</span><strong>{bookableUnit.maxCapacity}</strong></div><div><span>Adults</span><strong>{bookableUnit.maxAdultCapacity}</strong></div><div><span>Children</span><strong>{bookableUnit.maxKidsCapacity}</strong></div><div><span>Beds</span><strong>{bookableUnit.singleBeds + bookableUnit.doubleBeds}</strong></div></div>
        {facilities.length > 0 && <div className="owner-unit-facilities">{facilities.slice(0, 4).map((facility) => <span key={facility.id}>{facility.name}</span>)}{facilities.length > 4 && <span>+{facilities.length - 4}</span>}</div>}
        <div className="owner-unit-actions">
          <button type="button" onClick={() => navigate(`/units/${unitId}/unit-fascilities`, { state: { unitFacilities: facilities } })}>Amenities</button>
          <button type="button" onClick={() => navigate(`/units/${unitId}/add-price`)}>Pricing</button>
          <button type="button" onClick={() => navigate(`/units/${unitId}/addons`)}>Add-ons</button>
          <button className="is-primary" type="button" onClick={() => navigate(`/units/${unitId}/images`)}>Add images</button>
        </div>
      </div>
    </article>
  );
};

export default BookableUnit;
