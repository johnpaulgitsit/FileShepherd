import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import "../styles/login.css"; 

function Login() {
  const [formData, setFormData] = useState({ username: "", password: "", email: "" });
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleLogin = async (e) => {
    e.preventDefault();

    try {
      const response = await fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(formData),
      });

      if (!response.ok) {
        const text = await response.text();
        setError(text);
        return;
      }

      setError("");
      navigate("/dashboard");
    } catch (err) {
      setError("Login failed: " + err.message);
    }
  };

  return (
    <div className="login-container">
  <h2 className="login-title">Login to FileShepherd</h2>
  {error && <p className="error">{error}</p>}
  <form className="login-form" onSubmit={handleLogin}>
    <input
      className="login-input"
      name="username"
      placeholder="Username"
      value={formData.username}
      onChange={handleChange}
      required
    />
    <input
      className="login-input"
      name="email"
      type="email"
      placeholder="Email"
      value={formData.email}
      onChange={handleChange}
      required
    />
    <input
      className="login-input"
      name="password"
      type="password"
      placeholder="Password"
      value={formData.password}
      onChange={handleChange}
      required
    />
    <button type="submit">Log In</button>
  </form>
  <p>
    Don't have an account? <Link to="/register">Register here</Link>
  </p>
  
</div>

  );
}

export default Login;

