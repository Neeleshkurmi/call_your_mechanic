import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Topbar from "@/components/Topbar";
import BottomNav from "@/components/BottomNav";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import { apiGet, apiSend } from "@/lib/api";
import { getBookingDraft, clearBookingDraft, setActiveBookingId } from "@/lib/auth";
import { readApiError } from "@/lib/helpers";

const UserBooking = () => {
  const navigate = useNavigate();
  const [mechanicName, setMechanicName] = useState("Loading...");
  const [serviceName, setServiceName] = useState("Loading...");
  const [vehicleName, setVehicleName] = useState("Loading...");
  const [locationText, setLocationText] = useState("Loading...");
  const [loading, setLoading] = useState(false);
  const draft = getBookingDraft();

  useEffect(() => {
    if (!draft?.mechanicId) { toast.error("Choose a nearby mechanic first."); return; }
    setLocationText(`${draft.location?.lat}, ${draft.location?.lng}`);
    Promise.all([
      apiGet(`/api/v1/services/${draft.serviceId}`, { auth: false }),
      apiGet("/api/v1/vehicles"),
      apiGet(`/api/v1/mechanics/${draft.mechanicId}`),
    ]).then(([svc, veh, mech]) => {
      setMechanicName(mech.data.name);
      setServiceName(svc.data.name);
      const v = veh.data.find((x: any) => String(x.id) === String(draft.vehicleId));
      setVehicleName(v ? `${v.brand} ${v.model}` : "Selected vehicle");
    }).catch((err) => toast.error(readApiError(err)));
  }, []);

  const handleConfirm = async () => {
    if (!draft) return;
    setLoading(true);
    try {
      const res = await apiSend("/api/v1/bookings", "POST", {
        mechanicId: draft.mechanicId,
        vehicleId: draft.vehicleId,
        serviceId: draft.serviceId,
        latitude: draft.location?.lat,
        longitude: draft.location?.lng,
      });
      clearBookingDraft();
      setActiveBookingId(res.data.bookingId);
      toast.success("Booking confirmed!");
      navigate(`/app/user/tracking`);
    } catch (err) { toast.error(readApiError(err)); }
    finally { setLoading(false); }
  };

  return (
    <main className="page-shell">
      <Topbar eyebrow="Booking" title="Confirm Booking" actionLabel="Back" actionTo="/app/user/nearby" />
      <div className="panel">
        <div className="grid two mb-4">
          <article className="bg-secondary rounded-xl p-4"><h3 className="text-sm font-medium text-muted-foreground">Mechanic</h3><p className="service-name">{mechanicName}</p></article>
          <article className="bg-secondary rounded-xl p-4"><h3 className="text-sm font-medium text-muted-foreground">Service</h3><p className="service-name">{serviceName}</p></article>
          <article className="bg-secondary rounded-xl p-4"><h3 className="text-sm font-medium text-muted-foreground">Vehicle</h3><p className="service-name">{vehicleName}</p></article>
          <article className="bg-secondary rounded-xl p-4"><h3 className="text-sm font-medium text-muted-foreground">Location</h3><p className="service-name">{locationText}</p></article>
        </div>
        <Button className="w-full" disabled={loading} onClick={handleConfirm}>{loading ? "Confirming..." : "Confirm Booking"}</Button>
      </div>
      <BottomNav role="user" />
    </main>
  );
};

export default UserBooking;
