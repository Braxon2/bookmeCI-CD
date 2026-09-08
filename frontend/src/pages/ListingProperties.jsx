import { useNavigate } from "react-router-dom";
import PropertyCard from "../components/PropertyCard";
import { useAuth } from "../context/AuthContext";
import { useFetch } from "../hooks/useFetch";
import "./styles/ListingProperties.css";

const ListingProperties = () => {
  const { user } = useAuth();
  const apiURL = import.meta.env.VITE_API_URL || "";
  const navigate = useNavigate();
  const { data: properties, loading, error } = useFetch(
    user?.id ? `${apiURL}/api/users/${user.id}/properties` : null,
  );

  return (
    <main className="owner-properties-page">
      <div className="owner-properties-shell">
        <header className="owner-properties-header"><div><span>Owner workspace</span><h1>Your properties</h1>
          <p>Manage accommodation units, photos, pricing, amenities, and extras.</p></div>
          <button type="button" onClick={() => navigate("/add-property")}>+ Add property</button></header>

        {(!user || loading) && <div className="owner-properties-state"><span /><p>Loading your properties...</p></div>}
        {!loading && error && <div className="owner-properties-state"><h2>Couldn&apos;t load properties</h2><p>{error}</p></div>}
        {!loading && !error && properties?.length === 0 && <div className="owner-properties-state"><h2>Add your first property</h2><p>Create a property before adding bookable units and photos.</p><button type="button" onClick={() => navigate("/add-property")}>Create property</button></div>}
        {!loading && properties?.length > 0 && <section className="owner-properties-grid" aria-label="Your properties">
          {properties.map((property) => <PropertyCard key={property.publicId} property={property} />)}
        </section>}
      </div>
    </main>
  );
};

export default ListingProperties;
