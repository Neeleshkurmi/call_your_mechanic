import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import Login from "./pages/Login";
import ProfileSetup from "./pages/ProfileSetup";
import MechanicRegister from "./pages/MechanicRegister";
import MechanicDashboard from "./pages/MechanicDashboard";
import MechanicRequests from "./pages/MechanicRequests";
import MechanicJob from "./pages/MechanicJob";
import MechanicEarnings from "./pages/MechanicEarnings";
import MechanicProfile from "./pages/MechanicProfile";
import UserHome from "./pages/UserHome";
import UserNearby from "./pages/UserNearby";
import UserBooking from "./pages/UserBooking";
import UserTracking from "./pages/UserTracking";
import UserVehicles from "./pages/UserVehicles";
import UserProfile from "./pages/UserProfile";
import NotFound from "./pages/NotFound";

const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <Toaster />
      <Sonner />
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/profile-setup" element={<ProfileSetup />} />
          <Route path="/mechanic-register" element={<MechanicRegister />} />
          <Route path="/app/mechanic/dashboard" element={<MechanicDashboard />} />
          <Route path="/app/mechanic/requests" element={<MechanicRequests />} />
          <Route path="/app/mechanic/job" element={<MechanicJob />} />
          <Route path="/app/mechanic/earnings" element={<MechanicEarnings />} />
          <Route path="/app/mechanic/profile" element={<MechanicProfile />} />
          <Route path="/app/user/home" element={<UserHome />} />
          <Route path="/app/user/nearby" element={<UserNearby />} />
          <Route path="/app/user/booking" element={<UserBooking />} />
          <Route path="/app/user/tracking" element={<UserTracking />} />
          <Route path="/app/user/vehicles" element={<UserVehicles />} />
          <Route path="/app/user/profile" element={<UserProfile />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
