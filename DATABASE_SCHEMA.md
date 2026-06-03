# Database Entity And Table Map

Each module now includes `spring-boot-starter-data-jpa`, H2 runtime configuration, database-ready domain entities with `@Entity` and `@Table`, and `JpaRepository` interfaces beside the CoderPad-friendly in-memory repositories.

The services still use in-memory repositories for fast interview execution, but the entity mappings are real and Hibernate creates the tables during tests/local runs.

## Tables By Module

| Module | Entity | Table |
| --- | --- | --- |
| `file-upload-service` | `StoredUpload` | `stored_uploads` |
| `url-shortener-api` | `LinkRecord` | `short_links` |
| `notification-service` | `NotificationRecord` | `notifications` |
| `notification-service` | `DeliveryRecord` | `notification_deliveries` |
| `rate-limiter-api` | `RateLimitPolicy` | `rate_limit_policies` |
| `task-scheduler-api` | `ScheduledTask` | `scheduled_tasks` |
| `parking-lot-api` | `ParkingSpot` | `parking_spots` |
| `parking-lot-api` | `ParkingTicket` | `parking_tickets` |
| `meeting-room-scheduler-api` | `MeetingRoom` | `meeting_rooms` |
| `meeting-room-scheduler-api` | `MeetingRoom.features` | `meeting_room_features` |
| `meeting-room-scheduler-api` | `Booking` | `room_bookings` |
| `inventory-management-api` | `Product` | `products` |
| `inventory-management-api` | `Reservation` | `inventory_reservations` |
| `order-processing-api` | `CustomerOrder` | `customer_orders` |
| `customer-support-ticket-api` | `Agent` | `support_agents` |
| `customer-support-ticket-api` | `Agent.skills` | `support_agent_skills` |
| `customer-support-ticket-api` | `SupportTicket` | `support_tickets` |
| `customer-support-ticket-api` | `SupportTicket.comments` | `support_ticket_comments` |

## Why Some Objects Are Not Entities

- `DeliveryAttempt` is a delay-queue message, not durable notification state.
- `ScheduledWork` is a priority-queue work item, not the task record itself.
- `RateLimitDecision` is a response/value object.
- DTO records such as `OrderLine` and `ReserveLine` model API payloads. In production, line items can become child tables or JSON columns depending on consistency and query needs.

## Local Database

Every module uses an H2 in-memory database with `spring.jpa.hibernate.ddl-auto=create-drop`.

Example from a module:

```properties
spring.datasource.url=jdbc:h2:mem:url_shortener_api;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.open-in-view=false
spring.h2.console.enabled=true
```

For production, replace H2 with PostgreSQL/MySQL, set migrations with Flyway or Liquibase, and move from `create-drop` to versioned DDL.
