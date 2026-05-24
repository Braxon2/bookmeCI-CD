import { useEffect, useRef, useState } from "react";
import notFoundImage from "../assets/images/Image-not-found.png";
import { useFetch } from "../hooks/useFetch";
import usePost from "../hooks/usePost";
import "./styles/PropertyImages.css";
import { useParams } from "react-router-dom";
import useImageUpload from "../hooks/useImageUpload";
const PropertyImages = () => {
  const { propertyId } = useParams();
  const fileInputRef = useRef(null);

  const {
    data,
    loading: fetchLoading,
    error: fetchError,
  } = useFetch(`http://localhost:8080/api/properties/${propertyId}/images`);

  const [images, setImages] = useState([]);
  const [imageFile, setImageFile] = useState(null);

  const { uploadImage, isUploading, uploadError } = useImageUpload();

  useEffect(() => {
    if (data) {
      setImages(data);
    }
  }, [data]);

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!imageFile) return;

    const url = `http://localhost:8080/api/properties/${propertyId}/images`;
    const newImageUrl = await uploadImage(url, imageFile);

    if (newImageUrl) {
      setImages((prev) => [...prev, newImageUrl]);
      setImageFile(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    }
  };

  if (fetchLoading && images.length === 0) return <p>Loading images...</p>;
  if (fetchError) return <p>Error loading images: {fetchError}</p>;

  return (
    <div className="image-page">
      <div className="image-list">
        <div className="input-filed">
          <input
            type="file"
            name="imageupload"
            id="imageupload"
            onChange={(e) => setImageFile(e.target.files[0])}
            accept="image/*"
          />
          <button onClick={handleUpload} disabled={isUploading || !imageFile}>
            {isUploading ? "Uploading..." : "Upload"}
          </button>
        </div>
        {uploadError && (
          <p className="error-text" style={{ color: "red" }}>
            {uploadError}
          </p>
        )}
        <div className="grid-images">
          {images?.map((image, index) => (
            <div className="image-item" key={index}>
              <img
                src={image || notFoundImage}
                alt={`Property image ${index + 1}`}
              />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default PropertyImages;
