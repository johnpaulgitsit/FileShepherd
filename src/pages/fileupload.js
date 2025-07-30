import React, { useState, useEffect } from "react";
import "../styles/fileupload.css";

function FileUpload() {
  const [file, setFile] = useState(null);
  const [message, setMessage] = useState("");
  const [userFiles, setUserFiles] = useState([]);

  // Handle file selection
  const handleFileChange = (event) => {
    setFile(event.target.files[0]);
  };

  // Upload file to backend
  const handleUpload = async () => {
    if (!file) {
      setMessage("Please select a file");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await fetch("http://localhost:8080/files/upload", {
        method: "POST",
        headers: {
          "Accept": "application/json",
          "Authorization": `Bearer ${localStorage.getItem("token")}`, // Ensure authentication
        },
        credentials: "include",
        body: formData,
      });

      if (!response.ok) {
        throw new Error(`Upload failed: ${response.status} - ${await response.text()}`);
      }

      setMessage(await response.text());
      setFile(null);
      fetchUserFiles(); // Refresh file list after upload
    } catch (err) {
      console.error("Upload failed:", err);
      setMessage("Upload failed");
    }
  };

  // Fetch user's uploaded files
  const fetchUserFiles = async () => {
    try {
      const response = await fetch("http://localhost:8080/files/my-files", {
        method: "GET",
        headers: {
          "Authorization": `Bearer ${localStorage.getItem("token")}`,
        },
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error(`Server error: ${response.status} - ${response.statusText}`);
      }

      const files = await response.json();
      setUserFiles(files.length ? files : []);
    } catch (err) {
      console.error("Error fetching files:", err);
      setUserFiles([]);
    }
  };

  useEffect(() => {
    fetchUserFiles();
  }, []);

  return (
    <div className="upload-container">
  <h2>Upload File</h2>
  <input type="file" onChange={handleFileChange} />
  <button className="upload-button" onClick={handleUpload}>Upload</button>
  <p>{message}</p>

  <h3>My Files</h3>
  <ul>
    {userFiles.map((file) => (
      <li key={file.id} className="file-item">
        {file.filename}
      </li>
    ))}
  </ul>
</div>

  );
}


export default FileUpload;


