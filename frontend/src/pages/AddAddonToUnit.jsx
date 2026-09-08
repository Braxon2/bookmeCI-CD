import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useFetch } from "../hooks/useFetch";
import "./styles/AddAddonToUnit.css";

const AddAddonToUnit = () => {
  const apiURL = import.meta.env.VITE_API_URL || "";
  const { unitId } = useParams();
  const navigate = useNavigate();
  const { data: catalog, loading: catalogLoading } = useFetch(`${apiURL}/api/addons`);
  const { data: unit, loading: unitLoading } = useFetch(`${apiURL}/api/units/${unitId}/info`);
  const [addedAddons, setAddedAddons] = useState([]);
  const [removedIds, setRemovedIds] = useState([]);
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState("");

  const originalAddons = unit?.addonList || [];
  const attachedAddons = [...originalAddons, ...addedAddons]
    .filter((addon, index, all) => !removedIds.includes(addon.id) && all.findIndex((item) => item.id === addon.id) === index);
  const availableAddons = (catalog || []).filter((addon) =>
    !attachedAddons.some((attached) => attached.id === addon.id),
  );

  const addAddon = async (addon) => {
    setBusyId(`add-${addon.id}`);
    setError("");
    try {
      const response = await fetch(`${apiURL}/api/units/${unitId}/addons`, {
        method: "POST",
        headers: { Authorization: `Bearer ${localStorage.getItem("jwtToken")}`, "Content-Type": "application/json" },
        body: JSON.stringify({ id: addon.id, name: addon.name }),
      });
      const result = await response.json().catch(() => ({}));
      if (!response.ok) throw new Error(result.message || "The add-on could not be attached.");
      setRemovedIds((current) => current.filter((id) => id !== addon.id));
      setAddedAddons((current) => [...current, addon]);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusyId(null);
    }
  };

  const removeAddon = async (addon) => {
    setBusyId(`remove-${addon.id}`);
    setError("");
    try {
      const response = await fetch(`${apiURL}/api/units/${unitId}/addons/${addon.id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${localStorage.getItem("jwtToken")}` },
      });
      if (!response.ok) {
        const result = await response.json().catch(() => ({}));
        throw new Error(result.message || "The add-on could not be removed.");
      }
      setRemovedIds((current) => [...current, addon.id]);
      setAddedAddons((current) => current.filter((item) => item.id !== addon.id));
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusyId(null);
    }
  };

  const loading = catalogLoading || unitLoading;

  return (
    <main className="unit-addons-page">
      <div className="unit-addons-shell">
        <button className="unit-addons-back" type="button" onClick={() => navigate(-1)}>← Back to units</button>
        <header className="unit-addons-header"><div><span>Owner workspace</span><h1>Manage unit add-ons</h1>
          <p>{unit?.name ? `Choose optional services for ${unit.name}, then configure how each one is priced.` : "Choose optional services for this unit and configure their pricing."}</p></div>
          <strong className="unit-addons-count">{attachedAddons.length}</strong></header>
        {error && <p className="unit-addons-message" role="alert">{error}</p>}

        <div className="unit-addons-layout">
          <section className="unit-addon-panel">
            <div className="unit-addon-panel-heading"><div><span>Catalogue</span><h2>Available add-ons</h2></div><strong>{availableAddons.length}</strong></div>
            {loading ? <div className="unit-addon-empty"><p>Loading add-ons...</p></div> : availableAddons.length ? <div className="unit-addon-cards">
              {availableAddons.map((addon) => <article className="unit-addon-management-card" key={addon.id}>
                <span className="unit-addon-card-icon" aria-hidden="true">+</span><div><strong>{addon.name}</strong><small>Not attached to this unit</small></div>
                <button type="button" disabled={busyId !== null} onClick={() => addAddon(addon)}>{busyId === `add-${addon.id}` ? "Adding..." : "Add"}</button>
              </article>)}
            </div> : <div className="unit-addon-empty"><strong>Everything is attached</strong><p>All catalogue add-ons are already available for this unit.</p></div>}
          </section>

          <section className="unit-addon-panel">
            <div className="unit-addon-panel-heading"><div><span>This unit</span><h2>Attached add-ons</h2></div><strong>{attachedAddons.length}</strong></div>
            {loading ? <div className="unit-addon-empty"><p>Loading unit details...</p></div> : attachedAddons.length ? <div className="unit-addon-cards">
              {attachedAddons.map((addon) => <article className="unit-addon-management-card is-attached" key={addon.id}>
                <span className="unit-addon-card-icon" aria-hidden="true">✓</span><div><strong>{addon.name}</strong><small>Available for guests</small></div>
                <div className="unit-addon-card-actions"><button className="is-secondary" type="button" onClick={() => navigate(`/units/${unitId}/addons/${addon.id}`)}>Set pricing</button>
                  <button className="is-danger" type="button" disabled={busyId !== null} onClick={() => removeAddon(addon)}>{busyId === `remove-${addon.id}` ? "Removing..." : "Remove"}</button></div>
              </article>)}
            </div> : <div className="unit-addon-empty"><strong>No add-ons attached</strong><p>Add one from the catalogue to offer it to guests.</p></div>}
          </section>
        </div>
      </div>
    </main>
  );
};

export default AddAddonToUnit;
