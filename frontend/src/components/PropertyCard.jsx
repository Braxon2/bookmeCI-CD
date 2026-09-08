import { useNavigate } from "react-router-dom";
import notFound from "../assets/images/Image-not-found.png";
import { useFetch } from "../hooks/useFetch";
import "./styles/PropertyCard.css";

const PropertyCard = ({ property }) => {
  const apiURL = import.meta.env.VITE_API_URL || "";
  const navigate = useNavigate();
  const { data: thumbnail } = useFetch(`${apiURL}/api/properties/${property.publicId}/thumbnail`);
  const facilities = property.fascilitiesDTO || [];

  return (
    <article className="owner-property-card">
      <div className="owner-property-image"><img src={thumbnail?.url || notFound} alt={property.name}
        onError={(e) => { e.currentTarget.src = notFound; }} /><span>{property.propertyTypeDTO?.name || "Property"}</span></div>
      <div className="owner-property-content">
        <div><span className="owner-property-location">{property.city}, {property.country}</span><h2>{property.name}</h2><p>{property.address}</p></div>
        {facilities.length > 0 && <div className="owner-property-facilities">{facilities.slice(0, 3).map((facility) => <span key={facility.id}>{facility.name}</span>)}{facilities.length > 3 && <span>+{facilities.length - 3}</span>}</div>}
        <div className="owner-property-actions"><button type="button" onClick={() => navigate(`/properties/${property.publicId}/units`)}>Manage units <span>→</span></button>
          <button className="is-secondary" type="button" onClick={() => navigate(`/properties/${property.publicId}/images`)}>Manage photos</button></div>
      </div>
    </article>
  );
};

export default PropertyCard;
