import { useEffect, useRef, useState } from "react";
import "./styles/GuestDropdown.css";

const GuestDropdown = ({ adults, setAdults, kids, setKids }) => {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  const totalGuests = adults + kids;

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (ref.current && !ref.current.contains(event.target)) setOpen(false);
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div className="guests-dropdown" ref={ref}>
      <button
        className="guest-input"
        type="button"
        aria-haspopup="dialog"
        aria-expanded={open}
        onClick={() => setOpen((current) => !current)}
      >
        <span>{totalGuests} {totalGuests === 1 ? "guest" : "guests"}</span>
        <small>{adults} adults · {kids} children</small>
        <b aria-hidden="true">⌄</b>
      </button>

      {open && (
        <div className="guest-panel" role="dialog" aria-label="Choose guests">
          <div className="guest-menu">
            <div><strong>Adults</strong><small>Ages 18 or above</small></div>
            <div className="guest-stepper">
              <button type="button" aria-label="Remove one adult" disabled={adults <= 1}
                onClick={() => setAdults(Math.max(1, adults - 1))}>−</button>
              <span>{adults}</span>
              <button type="button" aria-label="Add one adult" onClick={() => setAdults(adults + 1)}>+</button>
            </div>
          </div>
          <div className="guest-menu">
            <div><strong>Children</strong><small>Ages 0–17</small></div>
            <div className="guest-stepper">
              <button type="button" aria-label="Remove one child" disabled={kids <= 0}
                onClick={() => setKids(Math.max(0, kids - 1))}>−</button>
              <span>{kids}</span>
              <button type="button" aria-label="Add one child" onClick={() => setKids(kids + 1)}>+</button>
            </div>
          </div>
          <button className="guest-done" type="button" onClick={() => setOpen(false)}>Done</button>
        </div>
      )}
    </div>
  );
};

export default GuestDropdown;
