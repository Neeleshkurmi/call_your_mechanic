import { NavLink } from "react-router-dom";
import { Home, Calendar, Car, User, Bell, Wrench, DollarSign } from "lucide-react";

interface BottomNavProps {
  role: "user" | "mechanic";
}

const userLinks = [
  { to: "/app/user/home", icon: Home },
  { to: "/app/user/tracking", icon: Calendar },
  { to: "/app/user/vehicles", icon: Car },
  { to: "/app/user/profile", icon: User },
];

const mechanicLinks = [
  { to: "/app/mechanic/dashboard", icon: Home },
  { to: "/app/mechanic/requests", icon: Bell },
  { to: "/app/mechanic/job", icon: Wrench },
  { to: "/app/mechanic/earnings", icon: DollarSign },
  { to: "/app/mechanic/profile", icon: User },
];

const BottomNav = ({ role }: BottomNavProps) => {
  const links = role === "user" ? userLinks : mechanicLinks;
  return (
    <nav className="bottom-nav">
      {links.map((l) => (
        <NavLink
          key={l.to}
          to={l.to}
          className={({ isActive }) => (isActive ? "active" : "")}
        >
          <l.icon size={20} />
        </NavLink>
      ))}
    </nav>
  );
};

export default BottomNav;
