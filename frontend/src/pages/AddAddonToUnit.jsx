import { useEffect, useState } from "react";
import { useFetch } from "../hooks/useFetch";
import "./styles/ListingFascilities.css";
import usePost from "../hooks/usePost";
import {
  Navigate,
  useLocation,
  useNavigate,
  useParams,
} from "react-router-dom";

const AddAddonToUnit = () => {
  const { data } = useFetch("http://localhost:8080/api/addons");
  const [addons, setAddons] = useState([]);

  const { error, post } = usePost();
  const navigate = useNavigate();
  const { unitId } = useParams();

  const { data: unit } = useFetch(
    `http://localhost:8080/api/units/${unitId}/info`,
  );

  const unitAddons = unit?.addonList || [];

  const availableAddons = addons.filter(
    (addon) => !unitAddons.some((unitAddon) => unitAddon.id === addon.id),
  );

  const handleSubmit = async (e, id, name) => {
    e.preventDefault();
    const res = await post(`http://localhost:8080/api/units/${unitId}/addons`, {
      id: id,
      name: name,
    });

    if (res) {
      navigate(`/units/${unitId}/addons/${id}`);
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
        <h3>Available Addons</h3>
        <div className="facilities-grid">
          {availableAddons.length > 0 ? (
            availableAddons.map((addon) => (
              <button
                key={addon.id}
                onClick={(e) => handleSubmit(e, addon.id, addon.name)}
                id={addon.id}
              >
                {addon.name}
              </button>
            ))
          ) : (
            <p>No available addons left to add.</p>
          )}
        </div>
        {error && <div>{error}</div>}
      </div>

      <div className="fascility-list">
        <h3>Unit Addons</h3>
        <div className="facilities-grid">
          {unitAddons.length > 0 ? (
            unitAddons.map((addon) => (
              <button
                key={addon.id}
                onClick={() => navigate(`/units/${unitId}/addons/${addon.id}`)}
                className="unit-addon-btn"
              >
                {addon.name}
              </button>
            ))
          ) : (
            <p>This unit has no addons yet.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default AddAddonToUnit;
