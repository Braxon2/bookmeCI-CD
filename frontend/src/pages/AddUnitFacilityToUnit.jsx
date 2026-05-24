import { useEffect, useState } from "react";
import { useFetch } from "../hooks/useFetch";
import "./styles/ListingFascilities.css";
import usePost from "../hooks/usePost";
import { useLocation, useParams } from "react-router-dom";
const AddUnitFacilityToUnit = () => {
  const { data } = useFetch("http://localhost:8080/api/unit-fascilities");
  const [unitFacilities, setUnitFacilities] = useState([]);
  const [selectedFacilties, setSelectedFacilties] = useState([]);

  const { error, post } = usePost();

  const { unitId } = useParams();

  const { data: unit } = useFetch(`http://localhost:8080/api/units/${unitId}`);
  const faciltiesFromUnit = unit?.unitFascilityDTO;

  const handleSubmit = async (e) => {
    e.preventDefault();

    const res = await post(
      `http://localhost:8080/api/units/${unitId}/add-unit-facilities`,
      {
        facilityIds: selectedFacilties,
      },
    );
  };

  const handleSelectedBox = (checked, id) => {
    if (checked) {
      setSelectedFacilties((prev) => [...prev, id]);
    } else {
      setSelectedFacilties((prev) => prev.filter((f) => f !== id));
    }
  };

  useEffect(() => {
    if (data) {
      setUnitFacilities(data);
    }
    if (faciltiesFromUnit) {
      const existingIds = faciltiesFromUnit.map((facility) => facility.id);
      setSelectedFacilties(existingIds);
    }
  }, [data, faciltiesFromUnit]);

  return (
    <div className="page-fascility">
      <div className="fascility-list">
        <div className="facilities-grid">
          {unitFacilities?.map((unitFacility) => (
            <label key={unitFacility.id} className="facility-item">
              <input
                type="checkbox"
                checked={selectedFacilties.includes(unitFacility.id)}
                onChange={(e) =>
                  handleSelectedBox(e.target.checked, unitFacility.id)
                }
              />
              {unitFacility.name}
            </label>
          ))}
        </div>
        <button onClick={handleSubmit}>Add facilities</button>
        {error && <div>error </div>}
      </div>
    </div>
  );
};

export default AddUnitFacilityToUnit;
