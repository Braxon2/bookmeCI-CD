import { useState } from "react";
import avatar from "../assets/images/avatar.png";
import { useAuth } from "../context/AuthContext";
import { useFetch } from "../hooks/useFetch";
import "./styles/UserProfile.css";

const formatDate = (value) => value
  ? new Date(value).toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" })
  : "N/A";
const formatPrice = (value) => new Intl.NumberFormat("en-GB", {
  style: "currency", currency: "EUR", minimumFractionDigits: 2,
}).format(Number(value) || 0);

const ReviewDialog = ({ booking, onClose, onSubmitted }) => {
  const apiURL = import.meta.env.VITE_API_URL || "";
  const [rating, setRating] = useState(0);
  const [hoveredRating, setHoveredRating] = useState(0);
  const [text, setText] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  const submitReview = async (event) => {
    event.preventDefault();
    if (!rating) { setError("Choose a rating from 1 to 5 stars."); return; }
    if (!text.trim()) { setError("Tell other guests about your stay."); return; }

    setIsSubmitting(true);
    setError("");
    try {
      const response = await fetch(`${apiURL}/api/reviews/bookings/${booking.id}/reviews`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("jwtToken")}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ rating, text: text.trim() }),
      });
      const result = await response.json().catch(() => ({}));
      if (!response.ok) throw new Error(result.message || "Your review could not be submitted.");
      onSubmitted(booking.id);
    } catch (submitError) {
      setError(submitError.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="profile-modal-overlay" role="presentation" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <section className="profile-review-dialog" role="dialog" aria-modal="true" aria-labelledby="review-title">
        <button className="profile-dialog-close" type="button" onClick={onClose} aria-label="Close review dialog">×</button>
        <span className="profile-dialog-eyebrow">Share your experience</span>
        <h2 id="review-title">Review {booking.bookableUnit.name}</h2>
        <p>Your feedback helps future guests choose the right stay.</p>
        <form onSubmit={submitReview}>
          <fieldset className="review-rating-field">
            <legend>Your rating</legend>
            <div className="review-stars" onMouseLeave={() => setHoveredRating(0)}>
              {[1, 2, 3, 4, 5].map((star) => (
                <button key={star} type="button" aria-label={`${star} ${star === 1 ? "star" : "stars"}`}
                  aria-pressed={rating === star} onMouseEnter={() => setHoveredRating(star)}
                  onClick={() => setRating(star)} className={star <= (hoveredRating || rating) ? "is-active" : ""}>★</button>
              ))}
            </div>
            <span>{rating ? `${rating} out of 5` : "Select a rating"}</span>
          </fieldset>
          <label className="review-text-field"><span>Your review</span>
            <textarea maxLength="3000" rows="5" value={text} onChange={(e) => setText(e.target.value)}
              placeholder="What did you enjoy? What should future guests know?" />
            <small>{text.length} / 3000</small>
          </label>
          {error && <p className="profile-dialog-error" role="alert">{error}</p>}
          <div className="profile-dialog-actions">
            <button type="button" className="is-secondary" onClick={onClose}>Not now</button>
            <button type="submit" disabled={isSubmitting}>{isSubmitting ? "Posting review..." : "Post review"}</button>
          </div>
        </form>
      </section>
    </div>
  );
};

