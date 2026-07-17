import PhoneMockup from "./PhoneMockup";
import "./LandingHero.css";

export default function LandingHero() {
  return (
    <section className="landing-hero">
      <div className="landing-hero-content">
        <h1>
          Learn Smarter with{" "}
          <span className="gradient-text">Academic AI</span>
        </h1>
        <p>
          Your intelligent study companion — practice exams, track progress,
          and achieve your academic goals.
        </p>
        <div className="landing-hero-buttons">
          <a href="/register" className="btn-primary">Get Started Free</a>
          <a href="/login" className="btn-secondary">Sign In</a>
        </div>
      </div>

      <div className="landing-hero-phone">
        <PhoneMockup>
          <div className="mock-screen">
            <div className="mock-header">
              <div className="mock-greeting">Welcome back 👋</div>
              <div className="mock-stats-row">
                <div className="mock-stat-card">
                  <span className="mock-stat-num">85%</span>
                  <span className="mock-stat-label">Avg Score</span>
                </div>
                <div className="mock-stat-card">
                  <span className="mock-stat-num">12</span>
                  <span className="mock-stat-label">Courses</span>
                </div>
              </div>
            </div>
            <div className="mock-body">
              <div className="mock-section-title">Recent Activity</div>
              <div className="mock-card">
                <div className="mock-card-dot mock-dot-green" />
                <div className="mock-card-text">
                  <div className="mock-card-title">Math 201 - Quiz 5</div>
                  <div className="mock-card-sub">Score: 92/100</div>
                </div>
              </div>
              <div className="mock-card">
                <div className="mock-card-dot mock-dot-blue" />
                <div className="mock-card-text">
                  <div className="mock-card-title">PHY 101 - Mock Exam</div>
                  <div className="mock-card-sub">In Progress</div>
                </div>
              </div>
              <div className="mock-card">
                <div className="mock-card-dot mock-dot-purple" />
                <div className="mock-card-text">
                  <div className="mock-card-title">CSC 301 - Assignment</div>
                  <div className="mock-card-sub">Due tomorrow</div>
                </div>
              </div>
            </div>
          </div>
        </PhoneMockup>
      </div>
    </section>
  );
}
