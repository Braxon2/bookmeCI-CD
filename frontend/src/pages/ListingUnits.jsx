import { useNavigate, useParams } from "react-router-dom";
import BookableUnit from "../components/BookableUnit";
import { useFetch } from "../hooks/useFetch";
import "./styles/ListingUnits.css";
const ListingUnits = () => {
  const { propertyId } = useParams();

  const { data: units } = useFetch(
    propertyId
      ? `http://localhost:8080/api/properties/${propertyId}/units`
      : null,
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
