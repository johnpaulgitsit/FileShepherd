import React from "react";
import { useNavigate } from "react-router-dom";
import "../styles/landing.css";

function LandingPage() {
  const navigate = useNavigate();

  return (
    <div className="landing-wrapper">
      {/* Background Glow */}
      <div className="glow"></div>

      {/* Glassmorphic Card */}
      <div className="glass-box">
        <h1 className="headline">Welcome to FileVault</h1>
        <p className="subheading">Where easy submissions meet cloud.</p>
        
        {/* Button Group */}
        <div className="button-group">
          <button className="button login-btn" onClick={() => navigate("/login")}>Login</button>
          <button className="button register-btn" onClick={() => navigate("/register")}>Register</button>
        </div>
      </div>
    </div>
  );
}

export default LandingPage;




