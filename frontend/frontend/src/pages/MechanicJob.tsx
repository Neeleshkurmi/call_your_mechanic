import { useState, useEffect } from "react";
import Topbar from "@/components/Topbar";
import BottomNav from "@/components/BottomNav";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import { apiGet, apiSend } from "@/lib/api";
import { readApiError, statusClass } from "@/lib/helpers";

const MechanicJob = () => {
  const [active, setActive] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiGet("/api/v1/bookings/active").then((res) => {
      const job = res.data.find((b: any) => ["ACCEPTED", "ON_THE_WAY", "STARTED"].includes(b.status));
      setActive(job || null);
    }).catch((err) => toast.error(readApiError(err)))
      .finally(() => setLoading(false));
  }, []);

  const nextAction = active ? ({
    ACCEPTED: { label: "Start travel", status: "ON_THE_WAY" },
    ON_THE_WAY: { label: "Start job", status: "STARTED" },
    STARTED: { label: "Complete job", status: "COMPLETED" },
  } as any)[active.status] : null;

  const handleNext = async () => {
    if (!active || !nextAction) return;
    try {
      if (nextAction.status === "COMPLETED") {
        await apiSend(`/api/v1/bookings/${active.bookingId}/complete`, "PATCH");
      } else {
        await apiSend(`/api/v1/bookings/${active.bookingId}/status`, "PATCH", { status: nextAction.status });
      }
      toast.success("Job updated.");
      window.location.reload();
    } catch (err) { toast.error(readApiError(err)); }
  };

  return (
    <main className="page-shell">
      <Topbar eyebrow="Active job" title="Job Progress" actionLabel="Requests" actionTo="/app/mechanic/requests" />
      <section className="booking-list">
        {loading ? <p className="helper-text text-center py-10">Loading...</p> :
          !active ? (
            <div className="panel text-center py-10"><p className="helper-text">No active job at the moment.</p></div>
          ) : (
            <article className="panel">
              <div className="font-bold mb-1">Booking #{active.bookingId}</div>
              <div className="flex gap-2 mb-3">
                <span className={`status-chip ${statusClass(active.status) === "warning" ? "bg-primary/20 text-primary" : "bg-success text-success-foreground"}`}>{active.status}</span>
                <span className="pill">{active.latitude}, {active.longitude}</span>
              </div>
              {nextAction && <Button onClick={handleNext}>{nextAction.label}</Button>}
            </article>
          )
        }
      </section>
      <BottomNav role="mechanic" />
    </main>
  );
};

export default MechanicJob;
