// import "./App.css";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import Navbar from "./components/Navbar";
import Login from "./pages/Login";
import Home from "./pages/Home";
import Register from "./pages/Register";
import Search from "./pages/Search";
import AddProperty from "./pages/AddProperty";
import ListingProperties from "./pages/ListingProperties";
import ListingUnits from "./pages/ListingUnits";
import ListingFascilities from "./pages/ListingFascilities";
import PropertyImages from "./pages/PropertyImages";
import AddUnit from "./pages/AddUnit";
import ListingUnitFacilties from "./pages/ListingUnitFacilties";
import AddUnitFacilityToUnit from "./pages/AddUnitFacilityToUnit";
import AddPeriodPriceUnit from "./pages/AddPeriodPriceUnit";
import SearchWithfilter from "./pages/SearchWithFilters";
import DetailedBookableUnit from "./pages/DetailedBookableUnit";
import UserProfile from "./pages/UserProfile";
import AddAddon from "./pages/AddAddon";
import AddAddonToUnit from "./pages/AddAddonToUnit";
import AddPeriodPriceAddon from "./pages/AddPeriodpriceAddon";
function App() {
  return (
    <BrowserRouter>
      <Navbar />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route path="/search" element={<Search />} />
        <Route path="/search-with-filter" element={<SearchWithfilter />} />

        <Route path="/add-property" element={<AddProperty />} />
        <Route path="/list-properties" element={<ListingProperties />} />

        <Route
          path="/properties/:propertyId/units"
          element={<ListingUnits />}
        />

        <Route path="/fascilities" element={<ListingFascilities />} />
        <Route path="/unit-fascilities" element={<ListingUnitFacilties />} />
        <Route
          path="/units/:unitId/unit-fascilities"
          element={<AddUnitFacilityToUnit />}
        />

        <Route
          path="/properties/:propertyId/images"
          element={<PropertyImages />}
        />

        <Route path="/properties/:propertyId/add-unit" element={<AddUnit />} />

        <Route path="/units/:unitId" element={<DetailedBookableUnit />} />

        <Route
          path="/units/:unitId/add-price"
          element={<AddPeriodPriceUnit />}
        />

        <Route path="/units/:unitId/addons" element={<AddAddonToUnit />} />
        <Route
          path="/units/:unitId/addons/:addonId"
          element={<AddPeriodPriceAddon />}
        />

        <Route path="/addons" element={<AddAddon />} />

        <Route path="/profile" element={<UserProfile />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
