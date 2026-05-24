import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { useFetch } from "../hooks/useFetch";
import "./styles/DetailedBookableUnit.css";
import { useState } from "react";

const DetailedBookableUnit = () => {
  const { unitId } = useParams();

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const navigate = useNavigate();

  const [searchParams] = useSearchParams();

  const startDate = searchParams.get("startDate");
  const endDate = searchParams.get("endDate");

  const { data: unit, loading } = useFetch(
    unitId
      ? `http://localhost:8080/api/units/${unitId}?startDate=${startDate}&endDate=${endDate}`
      : null,
  );

  const { data: addons } = useFetch(
    unitId
      ? `http://localhost:8080/api/units/${unitId}/addons?startDate=${startDate}&endDate=${endDate}`
      : null,
  );

  const [chosenAddons, setChosenAddons] = useState([]);

  const getNumberOfNights = () => {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const diffMs = end - start;
    return Math.max(1, Math.round(diffMs / (1000 * 60 * 60 * 24)));
  };

  const calculateAddonsTotal = () => {
    if (!addons) return 0;
    const nights = getNumberOfNights();
    return chosenAddons.reduce((sum, id) => {
      const addon = addons.find((a) => a.addonID === id);
      if (!addon) return sum;
      return sum + (addon.perNight ? addon.price * nights : addon.price);
    }, 0);
  };

  const totalPrice = (unit?.totalPriceForStay || 0) + calculateAddonsTotal();

  const toggleAddon = (id) => {
    setChosenAddons((prev) =>
      prev.includes(id)
        ? prev.filter((addonId) => addonId !== id)
        : [...prev, id],
    );
  };

  const handleBooking = async (e) => {
    e.preventDefault();

    setIsLoading(true);

    const bookingRequestDTO = {
      start_date: startDate,
      end_date: endDate,
      addons: chosenAddons.map((id) => ({ id })),
    };

    console.log(chosenAddons);
    try {
      const res = await fetch(
        `http://localhost:8080/api/units/${unitId}/book`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${localStorage.getItem("jwtToken")}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify(bookingRequestDTO),
        },
      );

      const json = await res.json();

      if (!res.ok) {
        throw new Error(
          json.message || `Error ${res.status}: ${res.statusText}`,
        );
      }

      setChosenAddons([]);
      navigate("/profile");
    } catch (er) {
      setError(er.message);
    } finally {
      setIsLoading(false);
    }
  };

  if (loading || !unit) return <p>Loading...</p>;

  const {
    propertyDTO: property,
    name: unitName,
    maxCapacity,
    squareMeters,
    singleBeds,
    doubleBeds,
  } = unit;
  const { city, country, address, description, name: propertyName } = property;

  return (
    <div className="page-unit-wrapper">
      <div className="unit-container">
        <div className="unit-layout-container">
          <div className="unit-info-detail">{propertyName}</div>

          <div className="unit-info-detail">
            {city},{country} - {address}
          </div>

          <div className="unit-info-detail">
            <h2>About the property</h2>
            <p>{description}</p>
          </div>

          {addons?.map((addon) => {
            const isChosen = chosenAddons.includes(addon.addonID);
            return (
              <div className="grid-of-addons" key={addon.addonID}>
                <div className="grid-item">{addon.name}</div>
                <div className="grid-item">{addon.price}</div>
                <div className="grid-item">
                  {addon.perNight === true ? "Per night" : "One time"}
                </div>
                <div className="grid-item">
                  <button
                    onClick={() => toggleAddon(addon.addonID)}
                    style={{
                      backgroundColor: isChosen ? "green" : "",
                      color: isChosen ? "white" : "",
                    }}
                  >
                    {isChosen ? "Added" : "Add"}
                  </button>
                </div>
              </div>
            );
          })}

          <div className="unit-info-detail">
            <p className="total-price">Total Price: {totalPrice}</p>
            <button className="booking-btn" onClick={handleBooking}>
              Book Now
            </button>
          </div>

          {isLoading && (
            <div className="unit-info-detail">
              <p>Loading...</p>
            </div>
          )}
          {error && (
            <div className="unit-info-detail">
              <p>{error}</p>
            </div>
          )}

          <div className="grid-facillities"></div>
        </div>
      </div>
    </div>
  );
};

export default DetailedBookableUnit;
