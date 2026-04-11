import { useState, useEffect } from "react";
import Topbar from "@/components/Topbar";
import BottomNav from "@/components/BottomNav";
import { toast } from "sonner";
import { apiGet } from "@/lib/api";
import { formatCurrency, formatDate, readApiError } from "@/lib/helpers";

const MechanicEarnings = () => {
  const [total, setTotal] = useState("₹0");
  const [completed, setCompleted] = useState("0");
  const [average, setAverage] = useState("₹0");
  const [bookings, setBookings] = useState<any[]>([]);

  useEffect(() => {
    Promise.all([
      apiGet("/api/v1/mechanics/me/earnings"),
      apiGet("/api/v1/mechanics/me/bookings"),
    ]).then(([earnings, bk]) => {
      setTotal(formatCurrency(earnings.data.totalEarnings));
      setCompleted(String(earnings.data.completedJobs || 0));
      setAverage(earnings.data.completedJobs ? formatCurrency(earnings.data.totalEarnings / earnings.data.completedJobs) : "₹0");
      setBookings(bk.data.filter((b: any) => b.status === "COMPLETED"));
    }).catch((err) => toast.error(readApiError(err)));
  }, []);

  return (
    <main className="page-shell">
      <Topbar eyebrow="Payout overview" title="Earnings" actionLabel="Dashboard" actionTo="/app/mechanic/dashboard" />
      <section className="stat-grid mb-4">
        <article className="stat-card"><p className="helper-text">Total Earnings</p><div className="stat-value">{total}</div></article>
        <article className="stat-card"><p className="helper-text">Completed Jobs</p><div className="stat-value">{completed}</div></article>
        <article className="stat-card col-span-2"><p className="helper-text">Average Per Job</p><div className="stat-value">{average}</div></article>
      </section>
      <section className="booking-list space-y-3">
        {bookings.length === 0 ? (
          <div className="panel text-center py-10"><p className="helper-text">No completed jobs yet.</p></div>
        ) : bookings.map((b) => (
          <article key={b.bookingId} className="panel">
            <div className="font-bold">Booking #{b.bookingId}</div>
            <div className="flex gap-2 mt-1">
              <span className="status-chip bg-success text-success-foreground">COMPLETED</span>
              <span className="pill">{formatDate(b.bookingTime)}</span>
            </div>
          </article>
        ))}
      </section>
      <BottomNav role="mechanic" />
    </main>
  );
};

export default MechanicEarnings;
