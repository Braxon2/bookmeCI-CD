import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import notFoundImage from "../assets/images/Image-not-found.png";
import { useFetch } from "../hooks/useFetch";
import useImageUpload from "../hooks/useImageUpload";
import "./styles/ImageManager.css";

const ImageManager = ({ title, description, endpoint, entityLabel }) => {
  const apiURL = import.meta.env.VITE_API_URL || "";
  const navigate = useNavigate();
  const inputRef = useRef(null);
  const { data, loading, error: fetchError } = useFetch(`${apiURL}${endpoint}`);
  const { uploadImage, isUploading, uploadError } = useImageUpload();
  const [file, setFile] = useState(null);
  const [uploadedImages, setUploadedImages] = useState([]);
  const previewUrl = useMemo(() => file ? URL.createObjectURL(file) : null, [file]);
  useEffect(() => () => { if (previewUrl) URL.revokeObjectURL(previewUrl); }, [previewUrl]);
  const images = [...(data || []), ...uploadedImages]
    .filter((image) => image?.url)
    .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0));

  const selectFile = (selectedFile) => {
    if (selectedFile?.type.startsWith("image/")) setFile(selectedFile);
  };

  const handleUpload = async (event) => {
    event.preventDefault();
    if (!file) return;
    const uploaded = await uploadImage(`${apiURL}${endpoint}`, file);
    if (uploaded?.url) {
      setUploadedImages((current) => [...current, uploaded]);
      setFile(null);
      if (inputRef.current) inputRef.current.value = "";
    }
  };

  return (
    <main className="image-manager-page">
      <div className="image-manager-shell">
        <button className="image-manager-back" type="button" onClick={() => navigate(-1)}>← Back</button>
        <header className="image-manager-header"><div><span>Owner workspace</span><h1>{title}</h1><p>{description}</p></div>
          <strong>{images.length}<small>{images.length === 1 ? "photo" : "photos"}</small></strong></header>

        <section className="image-upload-card">
          <div className="image-upload-copy"><span>Add a photo</span><h2>Upload clear, inviting images</h2><p>Use JPG, PNG, or WebP images up to 10 MB. Landscape photos usually look best in search results.</p></div>
          <form onSubmit={handleUpload}>
            <label className={`image-drop-zone ${previewUrl ? "has-preview" : ""}`}
              onDragOver={(e) => e.preventDefault()} onDrop={(e) => { e.preventDefault(); selectFile(e.dataTransfer.files[0]); }}>
              {previewUrl ? <img src={previewUrl} alt="Selected upload preview" /> : <><span aria-hidden="true">↑</span><strong>Choose or drop an image</strong><small>JPG, PNG or WebP · maximum 10 MB</small></>}
              <input ref={inputRef} type="file" accept="image/jpeg,image/png,image/webp" onChange={(e) => selectFile(e.target.files[0])} />
            </label>
            <div className="image-upload-actions">
              <div>{file ? <><strong>{file.name}</strong><small>{(file.size / 1024 / 1024).toFixed(1)} MB</small></> : <span>Select an image to continue</span>}</div>
              {file && <button className="image-clear-button" type="button" onClick={() => { setFile(null); inputRef.current.value = ""; }}>Clear</button>}
              <button className="image-upload-button" type="submit" disabled={!file || isUploading}>{isUploading ? "Uploading..." : "Upload photo"}</button>
            </div>
            {uploadError && <p className="image-manager-error" role="alert">{uploadError}</p>}
          </form>
        </section>

        <section className="image-gallery-card">
          <div className="image-gallery-heading"><div><span>Gallery</span><h2>{entityLabel} photos</h2></div><p>The first photo is used as the primary image.</p></div>
          {loading && <div className="image-manager-state"><span /><p>Loading photos...</p></div>}
          {!loading && fetchError && <div className="image-manager-state"><h3>Couldn&apos;t load photos</h3><p>{fetchError}</p></div>}
          {!loading && !fetchError && images.length === 0 && <div className="image-manager-state"><h3>No photos yet</h3><p>Upload the first image to make this {entityLabel.toLowerCase()} easier to discover.</p></div>}
          {!loading && images.length > 0 && <div className="managed-image-grid">
            {images.map((image, index) => <figure key={image.id ?? `${image.url}-${index}`}>
              <img src={image.url || notFoundImage} alt={`${entityLabel} photo ${index + 1}`} onError={(e) => { e.currentTarget.src = notFoundImage; }} />
              <figcaption><span>Photo {index + 1}</span>{(image.primary || index === 0) && <strong>Primary</strong>}</figcaption>
            </figure>)}
          </div>}
        </section>
      </div>
    </main>
  );
};

export default ImageManager;
