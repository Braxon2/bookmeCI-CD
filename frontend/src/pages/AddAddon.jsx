import AdminCatalogManager from "../components/AdminCatalogManager";

const AddAddon = () => (
  <AdminCatalogManager
    title="Booking add-ons"
    description="Manage optional services that owners can attach to their bookable units."
    itemLabel="Add-on"
    endpoint="/api/addons"
    kind="addon"
  />
);

export default AddAddon;
