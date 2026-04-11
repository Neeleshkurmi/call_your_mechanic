import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";

interface TopbarProps {
  eyebrow: string;
  title: string;
  actionLabel?: string;
  actionTo?: string;
  onLogout?: () => void;
}

const Topbar = ({ eyebrow, title, actionLabel, actionTo, onLogout }: TopbarProps) => {
  const navigate = useNavigate();
  return (
    <header className="topbar">
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h1 className="text-2xl font-bold">{title}</h1>
      </div>
      {onLogout && (
        <Button variant="secondary" size="sm" onClick={onLogout}>
          Logout
        </Button>
      )}
      {actionLabel && actionTo && (
        <Button variant="secondary" size="sm" onClick={() => navigate(actionTo)}>
          {actionLabel}
        </Button>
      )}
    </header>
  );
};

export default Topbar;
