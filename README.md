# Farmer Market Direct App - Backend

## 📖 Description
This is the backend service for the Farmer Market Direct App.  
It provides REST APIs for managing users, products, and orders, and uses JWT-based authentication for secure access.

---

## 🛠️ Tech Stack
- Java 17 (JDK 17)
- Spring Boot
- Spring Security
- JWT Authentication
- MySQL Database
- Maven

---

## ✨ Features
- User Registration & Login
- JWT Token-based Authentication & Authorization
- Role-based access (Admin / Farmer / User)
- Product Management APIs
- Order Management System
- Secure REST APIs

---

## 🔐 Authentication
This project uses **JWT (JSON Web Token)** for securing APIs.

- After login, a JWT token is generated
- The token must be included in headers:
  
Authorization: Bearer <your_token>

---

## 📂 Project Structure
/src
 ├── controller   → REST Controllers
 ├── service      → Business Logic
 ├── repository   → Database Layer
 ├── model        → Entity Classes
 ├── security     → JWT & Security Config
 └── dto          → Data Transfer Objects

---

## ⚙️ Setup Instructions

### 1. Clone Repository
git clone https://github.com/aarogya23/Farmer-Market-Direct-App.git

### 2. Navigate to Backend
cd server

### 3. Configure Database
Update `application.properties`:

spring.datasource.url=jdbc:mysql://localhost:3306/farmer_db  
spring.datasource.username=root  
spring.datasource.password=yourpassword  

---

### 4. Run Application
./mvnw spring-boot:run

OR (Windows)
mvn spring-boot:run

---

## 🔌 API Sample

### Login
POST /api/auth/login

### Get Products
GET /api/products

### Add Product (Protected)
POST /api/products

---

## 🧠 Future Improvements
- Payment Integration
- Real-time notifications
- Image upload support
- Deployment (Docker + Cloud)

---

## 🤝 Contributing
Feel free to fork and contribute to this project.

---
