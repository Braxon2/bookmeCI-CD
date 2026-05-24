import "./styles/UserProfile.css";
import avatar from "../assets/images/avatar.png";
import { useFetch } from "../hooks/useFetch";
import { useAuth } from "../context/AuthContext";
import { useEffect, useState } from "react";

const UserProfile = () => {
  const { user } = useAuth();

  const [isOpen, setIsOpen] = useState(false);

  const [usersBookings, setUsersBookings] = useState([]);

  const [bookingID, setBookingID] = useState(null);

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const { data: userInfo, loading: userLoading } = useFetch(
    user ? `http://localhost:8080/api/users/${user.id}` : null,
  );

  const { data: bookings, loading: bookingsLoading } = useFetch(
    user ? `http://localhost:8080/api/users/${user.id}/bookings` : null,
  );

  useEffect(() => {
    if (bookings) {
      setUsersBookings(bookings);
    }
  }, [bookings]);

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  if (!user) {
    return <div className="page-wrapper">Loading authentication...</div>;
  }

  if (userLoading || !userInfo) {
    return <div className="page-wrapper">Loading profile data...</div>;
  }

  const handleCancelEvent = async () => {
    setIsLoading(true);
    try {
      const res = await fetch(
        `http://localhost:8080/api/bookings/${bookingID}`,
        {
          method: "PATCH",
          headers: {
            Authorization: `Bearer ${localStorage.getItem("jwtToken")}`,
            "Content-Type": "application/json",
          },
        },
      );

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({
          message: "An unexpected error occurred",
        }));
        throw new Error(
          errorData.message || `Error ${res.status}: ${res.statusText}`,
        );
      }

      const updatedBooking = await res.json();

      setUsersBookings((prevBookings) =>
        prevBookings.map((booking) =>
          booking.id === bookingID
            ? { ...booking, status: updatedBooking.status }
            : booking,
        ),
      );

      setIsOpen(false);
      setBookingID(null);
    } catch (er) {
      setError(er.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleClick = (id) => {
    setIsOpen(true);
    setBookingID(id);
  };

  return (
    <div className="page-wrapper">
      {isOpen && (
        <div className="dialog-overlay">
          <div className="message-dialog">
            <p>Are you sure you want to cancel reservation?</p>
            <div className="dialog-actions">
              <button
                className="cancel"
                onClick={handleCancelEvent}
                disabled={isLoading}
              >
                {isLoading ? "Cancelling..." : "Yes"}
              </button>
              <button
                className="no-cancel"
                onClick={() => {
                  setIsOpen(false);
                  setBookingID(null);
                }}
              >
                No
              </button>
            </div>
            {error && <p style={{ color: "red", fontSize: "12px" }}>{error}</p>}
          </div>
        </div>
      )}

      <div className="layout-profile-container">
        <div className="profile-container">
          <div className="avatar">
            <img src={avatar} alt="avatar-image" />
          </div>
          <div className="user-info">
            <h2>User Profile</h2>
            <div className="input-group">
              <div className="input-field">
                <label>First name</label>
                <input type="text" readOnly value={userInfo.firstName} />
              </div>
              <div className="input-field">
                <label>Last name</label>
                <input type="text" readOnly value={userInfo.lastName} />
              </div>
            </div>
            <div className="input-field">
              <label>Email</label>
              <input type="text" readOnly value={userInfo.email} />
            </div>
            <div className="input-field">
              <label>Phone number</label>
              <input type="text" readOnly value={userInfo.phoneNumber} />
            </div>
          </div>
        </div>

        <hr className="divider" />

        <div className="bookings-section">
          <h2>My Bookings</h2>
          {bookingsLoading ? (
            <p>Loading bookings...</p>
          ) : usersBookings && usersBookings.length > 0 ? (
            <div className="bookings-list">
              {usersBookings.map((booking) => (
                <div className="booking-card" key={booking.id}>
                  <div className="booking-header">
                    <h3>{booking.bookableUnit.name}</h3>
                    <span
                      className={`status-badge ${booking.status.toLowerCase()}`}
                    >
                      {booking.status}
                    </span>
                  </div>

                  <div className="booking-details">
                    <div className="detail-item">
                      <span className="label">Check-in:</span>
                      <span className="value">
                        {formatDate(booking.checkIn)}
                      </span>
                    </div>
                    <div className="detail-item">
                      <span className="label">Check-out:</span>
                      <span className="value">
                        {formatDate(booking.checkOut)}
                      </span>
                    </div>
                    <div className="detail-item">
                      <span className="label">Booked On:</span>
                      <span className="value">
                        {formatDate(booking.createdAt)}
                      </span>
                    </div>
                    <div className="detail-item price-item">
                      <span className="label">Total Price:</span>
                      <span className="value price">
                        €{booking.totalPrice.toFixed(2)}
                      </span>
                    </div>
                    {booking.status === "CONFIRMED" && (
                      <div className="detail-item price-item">
                        <span
                          className="label"
                          onClick={() => handleClick(booking.id)}
                        >
                          Cancel reservation:
                        </span>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="no-bookings">You have no bookings yet.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default UserProfile;
