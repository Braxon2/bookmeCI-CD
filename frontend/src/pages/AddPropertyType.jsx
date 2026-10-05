import AdminCatalogManager from "../components/AdminCatalogManager";

const AddPropertyType = () => (
  <AdminCatalogManager
    title="Property types"
    description="Manage the accommodation categories owners can select when they create a property."
    itemLabel="Property type"
    endpoint="/api/property-type"
    kind="property-type"
    example="Boutique hotel"
    symbol="⌂"
  />
);

export default AddPropertyType;
