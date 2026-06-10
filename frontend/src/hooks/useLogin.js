import { useState } from "react";

const useLogin = () => {
  const apiURL = import.meta.env.VITE_API_URL || "";
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const login = async (email, password) => {
    setIsLoading(true);
    setError(null);

    try {
      const res = await fetch(`${apiURL}/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
      });

      const json = await res.json();

      if (res.ok) {
        setData(json);
        setError(null);
      } else {
        setError(json.error || "Login failed");
        setData(null);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return { data, isLoading, error, login };
};

export { useLogin };
