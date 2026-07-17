import "./PhoneMockup.css";

export default function PhoneMockup({ children, className = "" }) {
  return (
    <div className={`phone-perspective ${className}`}>
      <div className="phone-body">
        <div className="phone-notch">
          <div className="phone-notch-camera" />
        </div>
        <div className="phone-screen">
          {children}
        </div>
        <div className="phone-reflection" />
        <div className="phone-side phone-side-left" />
        <div className="phone-side phone-side-right" />
        <div className="phone-side phone-side-top" />
        <div className="phone-side phone-side-bottom" />
      </div>
      <div className="phone-shadow" />
    </div>
  );
}
