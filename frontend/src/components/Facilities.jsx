import { useFetch } from "../hooks/useFetch";

const Facilities = ({ selectedFacilities, setSelectedFacilities }) => {
  const { data: facilities } = useFetch(
    "http://localhost:8080/api/fascilities",
  );

  const handleChange = (facility, checked) => {
    if (checked) {
      setSelectedFacilities((prev) => [...prev, facility]);
    } else {
      setSelectedFacilities((prev) => prev.filter((f) => f.id !== facility.id));
    }
  };

  return (
    <fieldset className="facilities">
      <legend>Facilities</legend>

      <div className="facilities-grid">
        {facilities?.map((facility) => (
          <label key={facility.id} className="facility-item">
            <input
              type="checkbox"
              checked={selectedFacilities.some((f) => f.id === facility.id)}
              onChange={(e) => handleChange(facility, e.target.checked)}
            />
            {facility.name}
          </label>
        ))}
      </div>
    </fieldset>
  );
};

export default Facilities;
