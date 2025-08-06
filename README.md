# File Shepherd

## Overview

**File Shepherd** is a full-stack web application for secure file transfer and management in a cloud-based environment. Designed with an emphasis on backend architecture and security best practices, the app enables users to upload files into dedicated cloud folders, with access managed through session-based authentication and role-based permissions.

This project was built while learning more about file security, authentication systems, and scalable backend structures. File Shepherd takes a modular, extensible approach to secure file handling and account management.

---

## Tech Stack

- **Frontend**: React  
- **Backend**: Spring Boot (REST APIs)  
- **Database**: MySQL  
- **Cloud Storage**: Google Cloud Storage  
- **Security**: Session-based authentication, hashed passwords, RBAC

---

## Key Features

- Secure login using hashed credentials stored in SQL  
- Role-based access control 
- Session-based authentication with protected endpoints  
- Google Cloud Storage integration for user-specific file management  
- Admin control panel to lock or enable user accounts  
- CORS and security configuration to protect API access

---

## Project Goals

This project was an opportunity to move beyond tutorials and build a real, working system with security and architectural decisions I had to reason through myself. I focused on:

- Understanding session management and backend authentication  
- Designing a REST API that balances usability and security  
- Structuring a scalable file-handling system with cloud storage  
- Applying practices around password hashing and access control

---

## What’s Next

Planned future features:

- File encryption at rest and in transit  
- Two-factor authentication  
- Download/access logging  
- UI/UX improvements  

---

## Setup Instructions

### Prerequisites

- Java 17+  
- MySQL  
- Google Cloud credentials  
  
### Backend (Spring Boot)

1. Clone the repository  
2. Set your environment variables or `application.properties` for:
   - Database URL, username, password
   - Google Cloud bucket and service account key  
3. Run the backend:
   ```bash
   ./gradlew bootRun

