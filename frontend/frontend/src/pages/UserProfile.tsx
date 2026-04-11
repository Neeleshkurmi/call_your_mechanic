import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Topbar from "@/components/Topbar";
import BottomNav from "@/components/BottomNav";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";
import { apiGet, apiSend, logout } from "@/lib/api";
import { patchAuthState } from "@/lib/auth";
import { readApiError } from "@/lib/helpers";

const UserProfile = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", mobile: "", role: "USER" });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    apiGet("/api/v1/users/me").then((res) => {
      setForm({ name: res.data.name || "", mobile: res.data.mobile || "", role: res.data.role || "USER" });
    }).catch((err) => toast.error(readApiError(err)));
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await apiSend("/api/v1/users/me", "PUT", { name: form.name.trim(), role: form.role });
      patchAuthState({ user: res.data });
      toast.success("Profile updated.");
    } catch (err) { toast.error(readApiError(err)); }
    finally { setLoading(false); }
  };

  const handleLogout = async () => { await logout(); navigate("/"); };

  return (
    <main className="page-shell">
      <Topbar eyebrow="Account" title="User Profile" onLogout={handleLogout} />
      <div className="panel">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="field"><Label>Name</Label><Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></div>
          <div className="field"><Label>Phone</Label><Input value={form.mobile} readOnly /></div>
          <div className="field">
            <Label>Role</Label>
            <select className="w-full rounded-lg border border-input bg-background px-3 py-2.5 text-sm" value={form.role} disabled>
              <option value="USER">User</option>
              <option value="MECHANIC">Mechanic</option>
            </select>
          </div>
          <Button type="submit" className="w-full" disabled={loading}>{loading ? "Updating..." : "Update Profile"}</Button>
        </form>
      </div>
      <BottomNav role="user" />
    </main>
  );
};

export default UserProfile;
