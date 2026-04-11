import { useState } from "react";
import { useNavigate } from "react-router-dom";
import HeroPanel from "@/components/HeroPanel";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";
import { apiSend, apiGet } from "@/lib/api";
import { setAuthState, hasAuth, clearAuthState, patchAuthState } from "@/lib/auth";
import { readApiError } from "@/lib/helpers";
import { useEffect } from "react";

const Login = () => {
  const navigate = useNavigate();
  const [mobile, setMobile] = useState("");
  const [otp, setOtp] = useState("");
  const [otpSent, setOtpSent] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (hasAuth()) {
      apiGet("/api/v1/users/me").then((res) => {
        patchAuthState({ user: res.data });
        routeUser(res.data);
      }).catch(() => clearAuthState());
    }
  }, []);

  const routeUser = async (user: any) => {
    if (!user.profileCompleted) {
      navigate("/profile-setup");
      return;
    }
    if (user.role === "MECHANIC") {
      try {
        await apiGet("/api/v1/mechanics/me");
        navigate("/app/mechanic/dashboard");
      } catch (e: any) {
        if (e.status === 404) navigate("/mechanic-register");
        else navigate("/app/mechanic/dashboard");
      }
    } else {
      navigate("/app/user/home");
    }
  };

  const handleRequestOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!mobile.trim()) { toast.error("Please enter your mobile number"); return; }
    setLoading(true);
    try {
      await apiSend("/api/v1/auth/otp/request", "POST", { mobile: mobile.trim() }, { auth: false });
      setOtpSent(true);
      toast.success("OTP sent! Check backend logs.");
    } catch (err) { toast.error(readApiError(err)); }
    finally { setLoading(false); }
  };

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!otp.trim()) { toast.error("Please enter OTP"); return; }
    setLoading(true);
    try {
      const res = await apiSend("/api/v1/auth/otp/verify", "POST", { mobile: mobile.trim(), otp: otp.trim() }, { auth: false });
      setAuthState({
        accessToken: res.data.accessToken,
        refreshToken: res.data.refreshToken,
        user: { userId: res.data.userId, mobile: res.data.mobile, role: res.data.role, profileCompleted: res.data.profileCompleted },
      });
      if (res.data.profileCompleted) {
        await routeUser(res.data);
      } else {
        navigate("/profile-setup");
      }
    } catch (err) { toast.error(readApiError(err)); }
    finally { setLoading(false); }
  };

  return (
    <main className="page-shell flex items-center justify-center !pt-0">
      <section className="w-full space-y-6">
        <HeroPanel eyebrow="Trusted roadside support" title="Call Your Mechanic" subtitle="Get verified mechanics, live updates, and vehicle-specific help in one place." />
        <div className="panel space-y-4">
          <div className="field">
            <Label htmlFor="mobile">Enter Mobile Number</Label>
            <Input id="mobile" type="tel" placeholder="+91 9876543210" value={mobile} onChange={(e) => setMobile(e.target.value)} />
          </div>
          {!otpSent ? (
            <Button variant="secondary" className="w-full" disabled={loading} onClick={handleRequestOtp}>
              {loading ? "Sending..." : "Request OTP"}
            </Button>
          ) : (
            <>
              <div className="field">
                <Label htmlFor="otp">Enter OTP</Label>
                <Input id="otp" type="text" inputMode="numeric" maxLength={6} placeholder="_ _ _ _ _ _" value={otp} onChange={(e) => setOtp(e.target.value)} />
              </div>
              <Button className="w-full" disabled={loading} onClick={handleVerify}>
                {loading ? "Verifying..." : "Verify & Continue"}
              </Button>
            </>
          )}
          <p className="helper-text text-center">During development, OTP is logged by the backend after request.</p>
        </div>
      </section>
    </main>
  );
};

export default Login;
