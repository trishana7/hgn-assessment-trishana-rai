# Architectural and Technical Decisions

This document details the reasoning, architectural patterns, and design choices made in developing the SOS Alert Service.

---

## 1. Database Schema Decisions

We designed a relational schema consisting of 5 main tables to maximize isolation and represent relationships cleanly.

### `device`
* **Purpose**: Tracks active hardware tracking devices (e.g. Garmin, Spot) rather than tying a hardware device ID directly to a specific trek booking.
* **Fields**: `id`, `device_id` (Unique business key e.g. `"GPS-001"`), `name`, `status`.

### `trek_order`
* **Purpose**: Represents a trekking booking (avoiding the reserved SQL keyword `order`).
* **Fields**: `id`, `order_number` (Unique business key e.g. `"TREK001"`), `start_date`, `end_date`, `status`.

### `group_member`
* **Purpose**: Represents trekkers. We store them separately because multiple trekkers belong to a single order. Tying alerts directly to individual trekkers is unstable since multiple individuals share the same tracking device. 
* **Relationship**: Many-to-One with `trek_order`.

### `device_order` (The Mapping Table)
* **Purpose**: Solves Requirement #2 (One device shared across many orders over time). Instead of putting `device_id` inside `trek_order` (which limits a device to only one booking), this mapping table stores assignments with `assigned_from` and `assigned_to` timestamp windows.
* **Fields**: `id`, `device_id`, `order_id`, `assigned_from`, `assigned_to`.
* **SOS Date Lookup**: When an SOS is received with a `timestamp`, we resolve the order by querying:
  `assigned_from <= timestamp <= assigned_to` for that device.

### `alert`
* **Purpose**: Stores the generated SOS signals.
* **Fields**: `id`, `device_id`, `order_id`, `latitude`, `longitude`, `timestamp`, `status` (`NEW`, `CLAIMED`, `ESCALATED`), `claimed_by` (coordinator username), `urgent`.
* **Design Decision**: By linking the Alert to both the `Device` and the resolved `Order`, we preserve both which physical device triggered the distress signal and the list of trekkers in that trekking group.

---

## 2. Deduplication Window Choice

Satellite-based rescue beacons and tracking devices (e.g. Garmin inReach) are configured to automatically retransmit the SOS signal at short intervals (typically every 1 to 5 minutes) to ensure delivery through dense tree cover or bad weather. 
* **Choice**: **2-minute deduplication window**.
* **Reasoning**: If a user is panicking or the device automatically retries, receiving multiple identical signals within 2 minutes is expected. Creating separate alert records for each retransmission would clutter the dashboard and lead to coordination confusion.
* **Deduplication Check**: We check for alerts on the same device within the time frame of `[timestamp - 2 mins, timestamp + 2 mins]` that have **not** been claimed yet (i.e. status is not `"CLAIMED"`). If an alert exists, we return it to prevent duplicate notifications.

---

## 3. Concurrency and Row Locking

* **Choice**: **Pessimistic Locking (`PESSIMISTIC_WRITE`)**.
* **Reasoning**: If two emergency coordinators, Alice and Bob, simultaneously see a critical SOS and click "Claim", we must guarantee that only one coordinator successfully claims it.
  * Optimistic locking (using `@Version`) checks version state during commit. If there is a collision, it throws an `OptimisticLockingFailureException` during flushing, which requires catching and translating at the application layer.
  * Pessimistic locking (using `SELECT ... FOR UPDATE`) is safer and easier to explain for critical safety services. It blocks the second transaction immediately at the database level. 
* **Process Flow**:
  1. Transaction begins.
  2. The application executes `SELECT * FROM alert WHERE id = :id FOR UPDATE` (using Spring Data's `@Lock(LockModeType.PESSIMISTIC_WRITE)`).
  3. Coordinator A acquires the row lock.
  4. Coordinator B's query blocks, waiting for Coordinator A's transaction to finish.
  5. Coordinator A updates status to `"CLAIMED"`, sets `claimedBy = "Alice"`, and commits the transaction, releasing the lock.
  6. Coordinator B's query unblocks and reads the updated record.
  7. Coordinator B's code checks: `if ("CLAIMED".equals(alert.getStatus())) throw new BusinessException(..., HttpStatus.CONFLICT)`.
  8. Coordinator B receives a clear Conflict (HTTP 409) response.

---

## 4. Alert Escalation parameters

* **Choice**: **10-minute timeout, checked every minute**.
* **Reasoning**: When an SOS is triggered, rescue teams must respond immediately. If an alert remains in the `"NEW"` status for more than 10 minutes without being claimed, it indicates that either no coordinator is active, or the request has been missed. Automatically upgrading the status to `"ESCALATED"` and setting `urgent = true` flags the alert for emergency rescue dispatch.
* **Implementation**: A Spring Boot scheduler runs every 60 seconds (`@Scheduled(fixedRate = 60000)`), querying the database for alerts where `status = 'NEW'` and `timestamp < (now - 10 minutes)`, updating them in a transaction.

---

## 5. Assumptions Made

1. **Ordering / Assignment Constraints**: We assume that a device cannot be assigned to overlapping time ranges in `device_order` mapping. (e.g. Device `"GPS-001"` cannot be assigned to `"TREK001"` and `"TREK002"` at the exact same moment).
2. **Missing Active Booking**: If an SOS is received from a valid device but no active booking (Order) matches the SOS timestamp, we treat it as an validation failure and return a `400 Bad Request` with a helpful description, rather than silently ignoring or saving a orphan alert.
3. **No Authentication**: In line with keeping it simple, user authentication is omitted. The coordinator's name is passed explicitly in the claim body (`{"coordinator": "John"}`).

---

## 6. Project Lombok Integration

* **Choice**: Integrated **Project Lombok**.
* **Reasoning**: We refactored all JPA entity models and Data Transfer Objects (DTOs) to use Lombok annotations (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`). This eliminates boilerplate getter/setter methods, default constructors, and constructor assignments. It significantly improves codebase readability and maintainability while keeping runtime performance completely unaffected since Lombok runs at compilation time.
