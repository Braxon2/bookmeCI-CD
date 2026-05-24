import { useEffect, useRef, useState } from "react";
import "./styles/GuestDropdown.css";
const GuestDropdown = ({ adults, setAdults, kids, setKids }) => {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  const label = `Adults ${adults} · Kids ${kids}`;

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (ref.current && !ref.current.contains(e.target)) {
        setOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div className="guests-dropdown" ref={ref}>
      <div className="guest-input" onClick={() => setOpen(!open)}>
        <span>{label}</span>
      </div>
      {open && (
        <div className="guest-panel">
          <div className="guest-menu">
            <span>Adults: </span>
            <button
              className="circle-button"
              type="button"
              onClick={() => (adults > 1 ? setAdults(adults - 1) : adults)}
            >
              -
            </button>
            <button
              className="circle-button"
              type="button"
              onClick={() => setAdults(adults + 1)}
            >
              +
            </button>
          </div>
          <div className="guest-menu">
            <span>Kids: </span>
            <button
              className="circle-button"
              type="button"
              onClick={() => (kids > 0 ? setKids(kids - 1) : kids)}
            >
              -
            </button>
            <button
              className="circle-button"
              type="button"
              onClick={() => (kids >= 0 ? setKids(kids + 1) : kids)}
              kids
            >
              +
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default GuestDropdown;
