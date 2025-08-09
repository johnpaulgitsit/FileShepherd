import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/file-view.css';

function FileList() {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [username, setUsername] = useState(null);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const checkUserSession = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/auth/session", {
          credentials: 'include',
          cache: 'no-cache'
        });

        if (!response.ok) throw new Error('Session expired or unauthorized');

        const data = await response.json();
        if (!data.username) throw new Error('Invalid session response');
        setUsername(data.username);
      } catch (err) {
        console.error("Session check failed:", err);
        setError(err.message || 'Failed to get user session');
        navigate("/");
      }
    };

    checkUserSession();
  }, [navigate]);

  useEffect(() => {
    if (!username) return;

    const fetchFiles = async () => {
      try {
        const filesResponse = await fetch("http://localhost:8080/files/my-files", {
          credentials: 'include'
        });

        const data = await filesResponse.json();

        if (!filesResponse.ok) {
          throw new Error(data.message || 'Failed to fetch files');
        }

        if (!Array.isArray(data)) {
          throw new Error('Unexpected response format');
        }

        setFiles(data);
      } catch (err) {
        console.error("File fetch failed:", err);
        setError(err.message || 'Failed to load files');
      } finally {
        setLoading(false);
      }
    };

    fetchFiles();
  }, [username]);

  return (
    <div className="file-view-container">
      {loading && <p className="file-view-message">Loading...</p>}
      {error && <p className="file-view-error">{error}</p>}
      {!loading && !error && (
        <>
          <h2 className="file-view-heading">Files for <strong>{username}</strong></h2>
          {files.length === 0 ? (
            <p className="file-view-message">No files uploaded yet.</p>
          ) : (
            <ul className="file-view-list">
              {files.map((file, index) => (
                <li key={index} className="file-view-item">
                  <a
                    href={file.url}
                    className="file-view-link"
                    download
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    {file.filename.replace(/^[^_]+_/, '')}
                  </a>
                </li>
              ))}
            </ul>
          )}
        </>
      )}
    </div>
  );
}

export default FileList;



