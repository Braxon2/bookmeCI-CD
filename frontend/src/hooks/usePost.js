import { useState } from "react";

const usePost = () => {
  const [data, setData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const post = async (link, body) => {
    setIsLoading(true);
    setError(null);

    try {
      const res = await fetch(link, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("jwtToken")}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(body),
      });

      const json = await res.json().catch(() => ({
        message: "An unexpected error occurred",
      }));

      if (!res.ok) {
        throw new Error(
          json.message || `Error ${res.status}: ${res.statusText}`,
        );
      }

      setData(json);
      return json;
    } catch (err) {
      setError(err.message);
      return null;
    } finally {
      setIsLoading(false);
    }
  };

  return { data, isLoading, error, post };
};

export default usePost;
