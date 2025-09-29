import React, { useEffect, useState } from "react";
import { BrowserRouter as Router, Routes, Route, useNavigate } from "react-router-dom";
import axios from "axios";

import LandingPage from "./pages/landing";
import Login from "./pages/login";
import Register from "./pages/register";
import Dashboard from "./pages/dashboard";
import AdminDashboard from "./pages/admin-dashboard";
import FileUpload from "./pages/fileupload";
import FileView from "./pages/file-view";
import SetupWizard from "./pages/setup-wizard";
import Logo from "./components/logo";

function AppWrapper() {
  const [setupCompleted, setSetupCompleted] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const checkSetup = async () => {
      try {
        // First try backend
        const prodResponse = await axios.get("http://localhost:8080/api/auth/status");
        if (prodResponse.data.setupComplete) {
          setSetupCompleted(true);
          return;
        }
      } catch (err) {
        console.warn("Prod backend not available:", err.message);
      }

      try {
        // Fallback to wizard backend
        const wizardResponse = await axios.get("http://localhost:8081/api/wizard/status");
        if (wizardResponse.data.wizardComplete) {
          setSetupCompleted(true);
        } else {
          setSetupCompleted(false);
          navigate("/setup");
        }
      } catch (err) {
        console.error("Wizard backend not available:", err.message);
        setSetupCompleted(false);
        navigate("/setup");
      }
    };

    checkSetup();
  }, [navigate]);

  if (setupCompleted === null) return <p>Loading...</p>;

  return (
    <>
      <Logo />
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/fileupload" element={<FileUpload />} />
        <Route path="/file-view" element={<FileView />} />
        <Route path="/admin-dashboard" element={<AdminDashboard />} />
        <Route path="/setup" element={<SetupWizard />} />
      </Routes>
    </>
  );
}

function App() {
  return (
    <Router>
      <AppWrapper />
    </Router>
  );
}

export default App;
