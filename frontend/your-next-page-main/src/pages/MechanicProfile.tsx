import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Topbar from "@/components/Topbar";
import BottomNav from "@/components/BottomNav";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { toast } from "sonner";
import { apiGet, apiSend, logout } from "@/lib/api";
import { readApiError } from "@/lib/helpers";

const MechanicProfile = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", mobile: "", rating: "", experienceYears: "", skills: "", bio: "" });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    apiGet("/api/v1/mechanics/me").then((res) => {
      const d = res.data;
      setForm({ name: d.name || "", mobile: d.mobile || "", rating: d.rating ?? "New", experienceYears: String(d.experienceYears ?? 0), skills: d.skills || "", bio: d.bio || "" });
    }).catch((err) => toast.error(readApiError(err)));
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await apiSend("/api/v1/mechanics/me", "PUT", {
        experienceYears: Number(form.experienceYears),
        skills: form.skills.trim(),
        bio: form.bio.trim(),
      });
      toast.success("Profile updated!");
    } catch (err) { toast.error(readApiError(err)); }
    finally { setLoading(false); }
  };

  const handleLogout = async () => { await logout(); navigate("/"); };

  return (
    <main className="page-shell">
      <Topbar eyebrow="Mechanic identity" title="Mechanic Profile" onLogout={handleLogout} />
      <div className="panel">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid two">
            <div className="field"><Label>Name</Label><Input value={form.name} readOnly /></div>
            <div className="field"><Label>Phone</Label><Input value={form.mobile} readOnly /></div>
            <div className="field"><Label>Rating</Label><Input value={form.rating} readOnly /></div>
            <div className="field"><Label>Experience Years</Label><Input type="number" min={0} value={form.experienceYears} onChange={(e) => setForm({ ...form, experienceYears: e.target.value })} /></div>
          </div>
          <div className="field"><Label>Skills</Label><Textarea value={form.skills} onChange={(e) => setForm({ ...form, skills: e.target.value })} /></div>
          <div className="field"><Label>Bio</Label><Textarea value={form.bio} onChange={(e) => setForm({ ...form, bio: e.target.value })} /></div>
          <Button type="submit" className="w-full" disabled={loading}>{loading ? "Updating..." : "Update Profile"}</Button>
        </form>
      </div>
      <BottomNav role="mechanic" />
    </main>
  );
};

export default MechanicProfile;
