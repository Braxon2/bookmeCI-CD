import { useNavigate } from "react-router-dom";
import "./styles/PropertyCard.css";
import { useFetch } from "../hooks/useFetch";
import notFound from "../assets/images/Image-not-found.png";
const PropertyCard = ({ property }) => {
  const navigate = useNavigate();

  const navigateToUnits = () => {
    navigate(`/properties/${property.id}/units`);
  };

  const navigateToPropertyImages = () => {
    navigate(`/properties/${property.id}/images`);
  };

  const { data: thumbnail } = useFetch(
    `http://localhost:8080/api/properties/${property.id}/thumbnail`,
  );
  console.log("Thumbnail data:", thumbnail);
  return (
    <div className="property-card">
      <div className="property-image">
        <img src={thumbnail?.url || notFound} alt="not found" />
      </div>
      <div className="property-info">
        <div className="info-field">
          <h2>{property.name}</h2>
        </div>
        <div className="info-field">
          <p>{property.propertyTypeDTO.name}</p>
        </div>
        <div className="info-field">
          <p>
            {property.city}, {property.country}
          </p>
        </div>
        <div className="info-field">
          <p>{property.address}</p>
        </div>
        <div className="info-field">
          <button onClick={navigateToUnits}>Check Units</button>
        </div>
        <div className="info-field">
          <button onClick={navigateToPropertyImages}>Add Property image</button>
        </div>
      </div>
    </div>
  );
};

export default PropertyCard;
