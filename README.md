# Car Rental Graduation Project

This repository contains a full-stack car rental system based on:
- Backend: `Java 17 + Spring Boot + Spring Security + MyBatis-Plus + MySQL`
- Frontend: `Vue3 + Vite + TypeScript + Element Plus`

## 1. Project Structure

```text
lfz/
  backend/     Spring Boot API service
  frontend/    Vue3 web application (user + admin pages)
```

## 2. Implemented Features

### User Side
- Register / Login (JWT)
- Browse available cars
- Create rental order
- Pay order
- Cancel unpaid order
- View my orders

### Admin Side
- Create / edit / status update for cars
- Query all orders
- Confirm pickup
- Confirm return and settle extra fees

## 3. Backend Setup

## 3.1 Prerequisites
- JDK 17
- MySQL 8.x
- Maven 3.9+

## 3.2 Database
Create database first:

```sql
CREATE DATABASE car_rental DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

Then update credentials in:
- `backend/src/main/resources/application.yml`

Default config:
- host: `localhost:3306`
- username: `root`
- password: `123456`

`schema.sql` is auto executed on startup.

## 3.3 Run Backend

```bash
cd backend
mvn spring-boot:run
```

Default backend URL:
- `http://localhost:8080`

Swagger:
- `http://localhost:8080/swagger-ui/index.html`

### Default Admin Account
- username: `admin`
- password: `admin123`

The admin account and sample cars are auto initialized at startup if missing.

## 4. Frontend Setup

## 4.1 Prerequisites
- Node.js 20+

## 4.2 Install and Run

```bash
cd frontend
npm install
npm run dev
```

Default frontend URL:
- `http://localhost:5173`

Vite proxy is configured:
- `/api/* -> http://localhost:8080`

## 5. Main API Prefix

- `/api/v1/auth/*`
- `/api/v1/cars/*`
- `/api/v1/orders/*`
- `/api/v1/admin/cars/*`
- `/api/v1/admin/orders/*`

## 6. Status Codes

Order status:
- `10` Pending Pay
- `20` Pending Pickup
- `30` Renting
- `50` Finished
- `90` Canceled

Car status:
- `1` Available
- `2` Maintaining
- `3` Offline

## 7. UTF-8 Encoding (Important)

- Repository-level encoding is enforced by `.editorconfig` with `charset = utf-8`.
- Backend:
  - Maven compile/resources encoding is set to `UTF-8` in `backend/pom.xml`.
  - HTTP response/request encoding is forced to UTF-8 in `backend/src/main/resources/application.yml`.
- Frontend:
  - `frontend/index.html` uses `<meta charset="UTF-8" />`.

For IntelliJ IDEA, also ensure:
- `Settings -> Editor -> File Encodings -> Global/Project = UTF-8`
