import AdminCatalogManager from "../components/AdminCatalogManager";

const ListingFascilities = () => (
  <AdminCatalogManager
    title="Property facilities"
    description="Maintain the shared amenities owners can offer across an entire property."
    itemLabel="Facility"
    endpoint="/api/fascilities"
    kind="property"
  />
);

export default ListingFascilities;
