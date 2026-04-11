import { useState, useEffect } from "react";
import Topbar from "@/components/Topbar";
import BottomNav from "@/components/BottomNav";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import { apiGet, apiSend, openAuthenticatedSse } from "@/lib/api";
import { getActiveBookingId, setActiveBookingId, clearActiveBookingId } from "@/lib/auth";
import { readApiError, statusClass, estimateEta, formatDate } from "@/lib/helpers";
import { Wrapper, Status } from "@googlemaps/react-wrapper";
import { Map, Marker, DirectionsRoute } from "./MapComponents.tsx";

const UserTracking = () => {
  const googleMapsApiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;
  const [bookingId, setBookingId] = useState<string | null>(null);
  const [mechanicName, setMechanicName] = useState("Mechanic");
  const [status, setStatus] = useState("Loading");
  const [eta, setEta] = useState("Updating");
  const [mechanicMobile, setMechanicMobile] = useState("");
  const [userCoords, setUserCoords] = useState<{ lat: number; lng: number } | null>(null);
  const [mechanicCoords, setMechanicCoords] = useState<{ lat: number; lng: number } | null>(null);
  const [loading, setLoading] = useState(true);

  const render = (status: Status) => {
    switch (status) {
      case Status.LOADING:
        return <div className="text-center py-4">Loading map...</div>;
      case Status.FAILURE:
        return <div className="text-center py-4 text-destructive">Error loading map</div>;
      case Status.SUCCESS:
        return <Map center={userCoords || { lat: 0, lng: 0 }} zoom={15}>
          {userCoords && mechanicCoords && <DirectionsRoute origin={userCoords} destination={mechanicCoords} />}
          {userCoords && <Marker position={userCoords} title="Your Location" />}
          {mechanicCoords && <Marker position={mechanicCoords} title={`${mechanicName}'s Location`} />}
        </Map>;
    }
  };

  useEffect(() => {
    const init = async () => {
      let bid = getActiveBookingId();
      if (!bid) {
        try {
          const res = await apiGet("/api/v1/bookings/active");
          const active = res.data.find((b: { status: string; bookingId: string | number }) =>
            ["REQUESTED", "ACCEPTED", "ON_THE_WAY", "STARTED"].includes(b.status)
          );
          if (active) {
            bid = String(active.bookingId);
            setActiveBookingId(active.bookingId);
          }
        } catch (error) {
          console.error("Failed to fetch active booking:", error);
        }
      }
      if (!bid) {
        toast.error("No active booking selected.");
        setLoading(false);
        return;
      }
      setBookingId(bid);

      try {
        const [bk, mech] = await Promise.all([
          apiGet(`/api/v1/bookings/${bid}`),
          apiGet(`/api/v1/bookings/${bid}`).then((r) => apiGet(`/api/v1/mechanics/${r.data.mechanicId}`))
        ]);
        setMechanicName(mech.data.name);
        setMechanicMobile(mech.data.mobile || "");
        setStatus(bk.data.status);
        setEta(estimateEta(bk.data.status));
      } catch (error) {
        console.error("Failed to fetch booking details:", error);
        toast.error("Failed to load booking details");
      }

      try {
        const snap = await apiGet(`/api/v1/bookings/${bid}/location/latest`);
        if (snap.data?.userLocation) setUserCoords({ lat: snap.data.userLocation.latitude, lng: snap.data.userLocation.longitude });
        if (snap.data?.mechanicLocation) setMechanicCoords({ lat: snap.data.mechanicLocation.latitude, lng: snap.data.mechanicLocation.longitude });
      } catch (error) {
        console.error("Failed to fetch location snapshot:", error);
      }

      try {
        const stream = await openAuthenticatedSse(`/api/v1/bookings/${bid}/location/stream`, {
          onMessage: (event: MessageEvent | { data: unknown }) => {
            const p = event.data || event;
            if (p && typeof p === 'object' && 'actorType' in p && 'latitude' in p && 'longitude' in p) {
              const locationData = p as { actorType: string; latitude: number; longitude: number };
              if (locationData.actorType === "USER") {
                setUserCoords({ lat: locationData.latitude, lng: locationData.longitude });
              }
              if (locationData.actorType === "MECHANIC") {
                setMechanicCoords({ lat: locationData.latitude, lng: locationData.longitude });
              }
            }
          },
          onError: () => toast.error("Live updates paused."),
        });
        window.addEventListener("beforeunload", () => stream.close(), { once: true });
      } catch (error) {
        console.error("Failed to establish location stream:", error);
        toast.error("Failed to connect to live updates");
      }

      setLoading(false);
    };
    init();
  }, []);

  const handleCancel = async () => {
    if (!bookingId) return;
    try {
      const res = await apiSend(`/api/v1/bookings/${bookingId}/cancel`, "PATCH");
      setStatus(res.data.status);
      setEta(estimateEta(res.data.status));
      clearActiveBookingId();
      toast.success("Booking cancelled.");
    } catch (err) { toast.error(readApiError(err)); }
  };

  const sc = statusClass(status);

  return (
    <main className="page-shell">
      <Topbar eyebrow="Tracking" title="Live Tracking" actionLabel="Home" actionTo="/app/user/home" />
      {loading ? <p className="helper-text text-center py-10">Loading...</p> : !bookingId ? (
        <div className="panel text-center py-10"><p className="helper-text">No active booking.</p></div>
      ) : (
        <>
          <div className="grid two mb-4">
            <article className="rounded-2xl bg-secondary p-5">
              <h2 className="text-lg font-bold mb-1">Live Map</h2>
              <p className="helper-text mb-2">Track your mechanic in real-time.</p>
              {googleMapsApiKey ? (
                <Wrapper apiKey={googleMapsApiKey} render={render} />
              ) : (
                <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
                  Google Maps API key is missing. Add <code>VITE_GOOGLE_MAPS_API_KEY</code> to your <code>.env</code> file and restart the Vite server.
                </div>
              )}
            </article>
            <article className="panel">
              <h2 className="text-lg font-bold">{mechanicName}</h2>
              <div className="flex gap-2 mt-2 mb-3">
                <span className={`status-chip ${sc === "warning" ? "bg-primary/20 text-primary" : sc === "success" ? "bg-success text-success-foreground" : sc === "danger" ? "bg-destructive text-destructive-foreground" : "bg-secondary"}`}>{status}</span>
                <span className="pill">{eta}</span>
              </div>
              <div className="button-row">
                {mechanicMobile && <a href={`tel:${mechanicMobile}`}><Button variant="secondary" size="sm">Call</Button></a>}
                <Button variant="destructive" size="sm" onClick={handleCancel}>Cancel</Button>
              </div>
            </article>
          </div>
        </>
      )}
      <BottomNav role="user" />
    </main>
  );
};

export default UserTracking;
