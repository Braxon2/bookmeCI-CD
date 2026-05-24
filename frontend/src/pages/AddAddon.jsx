import { useEffect, useState } from "react";
import { useFetch } from "../hooks/useFetch";
import "./styles/ListingFascilities.css";
import usePost from "../hooks/usePost";
import { useLocation, useParams } from "react-router-dom";
const AddAddon = () => {
  const { data } = useFetch("http://localhost:8080/api/addons");
  const [addons, setAddons] = useState([]);
  const [selectedFacilties, setSelectedFacilties] = useState([]);

  const [addonName, setAddonName] = useState("");

  const { error, post } = usePost();

  const { unitId } = useParams();

  const handleSubmit = async (e) => {
    e.preventDefault();

    console.log(addonName);

    const res = await post(`http://localhost:8080/api/addons`, {
      name: addonName,
    });

    if (res && res.id) {
      setAddons((prev) => [...prev, res]);
      setAddonName("");
    }
  };

  useEffect(() => {
    if (data) {
      setAddons(data);
    }
  }, [data]);

  return (
    <div className="page-fascility">
      <div className="fascility-list">
        <div className="adding-fascility">
          <input
            type="text"
            value={addonName}
            onChange={(e) => setAddonName(e.target.value)}
          />
          <button onClick={handleSubmit}>Add</button>
        </div>
        <div className="facilities-grid">
          {addons?.map((addon) => (
            <label key={addon.id} className="facility-item">
              <input
                type="checkbox"
                onChange={(e) => setAddon(e.target.value)}
              />
              {addon.name}
            </label>
          ))}
        </div>
        {/* <button onClick={handleSubmit}>Add addon</button> */}
        {error && <div>error </div>}
      </div>
    </div>
  );
};

export default AddAddon;
