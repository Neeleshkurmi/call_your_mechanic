import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Topbar from "@/components/Topbar";
import BottomNav from "@/components/BottomNav";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import { apiGet, apiSend, logout } from "@/lib/api";
import { getBookingDraft, saveBookingDraft } from "@/lib/auth";
import { withGeolocation, readApiError, reverseGeocodeLocation } from "@/lib/helpers";

interface Service { id: number; name: string; description: string; vehicleType: string; }
interface Vehicle { id: number; brand: string; model: string; registrationNumber: string; vehicleType: string; }

const UserHome = () => {
  const navigate = useNavigate();
  const googleMapsApiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;
  const [services, setServices] = useState<Service[]>([]);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [selectedServiceId, setSelectedServiceId] = useState<number | null>(null);
  const [selectedVehicleId, setSelectedVehicleId] = useState<string>("");
  const [location, setLocation] = useState<{ lat: number; lng: number } | null>(null);
  const [locationLabel, setLocationLabel] = useState("Location name not available yet");

  useEffect(() => {
    const draft = getBookingDraft();
    if (draft?.serviceId) setSelectedServiceId(draft.serviceId);
    if (draft?.vehicleId) setSelectedVehicleId(String(draft.vehicleId));
    if (draft?.location) {
      setLocation(draft.location);
      setLocationLabel(draft.locationName || "Resolving location name...");
      if (!draft.locationName) {
        reverseGeocodeLocation(draft.location, googleMapsApiKey).then((name) => setLocationLabel(name));
      }
    }

    Promise.all([
      apiGet("/api/v1/services", { auth: false }),
      apiGet("/api/v1/vehicles"),
      apiGet("/api/v1/users/me/location/latest").catch(() => ({ data: null })),
    ]).then(([svc, veh, loc]) => {
      setServices(svc.data);
      setVehicles(veh.data);
      if (!draft?.location && loc.data) {
        const l = { lat: loc.data.latitude, lng: loc.data.longitude };
        setLocation(l);
        setLocationLabel("Resolving location name...");
        reverseGeocodeLocation(l, googleMapsApiKey).then((name) => {
          setLocationLabel(name);
          saveBookingDraft({ ...(getBookingDraft() || {}), location: l, locationName: name });
        });
      }
    }).catch((err) => toast.error(readApiError(err)));
  }, []);

  const handleSetLocation = async () => {
    try {
      const loc = await withGeolocation();
      await apiSend("/api/v1/users/me/location", "POST", { latitude: loc.lat, longitude: loc.lng });
      setLocation(loc);
      setLocationLabel("Resolving location name...");
      const locationName = await reverseGeocodeLocation(loc, googleMapsApiKey);
      setLocationLabel(locationName);
      saveBookingDraft({ ...(getBookingDraft() || {}), location: loc, locationName });
      toast.success("Location saved.");
    } catch (err) { toast.error(readApiError(err)); }
  };

  const handleFind = () => {
    if (!selectedServiceId || !selectedVehicleId || !location) {
      toast.error("Select a service, choose a vehicle, and set your location first.");
      return;
    }
    saveBookingDraft({
      serviceId: selectedServiceId,
      vehicleId: Number(selectedVehicleId),
      location,
      locationName: locationLabel !== "Resolving your location..." ? locationLabel : undefined
    });
    navigate("/app/user/nearby");
  };

  const handleLogout = async () => { await logout(); navigate("/"); };

  return (
    <main className="page-shell">
      <Topbar eyebrow="User flow" title="Roadside help in a few taps" onLogout={handleLogout} />
      <div className="grid two mb-4">
        <article className="panel">
          <div className="panel-header">
            <div><h2 className="text-lg font-bold">Current Location</h2><p className="helper-text">Use your saved location to discover nearby mechanics.</p></div>
            <Button variant="secondary" size="sm" onClick={handleSetLocation}>Set Location</Button>
          </div>
          <span className="pill">{locationLabel}</span>
        </article>
        <article className="panel">
          <h2 className="text-lg font-bold">Select Vehicle</h2>
          <p className="helper-text mb-2">Choose one of your saved vehicles before booking.</p>
          <select className="w-full rounded-lg border border-input bg-background px-3 py-2.5 text-sm" value={selectedVehicleId} onChange={(e) => setSelectedVehicleId(e.target.value)}>
            <option value="">Select vehicle</option>
            {vehicles.map((v) => <option key={v.id} value={v.id}>{v.brand} {v.model} ({v.registrationNumber})</option>)}
          </select>
        </article>
      </div>
      <section className="panel">
        <div className="panel-header">
          <div><h2 className="text-lg font-bold">Select Service</h2><p className="helper-text">Available services are pulled live from the catalog.</p></div>
          <Button size="sm" onClick={handleFind}>Find Mechanic</Button>
        </div>
        <div className="grid grid-cols-1 gap-2">
          {services.map((s) => (
            <button key={s.id} type="button" onClick={() => setSelectedServiceId(s.id)}
              className={`text-left rounded-xl border-2 p-3 transition-colors ${selectedServiceId === s.id ? "border-primary bg-primary/5" : "border-border"}`}>
              <div className="service-name">{s.name}</div>
              <div className="text-sm text-muted-foreground">{s.description}</div>
              <span className="pill mt-1">{s.vehicleType}</span>
            </button>
          ))}
        </div>
      </section>
      <BottomNav role="user" />
    </main>
  );
};

export default UserHome;
