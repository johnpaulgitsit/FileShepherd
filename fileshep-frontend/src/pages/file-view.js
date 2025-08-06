import React, { useEffect, useState } from 'react';
import '../styles/file-view.css';

function FileList() {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [username, setUsername] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    const checkUserSession = async () => {
      try {
        const response = await fetch(`http://localhost:8080/api/auth/session?t=${Date.now()}`, {
          credentials: 'include',
          cache: 'no-cache' 
        });
        
        if (!response.ok) {
          const errorData = await response.json();
          throw new Error(errorData.error || 'Failed to get user');
        }
        
        const data = await response.json();
        
        if (!data.enabled) {
          throw new Error('Account disabled: Your account has been disabled by an administrator');
        }
        if (data.locked) {
          throw new Error('Account locked: Your account is locked');
        }
        
        setUsername(data.username);
      } catch (err) {
        console.error(err);
        setError(err.message || 'Failed to get user session');
        setLoading(false);
      }
    };
  
    checkUserSession();
  }, []);
  
  useEffect(() => {
    if (!username) return;
    
    const fetchFiles = async () => {
      try {

        const sessionCheck = await fetch(`http://localhost:8080/api/auth/session?t=${Date.now()}`, {
          credentials: 'include',
          cache: 'no-cache'
        });
        
        if (!sessionCheck.ok) {
          throw new Error('Session expired');
        }
        
        const sessionData = await sessionCheck.json();
        if (!sessionData.enabled) {
          throw new Error('Your account has been disabled. Please contact an administrator.');
        }
        if (sessionData.locked) {
          throw new Error('Your account has been locked. Please contact an administrator.');
        }
        
        const filesResponse = await fetch(`http://localhost:8080/files/list?username=${username}`, {
          credentials: 'include'
        });
        
        if (!filesResponse.ok) throw new Error('Failed to get files');
        
        const data = await filesResponse.json();
        setFiles(data);
        
      } catch (err) {
        console.error(err);
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
              {files.map((url, index) => {
                const fileName = url.split('/').pop();
                return (
                  <li key={index} className="file-view-item">
                    <a
                      href={url}
                      className="file-view-link"
                      download
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      {fileName}
                    </a>
                  </li>
                );
              })}
            </ul>
          )}
        </>
      )}
    </div>
  );
}

export default FileList;




