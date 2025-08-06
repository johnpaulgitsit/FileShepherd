import React, { useEffect, useState } from "react";

const Files = () => {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch("http://localhost:8080/files/my-files", { 
      credentials: "include" 
    })
      .then(async (res) => {
        // Check if response is ok first
        if (!res.ok) {
          throw new Error(`HTTP error! status: ${res.status}`);
        }
        
        const text = await res.text();
        console.log("Response text:", text);
        
        // Check if response is actually JSON
        const contentType = res.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json')) {
          throw new Error(`Expected JSON but got: ${contentType}`);
        }
        
        try {
          return JSON.parse(text);
        } catch {
          throw new Error("Response is not valid JSON");
        }
      })
      .then((data) => {
        setFiles(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error fetching files:", err.message);
        setError(err.message);
        setLoading(false);
      });
  }, []);

  if (loading) return <p>Loading your files...</p>;
  if (error) return <p style={{ color: "red" }}>Error: {error}</p>;
  if (files.length === 0) return <p>No files found.</p>;

  return (
    <div style={{ padding: "20px" }}>
      <h2>Your Uploaded Files</h2>
      <ul>
        {files.map((file, index) => (
          <li key={index}>
            <a href={file.url} target="_blank" rel="noopener noreferrer">
              {file.filename}
            </a>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default Files;



