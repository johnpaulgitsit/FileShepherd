import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/dashboard.css";

function Dashboard() {
  const navigate = useNavigate();
  const [isAdmin, setIsAdmin] = useState(false);
  const [username, setUsername] = useState("");
  const [activeTab, setActiveTab] = useState("dashboard");
  const [stats, setStats] = useState({
    totalFiles: 0,
    storageUsed: 0,
    recentUploads: 0,
    totalUsers: 0
  });

  useEffect(() => {
    fetch("http://localhost:8080/api/auth/session", {
      credentials: "include"
    })
      .then(res => {
        if (!res.ok) throw new Error("Unauthorized");
        return res.json();
      })
      .then(data => {
        setUsername(data.username);
        setIsAdmin(data.isAdmin || false);

        // Simulated stats-> will replace with real API call later
        setStats({
          totalFiles: 1247,
          storageUsed: 75.3,
          recentUploads: 23,
          totalUsers: 156
        });
      })
      .catch(err => {
        console.error("Session check failed:", err);
        navigate("/");
      });
  }, [navigate]);

  const handleLogout = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/auth/logout", {
        method: "POST",
        credentials: "include"
      });
      if (!response.ok) throw new Error("Logout failed");
      navigate("/");
    } catch (err) {
      console.error("Error logging out:", err);
    }
  };

  const handleNavigation = (path, tabName) => {
    setActiveTab(tabName);
    navigate(path);
  };

  return (
    <div className="dashboard-wrapper">
      <aside className="dashboard-sidebar">
        <div className="dashboard-header">
        </div>

        <div className="nav-buttons">
          <button
            className={activeTab === "dashboard" ? "active" : ""}
            onClick={() => handleNavigation("/dashboard", "dashboard")}
          >
            Dashboard
          </button>
          <button
            className={activeTab === "upload" ? "active" : ""}
            onClick={() => handleNavigation("/fileupload", "upload")}
          >
            Upload Files
          </button>
          <button
            className={activeTab === "files" ? "active" : ""}
            onClick={() => handleNavigation("/file-view", "files")}
          >
            My Files
          </button>
          {isAdmin && (
            <button
              className={activeTab === "admin" ? "active" : ""}
              onClick={() => handleNavigation("/admin-dashboard", "admin")}
            >
              Admin Panel
            </button>
          )}
        </div>

        <div className="logout-section">
          <button onClick={handleLogout} className="logout-button">
            Logout
          </button>
        </div>
      </aside>

      <main className="dashboard-content">
        <div className="dashboard-header-content">
          <h1 className="dashboard-title">Dashboard Overview</h1>
          <p className="dashboard-subtitle">
            Welcome back{username ? `, ${username}` : ""}! Here's what's happening with your files today.
          </p>
        </div>

        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-header">
              <div className="stat-title">Total Files</div>
              <div className="stat-icon">📁</div>
            </div>
            <div className="stat-value">{stats.totalFiles.toLocaleString()}</div>
            <div className="stat-change">+12% from last month</div>
          </div>

          <div className="stat-card">
            <div className="stat-header">
              <div className="stat-title">Storage Used</div>
              <div className="stat-icon">💾</div>
            </div>
            <div className="stat-value">{stats.storageUsed}%</div>
            <div className="stat-change">+5.2% from last week</div>
          </div>

          <div className="stat-card">
            <div className="stat-header">
              <div className="stat-title">Recent Uploads</div>
              <div className="stat-icon">⬆️</div>
            </div>
            <div className="stat-value">{stats.recentUploads}</div>
            <div className="stat-change">+8 today</div>
          </div>

          <div className="stat-card">
            <div className="stat-header">
              <div className="stat-title">Active Users</div>
              <div className="stat-icon">👥</div>
            </div>
            <div className="stat-value">{stats.totalUsers}</div>
            <div className="stat-change negative">-2.1% from last week</div>
          </div>
        </div>

        <div className="chart-section">
          <div className="chart-header">
            <h3 className="chart-title">File Upload Activity</h3>
            <select style={{
              background: '#1a1d29',
              color: '#8b92a5',
              border: '1px solid #2d3142',
              padding: '0.5rem',
              borderRadius: '6px'
            }}>
              <option>Last 7 days</option>
              <option>Last 30 days</option>
              <option>Last 3 months</option>
            </select>
          </div>
          <div className="chart-placeholder">
            📈 ---Chart visualization will go here---
            <br />
            <small style={{ marginTop: '0.5rem', display: 'block' }}>
              Possiblly Integrate with Chart.js or Recharts for actual data visualization
            </small>
          </div>
        </div>

        <div className="chart-section">
          <div className="chart-header">
            <h3 className="chart-title">Quick Actions</h3>
          </div>
          <div className="action-buttons">
            <button
              className="dashboard-button"
              onClick={() => navigate("/fileupload")}
            >
              Upload Files
            </button>
            <button
              className="dashboard-button secondary"
              onClick={() => navigate("/file-view")}
            >
              View All Files
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}

export default Dashboard;


