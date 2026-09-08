import { useNavigate, useParams } from "react-router-dom";
import BookableUnit from "../components/BookableUnit";
import { useFetch } from "../hooks/useFetch";
import "./styles/ListingUnits.css";

const ListingUnits = () => {
  const { publicId } = useParams();
  const apiURL = import.meta.env.VITE_API_URL || "";
  const navigate = useNavigate();
  const { data: unitsResponse, loading, error } = useFetch(
    publicId ? `${apiURL}/api/properties/${publicId}/units` : null,
  );
  const units = unitsResponse?.content || [];

  return (
    <main className="owner-units-page"><div className="owner-units-shell">
      <button className="owner-units-back" type="button" onClick={() => navigate("/list-properties")}>← All properties</button>
      <header className="owner-units-header"><div><span>Owner workspace</span><h1>Bookable units</h1><p>Manage capacity, photos, amenities, seasonal prices, and optional services.</p></div>
        <button type="button" onClick={() => navigate(`/properties/${publicId}/add-unit`)}>+ Add unit</button></header>
      {loading && <div className="owner-units-state"><p>Loading units...</p></div>}
      {!loading && error && <div className="owner-units-state"><h2>Couldn&apos;t load units</h2><p>{error}</p></div>}
      {!loading && !error && units.length === 0 && <div className="owner-units-state"><h2>No units yet</h2><p>Add the first room, apartment, or house guests can book.</p><button type="button" onClick={() => navigate(`/properties/${publicId}/add-unit`)}>Add unit</button></div>}
      {!loading && units.length > 0 && <section className="owner-unit-list">{units.map((unit) => <BookableUnit key={unit.id} bookableUnit={unit} />)}</section>}
    </div></main>
  );
};

export default ListingUnits;
