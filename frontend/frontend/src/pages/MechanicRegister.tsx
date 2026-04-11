import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Topbar from "@/components/Topbar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { toast } from "sonner";
import { apiSend } from "@/lib/api";
import { patchAuthState } from "@/lib/auth";
import { readApiError } from "@/lib/helpers";
import { logout } from "@/lib/api";

const MechanicRegister = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({ experienceYears: "", skills: "", bio: "" });
  const [loading, setLoading] = useState(false);

  const handleLogout = async () => { await logout(); navigate("/"); };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await apiSend("/api/v1/mechanics/register", "POST", {
        experienceYears: Number(form.experienceYears),
        skills: form.skills.trim(),
        bio: form.bio.trim(),
      });
      patchAuthState({ accessToken: res.data.accessToken, refreshToken: res.data.refreshToken });
      toast.success("Mechanic profile created!");
      navigate("/app/mechanic/dashboard");
    } catch (err) { toast.error(readApiError(err)); }
    finally { setLoading(false); }
  };

  return (
    <main className="page-shell">
      <Topbar eyebrow="Mechanic onboarding" title="Become a Mechanic" onLogout={handleLogout} />
      <div className="panel">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="field">
            <Label>Experience</Label>
            <Input type="number" min={0} placeholder="5" value={form.experienceYears} onChange={(e) => setForm({ ...form, experienceYears: e.target.value })} />
          </div>
          <div className="field">
            <Label>Skills</Label>
            <Textarea placeholder="Engine diagnostics, battery jump start" value={form.skills} onChange={(e) => setForm({ ...form, skills: e.target.value })} />
          </div>
          <div className="field">
            <Label>Bio</Label>
            <Textarea placeholder="Tell users about your roadside expertise" value={form.bio} onChange={(e) => setForm({ ...form, bio: e.target.value })} />
          </div>
          <Button type="submit" className="w-full" disabled={loading}>{loading ? "Registering..." : "Register"}</Button>
        </form>
      </div>
    </main>
  );
};

export default MechanicRegister;
