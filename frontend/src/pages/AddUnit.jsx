import { useState } from "react";
import { useParams } from "react-router-dom";
import usePostProperty from "../hooks/usePostProperty";

const AddUnit = () => {
  const { propertyId } = useParams();

  const [maxCapacity, setMaxCapacity] = useState(0);
  const [squareMeters, setSquareMeters] = useState(0);
  const [totalUnits, setTotalUnits] = useState(0);
  const [singleBeds, setSingleBeds] = useState(0);
  const [doubleBeds, setDoubleBeds] = useState(0);
  const [maxAdultCapacity, setMaxAdultCapacity] = useState(0);
  const [maxKidsCapacity, setMaxKidsCapacity] = useState(0);
  const [name, setName] = useState("");

  const unitForCreation = {
    maxCapacity,
    squareMeters,
    totalUnits,
    singleBeds,
    doubleBeds,
    maxAdultCapacity,
    maxKidsCapacity,
    name,
  };

  const { data, isLoading, error, postProperty } = usePostProperty();

  const handleSubmit = async (e) => {
    e.preventDefault();
    await postProperty(
      `http://localhost:8080/api/properties/${propertyId}/add-unit`,
      unitForCreation,
    );
  };
  return (
    <div className="page">
      <form className="form-card" onSubmit={handleSubmit}>
        <h2 className="form-title">Add Unit</h2>
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
          <label htmlFor="maxCapacity">Max capacity</label>
          <input
            id="maxCapacity"
            type="number"
            name="maxCapacity"
            value={maxCapacity}
            onChange={(e) => setMaxCapacity(e.target.value)}
          />
        </div>
        <div className="form-field">
          <label htmlFor="squareMeters">Square Meters</label>
          <input
            type="number"
            step="0.1"
            value={squareMeters}
            onChange={(e) => setSquareMeters(parseFloat(e.target.value))}
          />
        </div>
        <div className="form-field">
          <label htmlFor="totalUnits">Total Units</label>
          <input
            id="totalUnits"
            type="number"
            name="totalUnits"
            value={totalUnits}
            onChange={(e) => setTotalUnits(e.target.value)}
          />
        </div>
        <div className="form-field">
          <label htmlFor="singleBeds">Single beds</label>
          <input
            id="singleBeds"
            type="number"
            name="singleBeds"
            value={singleBeds}
            onChange={(e) => setSingleBeds(e.target.value)}
          />
        </div>
        <div className="form-field">
          <label htmlFor="doubleBeds">Double Beds</label>
          <input
            id="doubleBeds"
            type="number"
            name="doubleBeds"
            value={doubleBeds}
            onChange={(e) => setDoubleBeds(e.target.value)}
          />
        </div>
        <div className="form-field">
          <label htmlFor="maxAdultCapacity">Max Adult Capcity</label>
          <input
            id="maxAdultCapacity"
            type="number"
            name="maxAdultCapacity"
            value={maxAdultCapacity}
            onChange={(e) => setMaxAdultCapacity(e.target.value)}
          />
        </div>
        <div className="form-field">
          <label htmlFor="totalUnits">Max Kids Capacity</label>
          <input
            id="maxKidsCapacity"
            type="number"
            name="maxKidsCapacity"
            value={maxKidsCapacity}
            onChange={(e) => setMaxKidsCapacity(e.target.value)}
          />
        </div>

        <button className="submit-btn">Add Unit</button>

        {error && <div className="error">{error}</div>}
        {data && <div className="error">{data.name}</div>}
      </form>
    </div>
  );
};

export default AddUnit;
