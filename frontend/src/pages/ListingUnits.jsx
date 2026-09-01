import { useNavigate, useParams } from "react-router-dom";
import BookableUnit from "../components/BookableUnit";
import { useFetch } from "../hooks/useFetch";
import "./styles/ListingUnits.css";
const ListingUnits = () => {
  const { publicId } = useParams();
  const apiURL = import.meta.env.VITE_API_URL || "";

  const { data: unitsResponse } = useFetch(
    publicId ? `${apiURL}/api/properties/${publicId}/units` : null,
  );

  const units = unitsResponse?.content || [];

  const navigate = useNavigate();
  const navigateToAddUnitPage = () => {
    navigate(`/properties/${publicId}/add-unit`);
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
