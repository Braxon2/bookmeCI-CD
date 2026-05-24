import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";

const useFetch = (url) => {
  const { isAuthenticated } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchData = async () => {
    if (!isAuthenticated || !url) {
      setData(null);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const token = localStorage.getItem("jwtToken");

      const response = await fetch(url, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      if (!response.ok) throw new Error("Failed to fetch");
      const result = await response.json();

      setData(result);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [url, isAuthenticated]);

  return { data, loading, error };
};

export { useFetch };
