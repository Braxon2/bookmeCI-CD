import { useState } from "react";
import Facilities from "../components/Facilities";
import { useFetch } from "../hooks/useFetch";
import usePostProperty from "../hooks/usePostProperty";
import "./styles/AddProperty.css";

const AddProperty = () => {
  const apiURL = import.meta.env.VITE_API_URL || "";
  const { data: types } = useFetch(`${apiURL}/api/property-type`);
  const { data, isLoading, error, postProperty } = usePostProperty();

  const [name, setName] = useState("");
  const [city, setCity] = useState("");
  const [country, setCountry] = useState("");
  const [typeId, setTypeId] = useState("");
  const [description, setDescription] = useState("");
  const [address, setAddress] = useState("");
  const [houseRules, setHouseRules] = useState("");
  const [importantInfo, setImportantInfo] = useState("");
  const [fascilities, setFascilities] = useState([]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    await postProperty(`${apiURL}/api/properties`, {
      name: name.trim(),
      country: country.trim(),
      city: city.trim(),
      propertyTypeDTO: { id: Number(typeId) },
      description: description.trim(),
      address: address.trim(),
      houseRules: houseRules.trim(),
      importantInfo: importantInfo.trim(),
      fascilitiesDTO: fascilities.map((facility) => ({ id: facility.id })),
    });
  };

  return (
    <main className="owner-property-page">
      <div className="owner-property-shell">
        <header className="owner-property-header">
          <span>Owner workspace</span>
          <h1>Add a new property</h1>
          <p>Tell guests what makes your place special. You can add units, pricing, and photos after creating the property.</p>
        </header>

        <div className="owner-property-layout">
          <aside className="owner-property-guide">
            <span>Property setup</span>
            <h2>A strong listing starts with clear details.</h2>
            <ol>
              <li className="is-active"><b>1</b><div><strong>Property basics</strong><small>Name, type, and description</small></div></li>
              <li><b>2</b><div><strong>Location</strong><small>Where guests will stay</small></div></li>
              <li><b>3</b><div><strong>Amenities & policies</strong><small>Set expectations clearly</small></div></li>
            </ol>
            <div className="owner-property-tip"><strong>Tip</strong><p>Use a recognizable property name and a description that highlights location, atmosphere, and ideal guests.</p></div>
          </aside>

          <form className="owner-property-form" onSubmit={handleSubmit}>
            <section className="owner-form-section">
              <div className="owner-section-heading"><span>01</span><div><h2>Property basics</h2><p>Introduce the property guests will be booking.</p></div></div>
              <div className="owner-form-grid">
                <label className="owner-field owner-field-wide"><span>Property name</span>
                  <input required value={name} onChange={(e) => setName(e.target.value)} placeholder="e.g. Riverside House" />
                  <small>The public name shown in search results.</small>
                </label>
                <label className="owner-field"><span>Property type</span>
                  <select required value={typeId} onChange={(e) => setTypeId(e.target.value)}>
                    <option value="">Select a property type</option>
                    {types?.map((type) => <option key={type.id} value={type.id}>{type.name}</option>)}
                  </select>
                </label>
                <label className="owner-field owner-field-wide"><span>Description</span>
                  <textarea required rows="6" value={description} onChange={(e) => setDescription(e.target.value)}
                    placeholder="Describe the atmosphere, location, and what guests can expect..." />
                  <small>{description.length} characters</small>
                </label>
              </div>
            </section>

            <section className="owner-form-section">
              <div className="owner-section-heading"><span>02</span><div><h2>Location</h2><p>Help guests understand where the property is situated.</p></div></div>
              <div className="owner-form-grid owner-location-grid">
                <label className="owner-field"><span>Country</span><input required value={country} onChange={(e) => setCountry(e.target.value)} placeholder="Serbia" /></label>
                <label className="owner-field"><span>City</span><input required value={city} onChange={(e) => setCity(e.target.value)} placeholder="Belgrade" /></label>
                <label className="owner-field owner-field-wide"><span>Street address</span><input required value={address} onChange={(e) => setAddress(e.target.value)} placeholder="Street and building number" /></label>
              </div>
            </section>

            <section className="owner-form-section">
              <div className="owner-section-heading"><span>03</span><div><h2>Amenities & policies</h2><p>Show what is included and communicate important expectations.</p></div></div>
              <Facilities selectedFacilities={fascilities} setSelectedFacilities={setFascilities} />
              <div className="owner-form-grid owner-policy-grid">
                <label className="owner-field"><span>House rules</span><textarea rows="5" value={houseRules} onChange={(e) => setHouseRules(e.target.value)} placeholder="Check-in times, quiet hours, pets..." /></label>
                <label className="owner-field"><span>Important information</span><textarea rows="5" value={importantInfo} onChange={(e) => setImportantInfo(e.target.value)} placeholder="Parking, access, deposits, or special instructions..." /></label>
              </div>
            </section>

            <footer className="owner-form-footer">
              <div><strong>Ready to create your property?</strong><span>You can update its units and images next.</span></div>
              <button type="submit" disabled={isLoading}>{isLoading ? "Creating property..." : "Create property"}<span aria-hidden="true">→</span></button>
            </footer>
            {error && <p className="owner-form-message is-error" role="alert">{error}</p>}
            {data && <p className="owner-form-message is-success" role="status"><strong>{data.name}</strong> was created successfully.</p>}
          </form>
        </div>
      </div>
    </main>
  );
};

export default AddProperty;
