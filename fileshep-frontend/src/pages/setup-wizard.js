import React, { useState, useEffect, useCallback } from 'react';
import '../styles/setup-wizard.css';

const FileShepherdWizard = () => {
  const [currentStep, setCurrentStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  
  // Step completion state
  const [steps, setSteps] = useState([
    { id: 1, title: 'Welcome', description: 'Get started with FileShepherd setup', completed: false },
    { id: 2, title: 'GCP Key Upload', description: 'Upload and verify Google Cloud credentials', completed: false },
    { id: 3, title: 'Database Connection', description: 'Test your database connection', completed: false },
    { id: 4, title: 'Save Configuration', description: 'Save your settings', completed: false },
    { id: 5, title: 'Summary', description: 'Review setup completion', completed: false }
  ]);

  // Form data
  const [gcpFile, setGcpFile] = useState(null);
  const [dbConfig, setDbConfig] = useState({
    host: 'localhost',
    port: '3306',
    dbName: '',
    username: '',
    password: '',
    bucket: ''
  });

  const [welcomeData, setWelcomeData] = useState(null);
  const [summaryData, setSummaryData] = useState(null);
  const [statusData, setStatusData] = useState(null);

  const finishSetup = () => {
    window.location.reload();
  };
  

  const API_BASE = 'http://localhost:8081/api/wizard';

  const clearMessages = () => {
    setError(null);
    setSuccess(null);
  };
  

  const updateStepCompletion = useCallback((stepId, completed) => {
    setSteps(prev => prev.map(step => 
      step.id === stepId ? { ...step, completed } : step
    ));
  }, []);

  const fetchWelcome = useCallback(async () => {
    try {
      setLoading(true);
      clearMessages();
      const response = await fetch(`${API_BASE}/welcome`);
      const data = await response.json();
      setWelcomeData(data);
      updateStepCompletion(1, true);
    } catch (err) {
      setError('Failed to load welcome data');
    } finally {
      setLoading(false);
    }
  }, [updateStepCompletion]);

  const fetchSummary = useCallback(async () => {
    try {
      setLoading(true);
      clearMessages();
      const response = await fetch(`${API_BASE}/summary`);
      const data = await response.json();
      setSummaryData(data);
      updateStepCompletion(5, true);
    } catch (err) {
      setError('Failed to load summary');
    } finally {
      setLoading(false);
    }
  }, [updateStepCompletion]);

  const fetchStatus = useCallback(async () => {
    try {
      const response = await fetch(`${API_BASE}/status`);
      const data = await response.json();
      setStatusData(data);
      
      // Update step completion based on status
      if (data.gcpKeyExists) {
        updateStepCompletion(2, true);
      }
      if (data.wizardComplete) {
        updateStepCompletion(3, true);
        updateStepCompletion(4, true);
        updateStepCompletion(5, true);
      }
    } catch (err) {
      console.error('Failed to fetch status:', err);
    }
  }, [updateStepCompletion]);

  useEffect(() => {
    if (currentStep === 1) {
      fetchWelcome();
      fetchStatus(); // Check initial status
    } else if (currentStep === 5) {
      fetchSummary();
    }
  }, [currentStep, fetchWelcome, fetchStatus, fetchSummary]);

  // Check status on component mount to see if wizard is already complete
  useEffect(() => {
    fetchStatus();
  }, [fetchStatus]);

  const uploadGcpKey = async () => {
    if (!gcpFile) {
      setError('Please select a GCP key file');
      return;
    }

    try {
      setLoading(true);
      clearMessages();
      
      const formData = new FormData();
      formData.append('file', gcpFile);

      const response = await fetch(`${API_BASE}/upload-key`, {
        method: 'POST',
        body: formData
      });

      const data = await response.json();
      
      if (data.success) {
        setSuccess(data.message);
        updateStepCompletion(2, true);
        // Refresh status after successful upload
        await fetchStatus();
        setTimeout(() => setCurrentStep(3), 1500);
      } else {
        setError(data.error || 'Failed to upload GCP key');
      }
    } catch (err) {
      setError('Network error while uploading GCP key');
    } finally {
      setLoading(false);
    }
  };

  const testDbConnection = async () => {
    if (!dbConfig.host || !dbConfig.dbName || !dbConfig.username) {
      setError('Please fill in all required database fields');
      return;
    }

    try {
      setLoading(true);
      clearMessages();

      const response = await fetch(`${API_BASE}/db-test`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(dbConfig)
      });

      const data = await response.json();
      
      if (data.success) {
        setSuccess(data.message);
        updateStepCompletion(3, true);
        setTimeout(() => setCurrentStep(4), 1500);
      } else {
        setError(data.error || 'Database connection failed');
      }
    } catch (err) {
      setError('Network error while testing database connection');
    } finally {
      setLoading(false);
    }
  };

  const saveConfiguration = async () => {
    if (!dbConfig.bucket) {
      setError('Please specify a GCP bucket name');
      return;
    }

    try {
      setLoading(true);
      clearMessages();

      const response = await fetch(`${API_BASE}/save-config`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(dbConfig)
      });

      const data = await response.json();
      
      if (data.success) {
        setSuccess(data.message);
        updateStepCompletion(4, true);
        // Refresh status after successful save
        await fetchStatus();
        setTimeout(() => setCurrentStep(5), 1500);
      } else {
        setError(data.error || 'Failed to save configuration');
      }
    } catch (err) {
      setError('Network error while saving configuration');
    } finally {
      setLoading(false);
    }
  };

  const renderStepContent = () => {
    switch (currentStep) {
      case 1:
  return (
    <div className="setup-step">
      <div className="setup-header">
        <div className="setup-icon icon-blue">FS</div>
        <h2 className="setup-title">Welcome to FileShepherd Setup</h2>
        <p className="setup-description">
          This wizard will help you configure FileShepherd with your Google Cloud Platform credentials and database settings.
        </p>
      </div>

      {statusData && (
        <div className="setup-status">
          <h3 className="setup-subtitle">Current Status:</h3>
          <div className="setup-status-items">
            <div className="setup-status-item">
              <span>Wizard Complete:</span>
              <span className={`setup-status-value ${statusData.wizardComplete ? 'success' : 'error'}`}>
                {statusData.wizardComplete ? '✓ Yes' : '✗ No'}
              </span>
            </div>
            <div className="setup-status-item">
              <span>GCP Key Exists:</span>
              <span className={`setup-status-value ${statusData.gcpKeyExists ? 'success' : 'error'}`}>
                {statusData.gcpKeyExists ? '✓ Yes' : '✗ No'}
              </span>
            </div>
            {statusData.variables && (
              <div className="setup-env">
                <div className="setup-env-title">Environment Variables:</div>
                <div className="setup-env-grid">
                  {Object.entries(statusData.variables).map(([key, value]) => (
                    <div key={key} className="setup-env-item">
                      <span className="setup-env-key">{key}:</span>
                      <span className={`setup-env-value ${value ? 'success' : 'error'}`}>
                        {value ? '✓' : '✗'}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {welcomeData && (
        <div className="setup-steps">
          <h3 className="setup-subtitle">Setup Steps:</h3>
          <ol className="setup-steps-list">
            {welcomeData.steps?.map((step, index) => (
              <li key={index} className="setup-step-item">
                <span className="setup-step-number">{index + 1}</span>
                <span className="setup-step-text">{step}</span>
              </li>
            ))}
          </ol>
        </div>
      )}
    </div>
  );

case 2:
  return (
    <div className="setup-step">
      <div className="setup-header">
        <div className="setup-icon icon-green">↑</div>
        <h2 className="setup-title">Upload GCP Service Account Key</h2>
        <p className="setup-description">
          Upload your Google Cloud Platform service account JSON key file to enable cloud storage access.
        </p>
      </div>

      <div className="setup-file-upload">
        <input
          type="file"
          accept=".json"
          onChange={(e) => setGcpFile(e.target.files?.[0] || null)}
          className="file-input"
          id="gcp-file"
        />
        <label htmlFor="gcp-file" className="setup-file-label">
          <span>↑ Choose GCP Key File</span>
        </label>
        {gcpFile && (
          <p className="setup-file-selected">
            Selected: {gcpFile.name}
          </p>
        )}
      </div>

      <div className="footer-buttons">
        <button
          onClick={uploadGcpKey}
          disabled={!gcpFile || loading}
          className="footer-button primary"
        >
          {loading ? 'Uploading & Verifying...' : 'Upload & Verify GCP Key'}
        </button>
      </div>
    </div>
  );

case 3:
  return (
    <div className="setup-step">
      <div className="setup-header">
        <div className="setup-icon icon-purple">DB</div>
        <h2 className="setup-title">Database Configuration</h2>
        <p className="setup-description">
          Configure your MySQL database connection settings.
        </p>
      </div>

      <div className="setup-form-grid">
        <div className="input-group">
          <label className="input-label">Host</label>
          <input
            type="text"
            value={dbConfig.host}
            onChange={(e) => setDbConfig(prev => ({ ...prev, host: e.target.value }))}
            className="input-field"
            placeholder="localhost"
          />
        </div>
        <div className="input-group">
          <label className="input-label">Port</label>
          <input
            type="text"
            value={dbConfig.port}
            onChange={(e) => setDbConfig(prev => ({ ...prev, port: e.target.value }))}
            className="input-field"
            placeholder="3306"
          />
        </div>
        <div className="input-group">
          <label className="input-label">Database Name *</label>
          <input
            type="text"
            value={dbConfig.dbName}
            onChange={(e) => setDbConfig(prev => ({ ...prev, dbName: e.target.value }))}
            className="input-field"
            placeholder="fileshepherd"
          />
        </div>
        <div className="input-group">
          <label className="input-label">Username *</label>
          <input
            type="text"
            value={dbConfig.username}
            onChange={(e) => setDbConfig(prev => ({ ...prev, username: e.target.value }))}
            className="input-field"
            placeholder="root"
          />
        </div>
        <div className="input-group">
          <label className="input-label">Password</label>
          <input
            type="password"
            value={dbConfig.password}
            onChange={(e) => setDbConfig(prev => ({ ...prev, password: e.target.value }))}
            className="input-field"
            placeholder="Enter database password"
          />
        </div>
      </div>

      <div className="footer-buttons">
        <button
          onClick={testDbConnection}
          disabled={loading}
          className="footer-button primary"
        >
          {loading ? 'Testing Connection...' : 'Test Database Connection'}
        </button>
      </div>
    </div>
  );


  case 4:
    return (
      <div className="setup-step">
        <div className="setup-header">
          <div className="setup-icon icon-yellow">ENV</div>
          <h2 className="setup-title">Save Configuration</h2>
          <p className="setup-description">
            Enter your GCP bucket name where files will be stored.
          </p>
        </div>
  
        <div className="setup-form-grid">
          <div className="input-group">
            <label className="input-label">Bucket Name *</label>
            <input
              type="text"
              value={dbConfig.bucket}
              onChange={(e) =>
                setDbConfig((prev) => ({ ...prev, bucket: e.target.value }))
              }
              className="input-field"
              placeholder="your-bucket-name"
            />
          </div>
        </div>
  
        <div className="footer-buttons">
          <button
            onClick={saveConfiguration}
            disabled={loading}
            className="footer-button primary"
          >
            {loading ? 'Saving...' : 'Save Configuration'}
          </button>
        </div>
      </div>
    );
  

    case 5:
      return (
        <div className="setup-step">
          <div className="setup-header">
            <div className="setup-icon icon-teal">✔</div>
            <h2 className="setup-title">Setup Complete</h2>
            <p className="setup-description">
              FileShepherd is now configured and ready to use.
            </p>
          </div>
    
          {summaryData && (
            <div className="setup-summary">
              <h3 className="setup-subtitle">Summary:</h3>
              <ul className="setup-summary-list">
                {summaryData.summary?.map((line, idx) => (
                  <li key={idx} className="setup-summary-item">
                    {line}
                  </li>
                ))}
              </ul>
            </div>
          )}
    
          <div className="footer-buttons">
            <button
              onClick={finishSetup}
              className="footer-button primary"
            >
              Launch FileShepherd
            </button>
          </div>
        </div>
      );
    

      default:
        return null;
    }
  };
  
  
  return (
    <div className="wizard-container">
      <div className="wizard-content">
        {/* Progress Bar */}
        <div className="progress-section">
          <div className="progress-bar">
            {steps.map((step, index) => (
              <div key={step.id} className="progress-step">
                <div
                  className={`step-circle ${
                    step.completed
                      ? 'completed'
                      : currentStep === step.id
                      ? 'current'
                      : 'pending'
                  }`}
                >
                  {step.completed ? '✓' : step.id}
                </div>
                {index < steps.length - 1 && (
                  <div
                    className={`step-connector ${
                      step.completed ? 'completed' : 'pending'
                    }`}
                  />
                )}
              </div>
            ))}
          </div>
          <div className="step-info">
            <h1 className="step-title">
              Step {currentStep}: {steps.find(s => s.id === currentStep)?.title}
            </h1>
            <p className="step-description">
              {steps.find(s => s.id === currentStep)?.description}
            </p>
          </div>
        </div>

        {/* Main Content */}
        <div className="content-card">
          {/* Loading Animation */}
          {loading && (
            <div className="loading-overlay">
              <video className="loading-video" autoPlay loop muted>
                <source src="/images/shep1.mp4" type="video/mp4" />
                <div className="loading-fallback">
                  <div className="spinner"></div>
                </div>
              </video>
            </div>
          )}

          {/* Error/Success Messages */}
          {error && (
            <div className="message error-message">
              <span className="message-icon">!</span>
              <span className="message-text">{error}</span>
            </div>
          )}
          {success && (
            <div className="message success-message">
              <span className="message-icon">✓</span>
              <span className="message-text">{success}</span>
            </div>
          )}

          {/* Step Content */}
          {renderStepContent()}

          {/* Navigation */}
          <div className="navigation">
            <button
              onClick={() => {
                setCurrentStep(Math.max(1, currentStep - 1));
                clearMessages();
              }}
              disabled={currentStep === 1 || loading}
              className="nav-button prev-button"
            >
              <span>← Previous</span>
            </button>

            {currentStep < 5 && currentStep !== 2 && currentStep !== 3 && currentStep !== 4 && (
              <button
                onClick={() => {
                  setCurrentStep(Math.min(5, currentStep + 1));
                  clearMessages();
                }}
                disabled={loading}
                className="nav-button next-button"
              >
                <span>Next →</span>
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default FileShepherdWizard;
