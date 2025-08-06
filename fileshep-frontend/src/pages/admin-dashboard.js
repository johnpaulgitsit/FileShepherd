import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/dashboard.css";

function AdminDashboard() {
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [accessDenied, setAccessDenied] = useState(false); 

  const fetchUsers = async () => {
    try {
      const response = await fetch("http://localhost:8080/admin/users/getUsers", {
        method: "GET",
        credentials: "include",
      });
      
      if (response.status === 403) {
        setAccessDenied(true);
        setLoading(false);
        return;
      }
      
      if (!response.ok) throw new Error("Failed to fetch users");
      const data = await response.json();
      setUsers(data);
      setLoading(false);
    } catch (err) {
      console.error("Error fetching users:", err);
      setLoading(false);
    }
  };

  const handleAction = async (id, action) => {
    try {
      const response = await fetch(`http://localhost:8080/admin/users/${id}/${action}`, {
        method: "PUT",
        credentials: "include",
      });
      if (!response.ok) throw new Error("Action failed");
      
      await fetchUsers();
      
      console.log(`User ${id} status changed to ${action}`);
      
    } catch (err) {
      console.error(`Error performing ${action} on user ${id}:`, err);
    }
  };

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

  useEffect(() => {
    fetchUsers();
  }, []);
  
  if (accessDenied) {
    return (
      <div className="dashboard-wrapper">
        <div className="dashboard-content">
          <h2>Access Denied</h2>
          <p>You don't have admin privileges.</p>
          <button onClick={() => navigate("/dashboard")}>Back to Dashboard</button>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-wrapper">
      <header className="dashboard-header">
        <div className="logo">FileShepherd Admin</div>
        <button onClick={handleLogout} className="logout-button">
          Logout
        </button>
      </header>
      <div className="dashboard-main">
        <aside className="dashboard-sidebar">
          <button onClick={() => navigate("/dashboard")}>User View</button>
        </aside>
        <main className="dashboard-content">
          <h2 className="dashboard-title">Admin User Management</h2>
          {loading ? (
            <p>Loading users...</p>
          ) : (
            <div className="table-section">
              <table className="user-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Username</th>
                    <th>Email</th>
                    <th>Enabled</th>
                    <th>Locked</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((user) => (
                    <tr key={user.id}>
                      <td>{user.id}</td>
                      <td>{user.username}</td>
                      <td>{user.email}</td>
                      <td>
                        <span className={`status-badge ${user.enabled ? 'enabled' : 'disabled'}`}>
                          {user.enabled ? "Yes" : "No"}
                        </span>
                      </td>
                      <td>
                        <span className={`status-badge ${user.locked ? 'locked' : 'unlocked'}`}>
                          {user.locked ? "Yes" : "No"}
                        </span>
                      </td>
                      <td>
                        <div className="action-buttons-table">
                          <button 
                            className="action-btn"
                            onClick={() => handleAction(user.id, user.locked ? "unlock" : "lock")}
                          >
                            {user.locked ? "Unlock" : "Lock"}
                          </button>
                          <button 
                            className="action-btn"
                            onClick={() => handleAction(user.id, user.enabled ? "disable" : "enable")}
                          >
                            {user.enabled ? "Disable" : "Enable"}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

export default AdminDashboard;

