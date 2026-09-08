import { useParams } from "react-router-dom";
import ImageManager from "../components/ImageManager";
import { useFetch } from "../hooks/useFetch";

const PropertyImages = () => {
  const apiURL = import.meta.env.VITE_API_URL || "";
  const { publicId } = useParams();
  const { data: property } = useFetch(`${apiURL}/api/properties/${publicId}`);
  return <ImageManager title={property?.name ? `${property.name} photos` : "Property photos"}
    description="Show guests the exterior, shared spaces, location highlights, and overall atmosphere."
    endpoint={`/api/properties/${publicId}/images`} entityLabel="Property" />;
};

export default PropertyImages;
