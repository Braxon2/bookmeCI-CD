import AdminCatalogManager from "../components/AdminCatalogManager";

const ListingUnitFacilties = () => (
  <AdminCatalogManager
    title="Unit facilities"
    description="Create room-level features that help guests compare individual accommodation units."
    itemLabel="Unit facility"
    endpoint="/api/unit-fascilities"
    kind="unit"
  />
);

export default ListingUnitFacilties;
