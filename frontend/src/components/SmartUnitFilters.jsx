import { useEffect, useRef, useState } from "react";

const SmartUnitFilters = ({ apiURL, context, catalogReady, onApply }) => {
  const [message, setMessage] = useState("");
  const [isApplying, setIsApplying] = useState(false);
  const [feedback, setFeedback] = useState("");
  const [hasError, setHasError] = useState(false);
  const requestRef = useRef(null);
  const contextKey = JSON.stringify(context);

  // Changing the form or leaving the page cancels a stale AI interpretation.
  useEffect(() => () => requestRef.current?.abort(), [contextKey]);

  const applySmartFilters = async (event) => {
    event.preventDefault();
    if (!message.trim() || isApplying || !catalogReady) return;
    const controller = new AbortController();
    requestRef.current = controller;
    setIsApplying(true);
    setFeedback("");
    setHasError(false);
    try {
      const response = await fetch(`${apiURL}/api/ai/unit-search`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("jwtToken")}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ message: message.trim(), context }),
        signal: controller.signal,
      });
      const result = await response.json().catch(() => ({}));
      if (!response.ok) {
        const fallback = response.status === 401 || response.status === 403
          ? "Please sign in to use smart filters."
          : response.status === 503 ? "Smart filters are unavailable. You can still use the filters below."
            : "Smart filters couldn't apply this search. Please try again.";
        throw new Error(result.message || fallback);
      }
      if (controller.signal.aborted) return;
      if (result.filters) {
        onApply(result.filters);
        setFeedback("Smart filters applied. You can adjust the choices below.");
      } else {
        setFeedback(result.clarification || "Add a destination, dates and the amenities you want, then try again.");
      }
    } catch (error) {
      if (error.name !== "AbortError") {
        setHasError(true);
        setFeedback(error.message);
      }
    } finally {
      if (requestRef.current === controller) setIsApplying(false);
    }
  };

  return (
    <form className="smart-unit-filters" onSubmit={applySmartFilters} aria-busy={isApplying}>
      <label htmlFor="smart-unit-search">Smart filters</label>
      <p id="smart-unit-search-help">Describe your stay in English or Serbian. Your current search fills in anything you leave out.</p>
      <textarea id="smart-unit-search" rows={5} maxLength={2000} value={message}
        onChange={(event) => { requestRef.current?.abort(); setMessage(event.target.value); setFeedback(""); }}
        aria-describedby="smart-unit-search-help"
        placeholder="Find units in Belgrade, Serbia, 10–15 December 2026, for 2 adults with parking and Wi-Fi." />
      <button type="submit" disabled={isApplying || !message.trim() || !catalogReady}>
        {isApplying ? "Applying smart filters..." : "Apply smart filters"}
      </button>
      {!catalogReady && <p className="smart-filter-feedback">Load the available amenities before using smart filters.</p>}
      {feedback && <p className={`smart-filter-feedback${hasError ? " smart-filter-error" : ""}`}
        role={hasError ? "alert" : "status"}>{feedback}</p>}
    </form>
  );
};

export default SmartUnitFilters;
