import { useState } from "react";
import { useAuth } from "../context/AuthContext";

const usePostProperty = () => {
  const [data, setData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const { user } = useAuth();

  const postProperty = async (link, propertyForCreation) => {
    setIsLoading(true);
    setError(null);

    try {
      const res = await fetch(link, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("jwtToken")}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(propertyForCreation),
      });

      const json = await res.json();

      if (!res.ok) {
        throw new Error(
          json.message || `Error ${res.status}: ${res.statusText}`,
        );
      }

      setData(json);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return { data, isLoading, error, postProperty };
};

export default usePostProperty;
