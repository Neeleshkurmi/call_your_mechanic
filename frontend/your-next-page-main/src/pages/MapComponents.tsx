import { useRef, useEffect, useState, createContext, useContext } from "react";

const MapContext = createContext<google.maps.Map | null>(null);

interface MapProps {
  center: google.maps.LatLngLiteral;
  zoom: number;
  children?: React.ReactNode;
}

export const Map: React.FC<MapProps> = ({ center, zoom, children }) => {
  const ref = useRef<HTMLDivElement>(null);
  const mapRef = useRef<google.maps.Map | null>(null);
  const [map, setMap] = useState<google.maps.Map | null>(null);

  useEffect(() => {
    if (ref.current && !mapRef.current) {
      mapRef.current = new google.maps.Map(ref.current, {
        center,
        zoom,
        disableDefaultUI: true,
        zoomControl: true,
      });
      setMap(mapRef.current);
    }
  }, [center, zoom]);

  useEffect(() => {
    if (mapRef.current) {
      mapRef.current.setCenter(center);
    }
  }, [center]);

  return (
    <MapContext.Provider value={map}>
      <div ref={ref} className="w-full h-64 rounded-lg" />
      {children}
    </MapContext.Provider>
  );
};

interface MarkerProps {
  position: google.maps.LatLngLiteral;
  title?: string;
}

interface DirectionsProps {
  origin: google.maps.LatLngLiteral;
  destination: google.maps.LatLngLiteral;
}

export const Marker: React.FC<MarkerProps> = ({ position, title }) => {
  const map = useContext(MapContext);
  const markerRef = useRef<google.maps.Marker | null>(null);

  useEffect(() => {
    if (map && !markerRef.current) {
      markerRef.current = new google.maps.Marker({
        position,
        map,
        title,
      });
    } else if (markerRef.current) {
      markerRef.current.setPosition(position);
      markerRef.current.setMap(map);
    }

    return () => {
      if (markerRef.current) {
        markerRef.current.setMap(null);
      }
    };
  }, [map, position, title]);

  return null;
};

export const DirectionsRoute: React.FC<DirectionsProps> = ({ origin, destination }) => {
  const map = useContext(MapContext);
  const rendererRef = useRef<google.maps.DirectionsRenderer | null>(null);

  useEffect(() => {
    if (!map) return;

    if (!rendererRef.current) {
      rendererRef.current = new google.maps.DirectionsRenderer({
        suppressMarkers: true,
        polylineOptions: {
          strokeColor: "#2563eb",
          strokeOpacity: 0.85,
          strokeWeight: 5,
        },
      });
    }

    rendererRef.current.setMap(map);

    const directionsService = new google.maps.DirectionsService();
    directionsService.route(
      {
        origin,
        destination,
        travelMode: google.maps.TravelMode.DRIVING,
      },
      (result, status) => {
        if (status === google.maps.DirectionsStatus.OK && result && rendererRef.current) {
          rendererRef.current.setDirections(result);
        }
      }
    );

    const bounds = new google.maps.LatLngBounds();
    bounds.extend(origin);
    bounds.extend(destination);
    map.fitBounds(bounds, 64);

    return () => {
      if (rendererRef.current) {
        rendererRef.current.setMap(null);
      }
    };
  }, [map, origin, destination]);

  return null;
};
