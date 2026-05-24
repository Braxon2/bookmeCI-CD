import { useState } from "react";

const useImageUpload = () => {
  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState(null);

  const uploadImage = async (url, file) => {
    setIsUploading(true);
    setUploadError(null);

    const formData = new FormData();
    formData.append("image", file);

    try {
      const token = localStorage.getItem("jwtToken");
      const res = await fetch(url, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
        body: formData,
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({
          message: "An unexpected error occurred",
        }));

        throw new Error(
          errorData.message || `Error ${res.status}: ${res.statusText}`,
        );
      }

      const rawText = await res.text();
      const cleanImageUrl = rawText.replace(/^"|"$/g, "");

      return cleanImageUrl;
    } catch (err) {
      console.error("Upload error:", err);
      setUploadError(err.message);
      return null;
    } finally {
      setIsUploading(false);
    }
  };

  return { uploadImage, isUploading, uploadError };
};

export default useImageUpload;
