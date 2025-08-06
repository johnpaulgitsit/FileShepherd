import React from "react";
import { useNavigate } from "react-router-dom";
import "../styles/landing.css";

function LandingPage() {
  const navigate = useNavigate();

  const handleNavigate = (path, role) => {
    navigate(path, { state: { role } });
  };

  return (
    <div className="landing-wrapper">
      <div className="glow"></div>

      <div className="glass-box">
        <h1 className="headline">Welcome to FileShepherd</h1>
        <p className="subheading">Where easy submissions meet cloud.</p>

        <div className="button-group">
          <button className="button login-btn" onClick={() => handleNavigate("/login")}>Login</button>
          <button className="button register-btn" onClick={() => handleNavigate("/register", "REGISTER")}>Register</button>
        </div>
      </div>
    </div>
  );
}

export default LandingPage;





