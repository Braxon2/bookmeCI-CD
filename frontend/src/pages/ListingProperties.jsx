import PropertyCard from "../components/PropertyCard";
import { useAuth } from "../context/AuthContext";
import { useFetch } from "../hooks/useFetch";
import "./styles/ListingProperties.css";
const ListingProperties = () => {
  const { user } = useAuth();
  const apiURL = import.meta.env.VITE_API_URL || "http://localhost:8080";
  const userId = user?.id;
  const { data: properties } = useFetch(
    userId ? `${apiURL}/api/users/${userId}/properties` : null,
  );
  if (!user) {
    return <p>Loading...</p>;
  }
  return (
    <div className="pagep">
      <div className="property-list">
        {properties?.map((p) => (
          <PropertyCard key={p.id} property={p} />
        ))}
      </div>
    </div>
  );
};

export default ListingProperties;
