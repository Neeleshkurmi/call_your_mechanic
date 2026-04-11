import { useState, useEffect } from "react";
import Topbar from "@/components/Topbar";
import BottomNav from "@/components/BottomNav";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import { apiGet, apiSend } from "@/lib/api";
import { readApiError } from "@/lib/helpers";

interface BookingRequest {
  bookingId: number;
  serviceId: number;
  latitude: number;
  longitude: number;
  status: string;
}

const MechanicRequests = () => {
  const [requests, setRequests] = useState<BookingRequest[]>([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    try {
      const res = await apiGet("/api/v1/mechanics/me/bookings");
      setRequests(res.data.filter((b: any) => b.status === "REQUESTED"));
    } catch (err) { toast.error(readApiError(err)); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleAction = async (id: number, action: "accept" | "reject") => {
    try {
      await apiSend(`/api/v1/bookings/${id}/${action}`, "PATCH");
      toast.success(`Booking ${action}ed.`);
      load();
    } catch (err) { toast.error(readApiError(err)); }
  };

  return (
    <main className="page-shell">
      <Topbar eyebrow="Requests" title="Incoming Requests" actionLabel="Dashboard" actionTo="/app/mechanic/dashboard" />
      <section className="request-list space-y-3">
        {loading ? <p className="helper-text text-center py-10">Loading...</p> :
          requests.length === 0 ? (
            <div className="panel text-center py-10"><p className="helper-text">No incoming requests right now.</p></div>
          ) : requests.map((b) => (
            <article key={b.bookingId} className="panel">
              <div className="font-bold mb-1">Booking #{b.bookingId}</div>
              <div className="flex gap-2 mb-3">
                <span className="pill">Service ID {b.serviceId}</span>
                <span className="pill">{b.latitude}, {b.longitude}</span>
              </div>
              <div className="button-row">
                <Button size="sm" onClick={() => handleAction(b.bookingId, "accept")}>Accept</Button>
                <Button size="sm" variant="destructive" onClick={() => handleAction(b.bookingId, "reject")}>Reject</Button>
              </div>
            </article>
          ))
        }
      </section>
      <BottomNav role="mechanic" />
    </main>
  );
};

export default MechanicRequests;
