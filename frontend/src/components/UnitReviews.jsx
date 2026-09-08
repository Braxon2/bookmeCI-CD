import { useEffect, useState } from "react";

const UnitReviews = ({ unitId, propertyId, unitName }) => {
  const apiURL = import.meta.env.VITE_API_URL || "";
  const [reviews, setReviews] = useState([]);
  const [source, setSource] = useState("unit");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!unitId || !propertyId) return undefined;
    const controller = new AbortController();
    const headers = { Authorization: `Bearer ${localStorage.getItem("jwtToken")}` };

    const getPage = async (url) => {
      const response = await fetch(url, { headers, signal: controller.signal });
      if (!response.ok) throw new Error("Guest reviews are unavailable right now.");
      return response.json();
    };

    const loadReviews = async () => {
      setLoading(true);
      setError("");
      try {
        const unitPage = await getPage(`${apiURL}/api/reviews/units/${unitId}/reviews?size=6`);
        if (unitPage.content?.length) {
          setReviews(unitPage.content);
          setSource("unit");
        } else {
          const propertyPage = await getPage(`${apiURL}/api/reviews/properties/${propertyId}/reviews?size=6`);
          setReviews(propertyPage.content || []);
          setSource("property");
        }
      } catch (loadError) {
        if (loadError.name !== "AbortError") setError(loadError.message);
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    };

    loadReviews();
    return () => controller.abort();
  }, [apiURL, propertyId, unitId]);

  const average = reviews.length
    ? reviews.reduce((sum, review) => sum + review.rating, 0) / reviews.length
    : 0;

  return (
    <section className="unit-reviews-section" aria-labelledby="unit-reviews-title">
      <div className="unit-reviews-heading">
        <div><span>Guest feedback</span><h2 id="unit-reviews-title">Reviews</h2></div>
        {reviews.length > 0 && <div className="unit-review-score"><strong>{average.toFixed(1)}</strong><span>★</span><small>{reviews.length} {reviews.length === 1 ? "review" : "reviews"}</small></div>}
      </div>

      {source === "property" && reviews.length > 0 && <p className="unit-review-fallback">
        There are no reviews for {unitName} yet, so we&apos;re showing recent reviews from other units at this property.
      </p>}
      {loading && <div className="unit-reviews-state"><span className="unit-loading-spinner" /><p>Loading guest reviews...</p></div>}
      {!loading && error && <div className="unit-reviews-state"><p>{error}</p></div>}
      {!loading && !error && reviews.length === 0 && <div className="unit-reviews-state"><h3>No reviews yet</h3><p>Be the first guest to share an experience after a completed stay.</p></div>}
      {!loading && !error && reviews.length > 0 && <div className="unit-reviews-grid">
        {reviews.map((review) => {
          const firstName = review.reviewer?.firstName || "Guest";
          const lastName = review.reviewer?.lastName || "";
          const initials = `${firstName[0] || "G"}${lastName[0] || ""}`;
          return <article key={review.publicId} className="unit-review-card">
            <header><span className="unit-review-avatar">{initials}</span><div><strong>{firstName} {lastName}</strong>
              <small>{new Date(review.createdAt).toLocaleDateString("en-GB", { month: "short", year: "numeric" })}</small></div>
              <span className="unit-review-stars" aria-label={`${review.rating} out of 5 stars`}>{"★".repeat(review.rating)}<i>{"★".repeat(5 - review.rating)}</i></span></header>
            {source === "property" && <span className="unit-review-unit-name">Stayed in {review.bookableUnitName}</span>}
            <p>{review.text}</p>
          </article>;
        })}
      </div>}
    </section>
  );
};

export default UnitReviews;
