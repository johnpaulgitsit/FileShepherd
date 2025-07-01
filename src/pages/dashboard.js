import React from "react";
import { useNavigate } from "react-router-dom";
import "../styles/dashboard.css";

function Dashboard() {
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/auth/logout", {
        method: "POST",
        credentials: "include",
      });

      if (!response.ok) throw new Error("Logout failed");

      navigate("/");
    } catch (err) {
      console.error("Error logging out:", err);
    }
  };

  return (
    <div className="dashboard-wrapper">
      {/* Top Navbar */}
      <header className="dashboard-header">
        <div className="logo">SecureCloud</div>
        <button onClick={handleLogout} className="logout-button">
          Logout
        </button>
      </header>

      {/* Sidebar + Main Content */}
      <div className="dashboard-main">
        <aside className="dashboard-sidebar">
          <button onClick={() => navigate("/dashboard")}>Home</button>
          <button onClick={() => navigate("/fileupload")}>Upload Files</button>
          <button onClick={() => navigate("/files")}>My Files</button>
        </aside>

        <main className="dashboard-content">
          <h2 className="dashboard-title">Welcome to Your Dashboard</h2>
          <p className="dashboard-subtitle">Manage your files easily from here.</p>
          <button className="dashboard-button" onClick={() => navigate("/fileupload")}>
            Upload Files
          </button>
        </main>
      </div>
    </div>
  );
}

export default Dashboard;


