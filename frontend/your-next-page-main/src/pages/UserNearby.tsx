import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Topbar from "@/components/Topbar";
import BottomNav from "@/components/BottomNav";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import { apiGet } from "@/lib/api";
import { getBookingDraft, saveBookingDraft } from "@/lib/auth";
import { readApiError } from "@/lib/helpers";

interface Mechanic { mechanicId: number; name: string; rating: number | null; distanceKm: number; experienceYears: number; skills: string; }

const UserNearby = () => {
  const navigate = useNavigate();
  const [mechanics, setMechanics] = useState<Mechanic[]>([]);
  const [loading, setLoading] = useState(true);
  const draft = getBookingDraft();

  useEffect(() => {
    if (!draft?.location) { toast.error("Start from home to choose a service and location."); setLoading(false); return; }
    apiGet(`/api/v1/mechanics/nearby?lat=${draft.location.lat}&lng=${draft.location.lng}`)
      .then((res) => setMechanics(res.data))
      .catch((err) => toast.error(readApiError(err)))
      .finally(() => setLoading(false));
  }, []);

  const selectMechanic = (m: Mechanic) => {
    saveBookingDraft({ ...draft!, mechanicId: m.mechanicId, mechanicName: m.name });
    navigate("/app/user/booking");
  };

  return (
    <main className="page-shell">
      <Topbar eyebrow="Discovery" title="Nearby Mechanics" actionLabel="Back" actionTo="/app/user/home" />
      {draft?.location && <p className="helper-text mb-4">Searching near {draft.location.lat}, {draft.location.lng}</p>}
      <section className="rounded-2xl bg-secondary p-6 mb-4 text-center">
        <h2 className="text-lg font-bold">Map-ready search area</h2>
        <p className="helper-text">Placeholder for map integration.</p>
      </section>
      <section className="space-y-3">
        {loading ? <p className="helper-text text-center py-10">Loading...</p> :
          mechanics.length === 0 ? (
            <div className="panel text-center py-10"><p className="helper-text">No mechanics nearby. Try again later.</p></div>
          ) : mechanics.map((m) => (
            <article key={m.mechanicId} className="panel">
              <div className="font-bold mb-1">{m.name}</div>
              <div className="flex flex-wrap gap-2 mb-2">
                <span className="pill">{m.rating ?? "New"} star</span>
                <span className="pill">{m.distanceKm} km away</span>
                <span className="pill">{m.experienceYears} yrs</span>
              </div>
              <p className="text-sm text-muted-foreground mb-3">{m.skills}</p>
              <Button size="sm" onClick={() => selectMechanic(m)}>Select Mechanic</Button>
            </article>
          ))
        }
      </section>
      <BottomNav role="user" />
    </main>
  );
};

export default UserNearby;
