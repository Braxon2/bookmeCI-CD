import { useState } from "react";
import { useFetch } from "../hooks/useFetch";
import usePost from "../hooks/usePost";
import "./styles/AdminCatalogManager.css";

const AdminCatalogManager = ({ title, description, itemLabel, endpoint, kind }) => {
  const apiURL = import.meta.env.VITE_API_URL || "";
  const { data, loading, error: fetchError } = useFetch(`${apiURL}${endpoint}`);
  const { isLoading, error: postError, post } = usePost();
  const [name, setName] = useState("");
  const [createdItems, setCreatedItems] = useState([]);
  const [localError, setLocalError] = useState("");
  const items = [...(data || []), ...createdItems];

  const handleSubmit = async (event) => {
    event.preventDefault();
    const cleanName = name.trim();
    if (!cleanName) {
      setLocalError(`Enter a name for the ${itemLabel.toLowerCase()}.`);
      return;
    }
    if (items.some((item) => item.name.toLowerCase() === cleanName.toLowerCase())) {
      setLocalError(`${cleanName} already exists in this catalogue.`);
      return;
    }

    setLocalError("");
    const result = await post(`${apiURL}${endpoint}`, { name: cleanName });
    if (result?.id) {
      setCreatedItems((current) => [...current, result]);
      setName("");
    }
  };

  return (
    <main className="admin-catalog-page">
      <div className="admin-catalog-shell">
        <header className="admin-catalog-header">
          <div>
            <span>Admin catalogue</span>
            <h1>{title}</h1>
            <p>{description}</p>
          </div>
          <div className={`admin-catalog-symbol is-${kind}`} aria-hidden="true">
            {kind === "addon" ? "+" : "✓"}
          </div>
        </header>

        <section className="admin-create-card">
          <div>
            <span>Create new</span>
            <h2>Add {itemLabel.toLowerCase()}</h2>
            <p>New entries become available to owners when they configure their listings.</p>
          </div>
          <form onSubmit={handleSubmit}>
            <label htmlFor={`${kind}-name`}>{itemLabel} name</label>
            <div className="admin-create-row">
              <input id={`${kind}-name`} value={name} onChange={(event) => setName(event.target.value)}
                placeholder={`e.g. ${kind === "addon" ? "Airport transfer" : kind === "unit" ? "Air conditioning" : "Swimming pool"}`} />
              <button type="submit" disabled={isLoading}>{isLoading ? "Adding..." : `Add ${itemLabel.toLowerCase()}`}</button>
            </div>
            {(localError || postError) && <p className="admin-form-error" role="alert">{localError || postError}</p>}
          </form>
        </section>

        <section className="admin-list-card">
          <div className="admin-list-heading">
            <div><span>Current catalogue</span><h2>{itemLabel}s</h2></div>
            <strong>{items.length} {items.length === 1 ? "entry" : "entries"}</strong>
          </div>

          {loading && <div className="admin-list-state"><span className="admin-loader" /><p>Loading catalogue...</p></div>}
          {!loading && fetchError && <div className="admin-list-state is-error"><h3>Couldn&apos;t load the catalogue</h3><p>{fetchError}</p></div>}
          {!loading && !fetchError && items.length === 0 && <div className="admin-list-state"><h3>No entries yet</h3><p>Add the first {itemLabel.toLowerCase()} using the form above.</p></div>}
          {!loading && items.length > 0 && <div className="admin-catalog-grid">
            {items.map((item, index) => <article key={item.id ?? `${item.name}-${index}`}>
              <span className="admin-item-index">{String(index + 1).padStart(2, "0")}</span>
              <div><strong>{item.name}</strong><small>Active and available</small></div>
              <span className="admin-item-status" aria-label="Active">✓</span>
            </article>)}
          </div>}
        </section>
      </div>
    </main>
  );
};

export default AdminCatalogManager;
