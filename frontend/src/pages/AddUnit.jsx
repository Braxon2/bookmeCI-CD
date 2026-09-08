import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import usePostProperty from "../hooks/usePostProperty";
import "./styles/AddUnit.css";

const AddUnit = () => {
  const apiURL = import.meta.env.VITE_API_URL || "";
  const { propertyId } = useParams();
  const navigate = useNavigate();
  const { data, isLoading, error, postProperty } = usePostProperty();
  const [form, setForm] = useState({
    name: "", maxCapacity: 1, squareMeters: 20, totalUnits: 1,
    singleBeds: 1, doubleBeds: 0, maxAdultCapacity: 1, maxKidsCapacity: 0,
  });

  const updateField = (field, value) => setForm((current) => ({ ...current, [field]: value }));
  const handleSubmit = async (event) => {
    event.preventDefault();
    await postProperty(`${apiURL}/api/properties/${propertyId}/add-unit`, {
      ...form,
      name: form.name.trim(),
      maxCapacity: Number(form.maxCapacity),
      squareMeters: Number(form.squareMeters),
      totalUnits: Number(form.totalUnits),
      singleBeds: Number(form.singleBeds),
      doubleBeds: Number(form.doubleBeds),
      maxAdultCapacity: Number(form.maxAdultCapacity),
      maxKidsCapacity: Number(form.maxKidsCapacity),
    });
  };

  return (
    <main className="add-unit-page">
      <div className="add-unit-shell">
        <button className="add-unit-back" type="button" onClick={() => navigate(-1)}>← Back to property</button>
        <header className="add-unit-header">
          <span>Owner workspace</span><h1>Add a bookable unit</h1>
          <p>Define the room, apartment, or house guests can reserve. Pricing, amenities, add-ons, and images can be configured afterwards.</p>
        </header>
        <form className="add-unit-form" onSubmit={handleSubmit}>
          <section className="add-unit-section">
            <div className="add-unit-section-heading"><span>01</span><div><h2>Unit basics</h2><p>Name the unit and describe its size and availability.</p></div></div>
            <div className="add-unit-grid">
              <label className="add-unit-field is-wide"><span>Unit name</span><input required value={form.name} onChange={(e) => updateField("name", e.target.value)} placeholder="e.g. Deluxe river-view apartment" /><small>Use a name guests can distinguish from your other units.</small></label>
              <label className="add-unit-field"><span>Size in square metres</span><input required min="1" step="0.1" type="number" value={form.squareMeters} onChange={(e) => updateField("squareMeters", e.target.value)} /></label>
              <label className="add-unit-field"><span>Number of identical units</span><input required min="1" type="number" value={form.totalUnits} onChange={(e) => updateField("totalUnits", e.target.value)} /></label>
            </div>
          </section>
          <section className="add-unit-section">
            <div className="add-unit-section-heading"><span>02</span><div><h2>Guests and beds</h2><p>Set safe occupancy and sleeping arrangements.</p></div></div>
            <div className="add-unit-grid">
              <label className="add-unit-field"><span>Maximum guests</span><input required min="1" type="number" value={form.maxCapacity} onChange={(e) => updateField("maxCapacity", e.target.value)} /></label>
              <label className="add-unit-field"><span>Maximum adults</span><input required min="1" type="number" value={form.maxAdultCapacity} onChange={(e) => updateField("maxAdultCapacity", e.target.value)} /></label>
              <label className="add-unit-field"><span>Maximum children</span><input required min="0" type="number" value={form.maxKidsCapacity} onChange={(e) => updateField("maxKidsCapacity", e.target.value)} /></label>
              <label className="add-unit-field"><span>Single beds</span><input required min="0" type="number" value={form.singleBeds} onChange={(e) => updateField("singleBeds", e.target.value)} /></label>
              <label className="add-unit-field"><span>Double beds</span><input required min="0" type="number" value={form.doubleBeds} onChange={(e) => updateField("doubleBeds", e.target.value)} /></label>
            </div>
          </section>
          <footer className="add-unit-footer">
            <div className="add-unit-summary"><span>{form.maxCapacity} guests</span><span>{Number(form.singleBeds) + Number(form.doubleBeds)} beds</span><span>{form.squareMeters} m²</span></div>
            <button className="add-unit-submit" type="submit" disabled={isLoading}>{isLoading ? "Creating unit..." : "Create unit"}</button>
          </footer>
          {error && <p className="add-unit-message is-error" role="alert">{error}</p>}
          {data && <p className="add-unit-message is-success" role="status"><strong>{data.name}</strong> was created. You can now add pricing, amenities, add-ons, and images.</p>}
        </form>
      </div>
    </main>
  );
};

export default AddUnit;
