import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Topbar from "@/components/Topbar";
import BottomNav from "@/components/BottomNav";
import { Button } from "@/components/ui/button";
import { Switch } from "@/components/ui/switch";
import { toast } from "sonner";
import { apiGet, apiSend, logout } from "@/lib/api";
import { formatCurrency, withGeolocation, readApiError } from "@/lib/helpers";

const MechanicDashboard = () => {
  const navigate = useNavigate();
  const [available, setAvailable] = useState(false);
  const [mechanicName, setMechanicName] = useState("Mechanic");
  const [earningsTotal, setEarningsTotal] = useState("₹0");
  const [completedJobs, setCompletedJobs] = useState("0");
  const [currentBooking, setCurrentBooking] = useState("No active booking");
  const [locationLabel, setLocationLabel] = useState("Location not captured yet");

  useEffect(() => {
    Promise.all([
      apiGet("/api/v1/mechanics/me"),
      apiGet("/api/v1/mechanics/me/earnings"),
      apiGet("/api/v1/bookings/active"),
    ]).then(([profile, earnings, bookings]) => {
      setMechanicName(profile.data.name);
      setAvailable(Boolean(profile.data.available));
      setEarningsTotal(formatCurrency(earnings.data.totalEarnings));
      setCompletedJobs(String(earnings.data.completedJobs || 0));
      const active = bookings.data[0];
      setCurrentBooking(active ? active.status : "No active booking");
    }).catch((err) => toast.error(readApiError(err)));

    apiGet("/api/v1/mechanics/me/location/latest").then((res) => {
      if (res.data) setLocationLabel(`${res.data.latitude}, ${res.data.longitude}`);
    }).catch(() => {});
  }, []);

  const toggleAvailability = async (checked: boolean) => {
    setAvailable(checked);
    try {
      const res = await apiSend("/api/v1/mechanics/me/availability", "PATCH", { available: checked });
      toast.success("Availability updated.");
    } catch (err) {
      setAvailable(!checked);
      toast.error(readApiError(err));
    }
  };

  const saveLocation = async () => {
    try {
      const loc = await withGeolocation();
      await apiSend("/api/v1/mechanics/me/location", "POST", { latitude: loc.lat, longitude: loc.lng });
      setLocationLabel(`${loc.lat}, ${loc.lng}`);
      toast.success("Location saved.");
    } catch (err) { toast.error(readApiError(err)); }
  };

  const handleLogout = async () => { await logout(); navigate("/"); };

  return (
    <main className="page-shell">
      <Topbar eyebrow="Mechanic hub" title="Mechanic Dashboard" onLogout={handleLogout} />
      <div className="grid two mb-4">
        <article className="panel">
          <h2 className="text-lg font-bold">{mechanicName}</h2>
          <p className="helper-text">Current status</p>
          <div className="field-inline mt-2">
            <Switch checked={available} onCheckedChange={toggleAvailability} />
            <span className={`status-chip ${available ? "bg-success text-success-foreground" : "bg-secondary text-secondary-foreground"}`}>
              {available ? "ONLINE" : "OFFLINE"}
            </span>
          </div>
        </article>
        <article className="panel">
          <h2 className="text-lg font-bold">Current Booking</h2>
          <p className="service-name mt-2">{currentBooking}</p>
        </article>
      </div>
      <section className="panel mb-4">
        <div className="panel-header">
          <div>
            <h2 className="text-lg font-bold">Current Location</h2>
            <p className="helper-text">Mechanic live location will be reused across requests and tracking.</p>
          </div>
          <Button variant="secondary" size="sm" onClick={saveLocation}>Save Location</Button>
        </div>
        <span className="pill">{locationLabel}</span>
      </section>
      <section className="stat-grid">
        <article className="stat-card"><p className="helper-text">Earnings Today</p><div className="stat-value">{earningsTotal}</div></article>
        <article className="stat-card"><p className="helper-text">Completed Jobs</p><div className="stat-value">{completedJobs}</div></article>
      </section>
      <BottomNav role="mechanic" />
    </main>
  );
};

export default MechanicDashboard;
