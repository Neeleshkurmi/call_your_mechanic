interface HeroPanelProps {
  eyebrow: string;
  title: string;
  subtitle: string;
}

const HeroPanel = ({ eyebrow, title, subtitle }: HeroPanelProps) => (
  <header className="hero-panel">
    <div className="brand-mark">CYM</div>
    <p className="eyebrow !text-primary">{eyebrow}</p>
    <h1>{title}</h1>
    <p className="hero-copy">{subtitle}</p>
  </header>
);

export default HeroPanel;
