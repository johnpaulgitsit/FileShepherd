import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";


import LandingPage from "./pages/landing";
import Login from "./pages/login";
import Register from "./pages/register";
import Dashboard from "./pages/dashboard";
import AdminDashboard from "./pages/admin-dashboard";
import FileUpload from "./pages/fileupload";
import FileView from "./pages/file-view";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/fileupload" element={<FileUpload />} />
        <Route path="/file-view" element={<FileView />} />
        <Route path="/admin-dashboard" element={<AdminDashboard/>} />
      </Routes>
    </Router>
  );
}

export default App;



