import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Topbar from "@/components/Topbar";
import BottomNav from "@/components/BottomNav";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";
import { apiGet, apiSend, logout } from "@/lib/api";
import { readApiError } from "@/lib/helpers";

interface Vehicle { id: number; brand: string; model: string; registrationNumber: string; vehicleType: string; }

const UserVehicles = () => {
  const navigate = useNavigate();
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState({ vehicleType: "CAR", brand: "", model: "", registrationNumber: "" });

  const load = () => apiGet("/api/v1/vehicles").then((r) => setVehicles(r.data)).catch((e) => toast.error(readApiError(e)));

  useEffect(() => { load(); }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const payload = { vehicleType: form.vehicleType, brand: form.brand.trim(), model: form.model.trim(), registrationNumber: form.registrationNumber.trim() };
    try {
      if (editingId) { await apiSend(`/api/v1/vehicles/${editingId}`, "PUT", payload); toast.success("Vehicle updated."); }
      else { await apiSend("/api/v1/vehicles", "POST", payload); toast.success("Vehicle added."); }
      setEditingId(null);
      setForm({ vehicleType: "CAR", brand: "", model: "", registrationNumber: "" });
      load();
    } catch (err) { toast.error(readApiError(err)); }
  };

  const handleEdit = (v: Vehicle) => {
    setEditingId(v.id);
    setForm({ vehicleType: v.vehicleType, brand: v.brand, model: v.model, registrationNumber: v.registrationNumber });
  };

  const handleDelete = async (id: number) => {
    await apiSend(`/api/v1/vehicles/${id}`, "DELETE");
    toast.success("Vehicle deleted.");
    if (editingId === id) { setEditingId(null); setForm({ vehicleType: "CAR", brand: "", model: "", registrationNumber: "" }); }
    load();
  };

  const handleLogout = async () => { await logout(); navigate("/"); };

  return (
    <main className="page-shell">
      <Topbar eyebrow="Garage" title="My Vehicles" onLogout={handleLogout} />
      <div className="grid two">
        <section className="panel">
          <h2 className="text-lg font-bold mb-3">{editingId ? "Edit vehicle" : "Add vehicle"}</h2>
          <form onSubmit={handleSubmit} className="space-y-3">
            <div className="field">
              <Label>Vehicle Type</Label>
              <select className="w-full rounded-lg border border-input bg-background px-3 py-2.5 text-sm" value={form.vehicleType} onChange={(e) => setForm({ ...form, vehicleType: e.target.value })}>
                <option value="CAR">Car</option>
                <option value="BIKE">Bike</option>
              </select>
            </div>
            <div className="field"><Label>Brand</Label><Input value={form.brand} onChange={(e) => setForm({ ...form, brand: e.target.value })} /></div>
            <div className="field"><Label>Model</Label><Input value={form.model} onChange={(e) => setForm({ ...form, model: e.target.value })} /></div>
            <div className="field"><Label>Registration Number</Label><Input value={form.registrationNumber} onChange={(e) => setForm({ ...form, registrationNumber: e.target.value })} /></div>
            <Button type="submit" className="w-full">Save Vehicle</Button>
          </form>
        </section>
        <section className="space-y-3">
          {vehicles.length === 0 ? (
            <div className="panel text-center py-10"><p className="helper-text">No vehicles added yet.</p></div>
          ) : vehicles.map((v) => (
            <article key={v.id} className="panel">
              <div className="font-bold">{v.brand} {v.model}</div>
              <div className="flex gap-2 mt-1 mb-3">
                <span className="pill">{v.vehicleType}</span>
                <span className="pill">{v.registrationNumber}</span>
              </div>
              <div className="button-row">
                <Button variant="secondary" size="sm" onClick={() => handleEdit(v)}>Edit</Button>
                <Button variant="destructive" size="sm" onClick={() => handleDelete(v.id)}>Delete</Button>
              </div>
            </article>
          ))}
        </section>
      </div>
      <BottomNav role="user" />
    </main>
  );
};

export default UserVehicles;