const UserProfile = () => {
  const apiURL = import.meta.env.VITE_API_URL || "";
  const { user } = useAuth();
  const [bookingUpdates, setBookingUpdates] = useState({});
  const [bookingToCancel, setBookingToCancel] = useState(null);
  const [bookingToReview, setBookingToReview] = useState(null);
  const [reviewedBookings, setReviewedBookings] = useState([]);
  const [isCancelling, setIsCancelling] = useState(false);
  const [cancelError, setCancelError] = useState("");

  const { data: userInfo, loading: userLoading } = useFetch(user ? `${apiURL}/api/users/${user.id}` : null);
  const { data: bookings, loading: bookingsLoading } = useFetch(user ? `${apiURL}/api/users/${user.id}/bookings` : null);

  const usersBookings = (bookings || []).map((booking) => ({
    ...booking,
    ...(bookingUpdates[booking.id] || {}),
  }));

  const cancelBooking = async () => {
    setIsCancelling(true);
    setCancelError("");
    try {
      const response = await fetch(`${apiURL}/api/bookings/${bookingToCancel.id}`, {
        method: "PATCH",
        headers: { Authorization: `Bearer ${localStorage.getItem("jwtToken")}` },
      });
      const result = await response.json().catch(() => ({}));
      if (!response.ok) throw new Error(result.message || "The reservation could not be cancelled.");
      setBookingUpdates((current) => ({
        ...current,
        [bookingToCancel.id]: { status: result.status },
      }));
      setBookingToCancel(null);
    } catch (error) {
      setCancelError(error.message);
    } finally {
      setIsCancelling(false);
    }
  };

  if (!user || userLoading || !userInfo) {
    return <main className="profile-page"><div className="profile-state"><span /><p>Loading your profile...</p></div></main>;
  }

  return (
    <main className="profile-page">
      {bookingToReview && <ReviewDialog booking={bookingToReview} onClose={() => setBookingToReview(null)}
        onSubmitted={(id) => { setReviewedBookings((current) => [...current, id]); setBookingToReview(null); }} />}
      {bookingToCancel && <div className="profile-modal-overlay" role="presentation">
        <section className="profile-confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="cancel-title">
          <span className="profile-dialog-eyebrow">Cancel reservation</span>
          <h2 id="cancel-title">Are you sure?</h2>
          <p>This will cancel your reservation for <strong>{bookingToCancel.bookableUnit.name}</strong>.</p>
          {cancelError && <p className="profile-dialog-error" role="alert">{cancelError}</p>}
          <div className="profile-dialog-actions"><button type="button" className="is-secondary" onClick={() => setBookingToCancel(null)}>Keep booking</button>
            <button type="button" className="is-danger" onClick={cancelBooking} disabled={isCancelling}>{isCancelling ? "Cancelling..." : "Cancel booking"}</button></div>
        </section>
      </div>}

      <div className="profile-shell">
        <header className="profile-header"><div><span>Your account</span><h1>Trips and profile</h1><p>Review upcoming stays and share feedback from completed trips.</p></div></header>
        <section className="profile-summary-card">
          <img src={avatar} alt="Profile avatar" />
          <div className="profile-identity"><span>Guest</span><h2>{userInfo.firstName} {userInfo.lastName}</h2><p>{userInfo.email}</p></div>
          <dl><div><dt>Phone</dt><dd>{userInfo.phoneNumber || "Not provided"}</dd></div><div><dt>Total bookings</dt><dd>{usersBookings.length}</dd></div></dl>
        </section>

        <section className="profile-bookings-section">
          <div className="profile-section-heading"><div><span>Your trips</span><h2>Reservations</h2></div><strong>{usersBookings.length}</strong></div>
          {bookingsLoading && <div className="profile-state"><span /><p>Loading reservations...</p></div>}
          {!bookingsLoading && usersBookings.length === 0 && <div className="profile-empty"><h3>No reservations yet</h3><p>Your future trips will appear here after you book a stay.</p></div>}
          {!bookingsLoading && usersBookings.length > 0 && <div className="profile-booking-list">
            {usersBookings.map((booking) => {
              const reviewed = reviewedBookings.includes(booking.id);
              return <article className="profile-booking-card" key={booking.id}>
                <header><div><span>Reservation</span><h3>{booking.bookableUnit.name}</h3></div>
                  <span className={`profile-status is-${booking.status.toLowerCase()}`}>{booking.status}</span></header>
                <div className="profile-booking-details">
                  <div><span>Check-in</span><strong>{formatDate(booking.checkIn)}</strong></div>
                  <div><span>Check-out</span><strong>{formatDate(booking.checkOut)}</strong></div>
                  <div><span>Booked</span><strong>{formatDate(booking.createdAt)}</strong></div>
                  <div className="is-price"><span>Total</span><strong>{formatPrice(booking.totalPrice)}</strong></div>
                </div>
                {(booking.status === "CONFIRMED" || booking.status === "COMPLETED") && <footer>
                  {booking.status === "CONFIRMED" && <button className="profile-cancel-button" type="button" onClick={() => setBookingToCancel(booking)}>Cancel reservation</button>}
                  {booking.status === "COMPLETED" && <button className="profile-review-button" type="button" disabled={reviewed}
                    onClick={() => setBookingToReview(booking)}>{reviewed ? "Review submitted ✓" : "Leave a review"}</button>}
                </footer>}
              </article>;
            })}
          </div>}
        </section>
      </div>
    </main>
  );
};

export default UserProfile;
