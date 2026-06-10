import { useNavigate, useParams } from "react-router-dom";
import BookableUnit from "../components/BookableUnit";
import { useFetch } from "../hooks/useFetch";
import "./styles/ListingUnits.css";
const ListingUnits = () => {
  const { propertyId } = useParams();
  const apiURL = import.meta.env.VITE_API_URL || "";

  const { data: units } = useFetch(
    propertyId ? `${apiURL}/api/properties/${propertyId}/units` : null,
  );

  const navigate = useNavigate();
  const navigateToAddUnitPage = () => {
    navigate(`/properties/${propertyId}/add-unit`);
  };
  return (
    <div className="pageb">
      <div className="units-list">
        <div className="input-filed">
          <button onClick={navigateToAddUnitPage}>Add a new unit</button>
        </div>
        {units?.map((unit) => (
          <BookableUnit key={unit.id} bookableUnit={unit} />
        ))}
      </div>
    </div>
  );
};

export default ListingUnits;
