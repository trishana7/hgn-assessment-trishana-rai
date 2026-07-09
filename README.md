# Trekking SOS Alert Service

A Spring Boot application that manages satellite SOS alerts. It processes distress signals, assigns them to active trekking bookings, deduplicates rapid retransmissions, manages concurrent claims using pessimistic locking, and automatically escalates unresolved alerts.

---

## Technical Stack
* **Java 21**
* **Spring Boot 4.x** (Starter JPA, Web, Scheduling)
* **PostgreSQL 15**
* **Springdoc OpenAPI (Swagger UI)**

## Running Locally for Development

If you have Java 21 installed and want to run it without Docker:

1. **Start PostgreSQL locally** (ensure database name is `sosdb`, username is `postgres`, password is `password`, port `5432`). The application connects to PostgreSQL by default.
2. **Run the Application**
   ```bash
   ./gradlew bootRun
   ```

---

## Seed Data

To make testing easy, the database is automatically seeded upon startup with the following mock data if empty:
* **Devices**:
  * `GPS-001` (Garmin)
  * `GPS-002` (Spot)
* **Orders**:
  * `TREK001` (July 1 - July 10, 2026). Group Members: *Alice Smith*, *Bob Jones*.
  * `TREK002` (July 15 - July 25, 2026). Group Members: *Charlie Brown*.
* **Assignments**:
  * `GPS-001` is assigned to `TREK001` for the duration of the trek.
  * `GPS-002` is assigned to `TREK002` for the duration of the trek.

---

## API Documentation & Swagger UI

Once the application is running, you can explore and interact with the APIs via the Swagger interface:
* **Swagger UI URL**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **JSON API Docs**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## REST Endpoints & Example Requests

### 1. Receive SOS Signal
* **Endpoint**: `POST /api/alerts`
* **Description**: Processes incoming SOS. Automatically looks up the active trekking booking for the device on the given date, check for duplicates within a 2-minute window, and saves a new alert if unique.
* **Curl Request**:
  ```bash
  curl -X POST http://localhost:8080/api/alerts \
    -H "Content-Type: application/json" \
    -d '{
      "deviceId": "GPS-001",
      "latitude": 27.72,
      "longitude": 85.32,
      "timestamp": "2026-07-08T10:00:00"
    }'
  ```
* **Response (200 OK)**:
  ```json
  {
    "id": 1,
    "deviceId": "GPS-001",
    "deviceName": "Garmin",
    "orderId": 1,
    "orderNumber": "TREK001",
    "latitude": 27.72,
    "longitude": 85.32,
    "timestamp": "2026-07-08T10:00:00",
    "status": "NEW",
    "claimedBy": null,
    "urgent": false,
    "groupMembers": [
      { "id": 1, "name": "Alice Smith" },
      { "id": 2, "name": "Bob Jones" }
    ]
  }
  ```

---

### 2. Claim Alert
* **Endpoint**: `POST /api/alerts/{id}/claim`
* **Description**: Locks the row in the database using pessimistic locking. A coordinator claims the alert. If another coordinator attempts to claim it concurrently, they will receive a conflict error.
* **Curl Request**:
  ```bash
  curl -X POST http://localhost:8080/api/alerts/1/claim \
    -H "Content-Type: application/json" \
    -d '{
      "coordinator": "John"
    }'
  ```
* **Response (200 OK)**:
  ```json
  {
    "id": 1,
    "deviceId": "GPS-001",
    "deviceName": "Garmin",
    "orderId": 1,
    "orderNumber": "TREK001",
    "latitude": 27.72,
    "longitude": 85.32,
    "timestamp": "2026-07-08T10:00:00",
    "status": "CLAIMED",
    "claimedBy": "John",
    "urgent": false,
    "groupMembers": [
      { "id": 1, "name": "Alice Smith" },
      { "id": 2, "name": "Bob Jones" }
    ]
  }
  ```

---

### 3. Get All Alerts
* **Endpoint**: `GET /api/alerts`
* **Curl Request**:
  ```bash
  curl -X GET http://localhost:8080/api/alerts
  ```

---

### 4. Get Alert by ID
* **Endpoint**: `GET /api/alerts/{id}`
* **Curl Request**:
  ```bash
  curl -X GET http://localhost:8080/api/alerts/1
  ```
