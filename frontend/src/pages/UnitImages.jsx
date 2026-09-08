import { useParams } from "react-router-dom";
import ImageManager from "../components/ImageManager";
import { useFetch } from "../hooks/useFetch";

const UnitImages = () => {
  const apiURL = import.meta.env.VITE_API_URL || "";
  const { unitId } = useParams();
  const { data: unit } = useFetch(`${apiURL}/api/units/${unitId}/info`);
  return <ImageManager title={unit?.name ? `${unit.name} photos` : "Unit photos"}
    description="Help guests understand the sleeping space, layout, bathroom, views, and in-room amenities."
    endpoint={`/api/units/${unitId}/images`} entityLabel="Unit" />;
};

export default UnitImages;
