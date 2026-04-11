import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import HeroPanel from "@/components/HeroPanel";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";
import { apiGet, apiSend } from "@/lib/api";
import { getAuthState, patchAuthState } from "@/lib/auth";
import { readApiError } from "@/lib/helpers";

const ProfileSetup = () => {
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [role, setRole] = useState<"USER" | "MECHANIC" | "">("");
  const [loading, setLoading] = useState(false);
  const [userMobile, setUserMobile] = useState("");

  useEffect(() => {
    apiGet("/api/v1/users/me").then((res) => {
      patchAuthState({ user: res.data });
      setUserMobile(res.data.mobile || "");
      setName(res.data.name || "");
      if (res.data.role) setRole(res.data.role);
      if (res.data.profileCompleted) {
        navigate(res.data.role === "MECHANIC" ? "/app/mechanic/dashboard" : "/app/user/home");
      }
    }).catch(() => navigate("/"));
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !role) { toast.error("Please fill in all fields"); return; }
    setLoading(true);
    try {
      const res = await apiSend("/api/v1/users/me/profile", "POST", { name: name.trim(), role });
      patchAuthState({ accessToken: res.data.accessToken, refreshToken: res.data.refreshToken, user: res.data.user });
      toast.success("Profile saved!");
      if (role === "MECHANIC") navigate("/mechanic-register");
      else navigate("/app/user/home");
    } catch (err) { toast.error(readApiError(err)); }
    finally { setLoading(false); }
  };

  return (
    <main className="page-shell flex items-center justify-center !pt-0">
      <section className="w-full space-y-6">
        <HeroPanel eyebrow="Finish onboarding" title="Complete Your Profile" subtitle="Choose how you want to use the app today." />
        <div className="panel">
          <p className="helper-text mb-4">Signed in as <strong>{userMobile || "..."}</strong></p>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="field">
              <Label htmlFor="full-name">Name</Label>
              <Input id="full-name" placeholder="Your full name" value={name} onChange={(e) => setName(e.target.value)} />
            </div>
            <div className="field">
              <Label>Select Role</Label>
              <label className="field-inline cursor-pointer">
                <input type="radio" name="role" value="USER" checked={role === "USER"} onChange={() => setRole("USER")} className="accent-primary" />
                <span>User</span>
              </label>
              <label className="field-inline cursor-pointer">
                <input type="radio" name="role" value="MECHANIC" checked={role === "MECHANIC"} onChange={() => setRole("MECHANIC")} className="accent-primary" />
                <span>Mechanic</span>
              </label>
            </div>
            <Button type="submit" className="w-full" disabled={loading}>{loading ? "Saving..." : "Continue"}</Button>
          </form>
        </div>
      </section>
    </main>
  );
};

export default ProfileSetup;
