import { Calendar, DatePickerInput } from "@mantine/dates";
import { useEffect, useState } from "react";
import "./styles/AddPeriodPriceUnit.css";
import usePost from "../hooks/usePost";
import { useParams } from "react-router-dom";
import { useFetch } from "../hooks/useFetch";
import dayjs from "dayjs";
import { Text, Button } from "@mantine/core";
import { useToggle } from "@mantine/hooks";

const AddPeriodPriceAddon = () => {
  const { unitId, addonId } = useParams();

  const [value, setValue] = useState([null, null]);
  const [price, setPrice] = useState(0.0);

  const [perNight, togglePerNight] = useToggle([false, true]);

  const [isToggling, setIsToggling] = useState(false);

  const { data: priceDates } = useFetch(
    unitId
      ? `http://localhost:8080/api/units/${unitId}/addons/${addonId}/period-prices`
      : null,
  );

  const { data, post } = usePost();

  const periodPriceToAdd = {
    price,
    startDate: value[0],
    endDate: value[1],
  };

  const handleDateChange = (dates) => {
    setValue(dates);
  };

  const handleSubmit = async () => {
    await post(
      `http://localhost:8080/api/units/${unitId}/addons/${addonId}/add-price`,
      periodPriceToAdd,
    );
  };

  const handleTogglePerNight = async () => {
    setIsToggling(true);
    const nextValue = !perNight;

    try {
      const res = await fetch(
        `http://localhost:8080/api/units/${unitId}/addons/${addonId}/billing-type`,
        {
          method: "PATCH",
          headers: {
            Authorization: `Bearer ${localStorage.getItem("jwtToken")}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            isPerNight: nextValue,
          }),
        },
      );

      if (res.ok) {
        togglePerNight();
      } else {
        console.error("Failed to update billing type");
      }
    } catch (error) {
      console.error("Error updating billing type:", error);
    } finally {
      setIsToggling(false);
    }
  };

  useEffect(() => {
    if (priceDates && priceDates.length > 0) {
      togglePerNight(Boolean(priceDates[0].isPerNight));
    } else if (priceDates && priceDates.length === 0) {
      togglePerNight(false);
    }
  }, [priceDates, togglePerNight]);

  return (
    <div className="page-prices">
      <div className="price-layout">
        <DatePickerInput
          valueFormat="YYYY MMMM DD"
          type="range"
          placeholder="Choose a date range"
          value={value}
          minDate={new Date()}
          onChange={handleDateChange}
        />

        <div
          className="billing-toggle-container"
          style={{ margin: "10px 0", display: "flex", alignItems: "center" }}
        >
          <label style={{ marginRight: "10px", fontWeight: "bold" }}>
            Billing Type:
          </label>
          <Button
            onClick={handleTogglePerNight}
            loading={isToggling}
            color={perNight ? "green" : "blue"}
            variant="filled"
          >
            {perNight ? "Per Night" : "Per Stay / Flat Rate"}
          </Button>
        </div>

        <label htmlFor="price">
          Price:
          <input
            type="number"
            step={0.1}
            value={price}
            id="price"
            name="price"
            onChange={(e) => setPrice(parseFloat(e.target.value))}
          />
        </label>

        <button onClick={handleSubmit}>Add price</button>

        <div className="available-prices">
          <h2>Available prices</h2>
          <Calendar
            static
            renderDay={(date) => {
              const currentDate = dayjs(date);

              const priceForDate = priceDates?.findLast((p) => {
                return (
                  (currentDate.isAfter(p.startDate) ||
                    currentDate.isSame(p.startDate)) &&
                  (currentDate.isBefore(p.endDate) ||
                    currentDate.isSame(p.endDate))
                );
              });

              return (
                <div>
                  <div>{dayjs(date).date()}</div>
                  {priceForDate && (
                    <Text
                      size="xs"
                      c="blue"
                      fw={"700"}
                      style={{ fontSize: "8px" }}
                    >
                      ${priceForDate.pricePerNight || priceForDate.price}
                    </Text>
                  )}
                </div>
              );
            }}
          />
        </div>
      </div>
    </div>
  );
};

export default AddPeriodPriceAddon;
