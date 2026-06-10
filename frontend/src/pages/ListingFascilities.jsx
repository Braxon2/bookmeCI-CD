import { useEffect, useState } from "react";
import { useFetch } from "../hooks/useFetch";
import "./styles/ListingFascilities.css";
import usePost from "../hooks/usePost";
const ListingFascilities = () => {
  const apiURL = import.meta.env.VITE_API_URL || "";
  const { data } = useFetch(`${apiURL}/api/fascilities`);
  const [facilities, setFacilities] = useState([]);

  const [fascility, setFascility] = useState("");

  const { isLoading, error, post } = usePost();

  const handleSubmit = async (e) => {
    e.preventDefault();

    const res = await post(`${apiURL}/api/fascilities`, {
      name: fascility,
    });
    console.log(apiURL);

    if (res && res.id) {
      setFacilities((prev) => [...prev, res]);
      setFascility("");
    }
  };

  useEffect(() => {
    if (data) {
      setFacilities(data);
    }
  }, [data]);

  return (
    <div className="page-fascility">
      <div className="fascility-list">
        <div className="adding-fascility">
          <input
            type="text"
            value={fascility}
            onChange={(e) => setFascility(e.target.value)}
          />
          <button onClick={handleSubmit}>Add</button>
        </div>
        <div className="facilities-grid">
          {facilities?.map((facility) => (
            <label key={facility.id} className="facility-item">
              {facility.name}
            </label>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ListingFascilities;
