import { useEffect, useState } from "react";
import { useFetch } from "../hooks/useFetch";
import "./styles/ListingFascilities.css";
import usePost from "../hooks/usePost";
const ListingUnitFacilties = () => {
  const { data } = useFetch("http://localhost:8080/api/unit-fascilities");
  const [unitFacilities, setUnitFacilities] = useState([]);

  const [unitFascility, setUnitFascility] = useState("");

  const { isLoading, error, post } = usePost();

  const handleSubmit = async (e) => {
    e.preventDefault();

    const res = await post("http://localhost:8080/api/unit-fascilities", {
      name: unitFascility,
    });

    if (res && res.id) {
      setUnitFacilities((prev) => [...prev, res]);
      setUnitFascility("");
    }
  };

  useEffect(() => {
    if (data) {
      setUnitFacilities(data);
    }
  }, [data]);

  return (
    <div className="page-fascility">
      <div className="fascility-list">
        <div className="adding-fascility">
          <input
            type="text"
            value={unitFascility}
            onChange={(e) => setUnitFascility(e.target.value)}
          />
          <button onClick={handleSubmit}>Add</button>
        </div>
        <div className="facilities-grid">
          {unitFacilities?.map((unitFacility) => (
            <label key={unitFacility.id} className="facility-item">
              <input type="checkbox" />
              {unitFacility.name}
            </label>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ListingUnitFacilties;
