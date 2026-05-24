import "./styles/AddProperty.css";
import Facilities from "../components/Facilities";
import { useFetch } from "../hooks/useFetch";
import usePostProperty from "../hooks/usePostProperty";
import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

const AddProperty = () => {
  const { data: types } = useFetch("http://localhost:8080/api/property-type");

  const [name, setName] = useState("");
  const [city, setCity] = useState("");
  const [country, setCountry] = useState("");
  const [typeId, setTypeId] = useState("");

  const [description, setDescription] = useState("");
  const [address, setAddress] = useState("");
  const [houseRules, setHouseRules] = useState("");
  const [importantInfo, setImportantInfo] = useState("");
  const [fascilities, setFascilities] = useState([]);

  const navigate = useNavigate();

  const propertyForCreation = {
    name,
    country,
    city,
    propertyTypeDTO: { id: typeId },
    description,
    address,
    houseRules,
    importantInfo,
    fascilitiesDTO: fascilities.map((f) => ({ id: f.id })),
  };

  const { data, isLoading, error, postProperty } = usePostProperty();

  const handleSubmit = async (e) => {
    e.preventDefault();
    await postProperty(
      "http://localhost:8080/api/properties",
      propertyForCreation,
    );
  };
  return (
    <div className="page">
      <form className="form-card" onSubmit={handleSubmit}>
        <h2 className="form-title">Register property</h2>
        <div className="form-field">
          <label htmlFor="name">Name</label>
          <input
            id="name"
            name="name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            type="text"
          />
        </div>
        <div className="form-field">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            name="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>
        <div className="form-field">
          <label htmlFor="country">Country</label>
          <input
            id="country"
            type="text"
            name="country"
            value={country}
            onChange={(e) => setCountry(e.target.value)}
          />
        </div>
        <div className="form-field">
          <label htmlFor="city">City</label>
          <input
            id="city"
            type="text"
            name="city"
            value={city}
            onChange={(e) => setCity(e.target.value)}
          />
        </div>
        <div className="form-field">
          <label htmlFor="address">Address</label>
          <input
            id="address"
            type="text"
            name="caddress"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
          />
        </div>
        <div className="form-field">
          <label htmlFor="importantInfo">Important info</label>
          <textarea
            id="importantInfo"
            name="importantInfo"
            value={importantInfo}
            onChange={(e) => setImportantInfo(e.target.value)}
          />
        </div>
        <div className="form-field">
          <label htmlFor="houseRules">House Rules</label>
          <textarea
            id="houseRules"
            name="houseRules"
            value={houseRules}
            onChange={(e) => setHouseRules(e.target.value)}
          />
        </div>
        <div className="form-field">
          <label htmlFor="type">Type of property</label>

          <select
            id="type"
            name="type"
            value={typeId}
            onChange={(e) => setTypeId(Number(e.target.value))}
          >
            <option value="">Select type</option>
            {types?.map((type) => (
              <option key={type.id} value={type.id}>
                {type.name}
              </option>
            ))}
          </select>
        </div>
        <Facilities
          selectedFacilities={fascilities}
          setSelectedFacilities={setFascilities}
        />
        <button className="submit-btn" disabled={isLoading}>
          Add property
        </button>

        {error && <div className="error">{error}</div>}
        {data && <div className="error">{data.name}</div>}
      </form>
    </div>
  );
};

export default AddProperty;
