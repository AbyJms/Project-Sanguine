# 🩸 Project Sanguine

*A Blood Donation & Request Management System*

---

## 🚀 Overview

Project Sanguine is a full-stack web application designed to connect **blood donors, hospitals, and administrators** efficiently.
It enables users to register, request blood, and manage donations through a structured and secure system.

---

## ⚙️ Features

### 👤 User Roles

* **Client**

  * Register & login
  * Select district & city
  * Request blood

* **Hospital**

  * Manage blood requests
  * Accept / fulfill requests

* **Admin**

  * Manage users & hospitals
  * Monitor system activity

* **👑 Head Admin (SysAdmin)**

  * Create/delete other admins
  * Full system control

---

## 🧠 Key Highlights

* 🔗 Relational database design (PostgreSQL)
* 🔐 Role-based access control
* 🧩 Modular backend (Node.js + Express)
* 🌐 Deployed on Vercel
* 🏙️ Dynamic city/district selection system
* 🛡️ Secure admin hierarchy (single head admin enforced)

---

## 🏗️ Tech Stack

| Layer      | Technology            |
| ---------- | --------------------- |
| Frontend   | HTML, CSS, JavaScript |
| Backend    | Node.js, Express      |
| Database   | PostgreSQL (Supabase) |
| Deployment | Vercel                |

---

## 🗄️ Database Design

Core tables:

* `users` → authentication
* `client` → donor data
* `hospital` → hospital data
* `admin` → admin control
* `cities` → location mapping
* `request` → blood requests

---

## 🔐 Authentication & Authorization

* JWT-based authentication
* Role-based middleware
* Head Admin restriction enforced at DB + backend

---

## 🌍 Live Demo

👉 https://projectsanguine.vercel.app

---

## ⚡ Getting Started

```bash
# Clone repo
git clone https://github.com/AbyJms/Project-Sanguine.git

# Install dependencies in backend folder
npm install

# Run server
node server.js
```

---

## 🧪 Environment Variables

Create a `.env` file:

```env
DB_HOST=your_host
DB_USER=your_user
DB_PASSWORD=your_password
DB_NAME=your_db
JWT_SECRET=your_secret
```

---

## 🔮 Future Improvements

* Password hashing (bcrypt) 🔐
* Email verification 📧
* Real-time request tracking ⚡
* Admin dashboard UI 📊

---

## ⭐ Final Note

This project demonstrates:

* strong database fundamentals
* backend logic & API design
* real-world system architecture

---
