# Notification System – System Design Project

## 1. Project Overview

The Notification System is an asynchronous, event-driven application developed using Java, Spring Boot, PostgreSQL, RabbitMQ, and Docker.

It allows other applications or services to request notifications through REST APIs. Instead of sending notifications synchronously during the API request, the system stores the notification and processes it asynchronously using RabbitMQ.

The system currently supports three notification channels:

- Email
- SMS
- Push notification

Email delivery is integrated with Mailpit for local testing. SMS and push notification delivery are simulated.

### Main objectives

- Build an asynchronous notification processing system.
- Avoid losing notification requests when message publishing fails.
- Implement the Transactional Outbox Pattern.
- Support multiple notification channels.
- Implement idempotency to prevent duplicate requests.
- Implement message retry and Dead Letter Queues.
- Track notification processing status.
- Containerize the application using Docker Compose.

---

## 2. Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Programming language |
| Spring Boot 4.1.2 | Backend framework |
| Spring Web MVC | REST APIs |
| Spring Data JPA | Database operations |
| PostgreSQL 18.3 | Persistent storage |
| RabbitMQ 4 | Message broker |
| Spring AMQP | RabbitMQ integration |
| JavaMailSender | Email sending |
| Mailpit | Email testing |
| Jackson 3 | JSON serialization |
| Gradle | Build tool |
| Docker | Containerization |
| Docker Compose | Multi-container management |
| Spring Actuator | Health monitoring |

---

## 3. High-Level Architecture

```text
                    Client / Postman
                           |
                           |
                           v
                 Notification Controller
                           |
                           v
                  Notification Service
                           |
                 Transactional Operation
                           |
                 +---------+---------+
                 |                   |
                 v                   v
           Notification          Outbox Event
              Table                 Table
                 |                   |
                 +---------+---------+
                           |
                     PostgreSQL
                           |
                           v
                    Outbox Publisher
                           |
                           v
                      RabbitMQ
                    Direct Exchange
                 notification.exchange
                           |
             +-------------+-------------+
             |             |             |
             v             v             v
         Email Queue    SMS Queue    Push Queue
             |             |             |
             v             v             v
        Email Consumer SMS Consumer Push Consumer
             |             |             |
             v             v             v
         Mailpit       Simulated     Simulated
                        SMS Provider  Push Provider
             |
             v
       Notification Status
          Updated in DB
```

### Main components

1. Notification Controller – exposes REST endpoints.
2. Notification Service – handles notification creation and retrieval.
3. Notification Repository – communicates with PostgreSQL.
4. Outbox Event Repository – manages outbox records.
5. Outbox Publisher – publishes pending outbox events to RabbitMQ.
6. RabbitMQ – distributes messages to channel-specific queues.
7. Consumers – process email, SMS, and push notifications.
8. Mailpit – captures outgoing emails during development.

---

## 4. Database Design

We implemented two main entities.

### 4.1 Notification Entity

Stores notification details and processing status.

Important fields:

- `id` – unique notification identifier (UUID).
- `sourceService` – application that requested the notification.
- `recipientId` – recipient identifier.
- `channel` – EMAIL, SMS, or PUSH.
- `destination` – email address or phone number.
- `eventType` – business event that triggered the notification.
- `templateCode` – notification template identifier.
- `idempotencyKey` – prevents duplicate requests.
- `payload` – notification data stored as JSON.
- `status` – current notification status.
- `createdAt` – creation timestamp.
- `updatedAt` – last update timestamp.

### 4.2 OutboxEvent Entity

Stores events that need to be published to RabbitMQ.

Important fields:

- `id` – unique outbox event identifier.
- `notificationId` – associated notification ID.
- `eventType` – event name, such as `NOTIFICATION_CREATED`.
- `payload` – serialized event payload.
- `status` – NEW or PUBLISHED.
- `createdAt` – event creation timestamp.

### 4.3 Relationship

A notification creation operation creates:

1. One Notification record.
2. One corresponding OutboxEvent record.

Both records are saved within the same database transaction.

This prevents a notification from being saved without its corresponding event.

---

## 5. Notification Status Lifecycle

Implemented notification statuses:

| Status | Meaning |
|---|---|
| PENDING | Notification request has been accepted. |
| PROCESSING | Consumer has started processing. |
| SENT | Provider has accepted the notification. |
| DELIVERED | Delivery has been confirmed by the provider. |
| RETRYING | Notification is waiting for another processing attempt. |
| FAILED | Notification processing has permanently failed. |

### Status flow

```text
PENDING
   |
   v
PROCESSING
   |
   +--------------------+
   |                    |
   v                    v
 SENT                 RETRYING
   |                    |
   v                    v
DELIVERED             PROCESSING
                        |
                        v
                    SENT / FAILED
```

Important distinction:

- SENT means the provider accepted the notification.
- DELIVERED means the provider confirmed successful delivery.
- For the current local implementation, Mailpit captures emails. It does not provide a real-world recipient delivery confirmation.

---

## 6. Notification Creation Flow

The client sends a POST request to the Notification API.

### Step 1: Receive request

NotificationController receives the request and calls NotificationService.

### Step 2: Idempotency check

The service checks whether a notification already exists using:

- sourceService
- idempotencyKey
- channel

If a matching notification exists, the existing notification ID and status are returned.

This prevents duplicate notification creation for repeated requests using the same idempotency key and source/channel combination.

### Step 3: Create notification

A new Notification entity is created with:

- Request details
- Serialized payload
- Initial status PENDING

### Step 4: Create outbox event

An OutboxEvent is created with:

- Associated notification ID
- Event type `NOTIFICATION_CREATED`
- Serialized event payload
- Initial status NEW

### Step 5: Save both records

Both entities are saved within the same transactional method.

```text
Notification Service
        |
        v
   @Transactional
        |
        +------------------+
        |                  |
        v                  v
 Notification         OutboxEvent
        |                  |
        +--------+---------+
                 |
                 v
             PostgreSQL
```

If the transaction fails, both database operations are rolled back.

---

## 7. Transactional Outbox Pattern

### Why did we implement it?

Consider the following scenario:

1. Notification is saved successfully in PostgreSQL.
2. RabbitMQ is temporarily unavailable.
3. Message publishing fails.

Without an outbox mechanism, the notification might remain saved without ever being published.

The Transactional Outbox Pattern solves this problem.

### Implementation

The notification and corresponding outbox event are saved in one database transaction.

A scheduled OutboxPublisher periodically retrieves NEW outbox events.

Its processing flow:

1. Fetch pending outbox events.
2. Read the event payload.
3. Publish the event to RabbitMQ.
4. Wait for publisher confirmation.
5. Check for returned messages.
6. Mark the outbox event as PUBLISHED after successful publishing.

```text
Outbox Table
     |
     v
Find NEW Events
     |
     v
Outbox Publisher
     |
     v
RabbitMQ Exchange
     |
     v
Publisher Confirmation
     |
     v
Update Outbox Status
     |
     v
   PUBLISHED
```

The outbox pattern prevents the database/message-broker dual-write problem.

Note: Publisher confirmation does not guarantee that a consumer has processed the message. Also, a crash after RabbitMQ accepts a message but before the outbox record is marked PUBLISHED can result in duplicate publishing. Consumers must therefore handle duplicate messages safely.

---

## 8. RabbitMQ Implementation

### Exchange

Exchange name:

`notification.exchange`

Exchange type:

`DIRECT`

### Routing keys

| Channel | Routing key |
|---|---|
| EMAIL | notification.email |
| SMS | notification.sms |
| PUSH | notification.push |

### Queues

| Queue | Purpose |
|---|---|
| notification.email.queue | Email processing |
| notification.sms.queue | SMS processing |
| notification.push.queue | Push notification processing |

### Message routing

```text
                  notification.exchange
                         DIRECT
                            |
            +---------------+---------------+
            |               |               |
            v               v               v
   notification.email  notification.sms  notification.push
            |               |               |
            v               v               v
   email.queue          sms.queue        push.queue
```

The direct exchange routes messages according to their routing keys.

---

## 9. Consumer Implementation

We implemented separate consumers for each notification channel.

### Email Consumer

Responsibilities:

1. Receive the message from the email queue.
2. Read notification details.
3. Check whether the notification is already SENT.
4. Update status to PROCESSING.
5. Send email using JavaMailSender.
6. Update status to SENT after successful provider acceptance.

Mailpit is used as the local SMTP server.

### SMS Consumer

Responsibilities:

1. Receive the SMS message.
2. Update processing status.
3. Simulate SMS provider processing.
4. Update the notification status.

### Push Consumer

Responsibilities:

1. Receive the push message.
2. Update processing status.
3. Simulate push provider processing.
4. Update the notification status.

SMS and push are simulated, not connected to external providers.

### Duplicate processing protection

Consumers check whether the notification is already SENT before processing it again.

This is a basic duplicate-processing safeguard. It does not provide complete exactly-once processing, particularly when multiple consumers process the same notification concurrently.

---

## 10. Retry Mechanism and Dead Letter Queues

We configured retry-related processing using Spring AMQP.

### Dead Letter Exchange

`notification.dlx`

### Dead Letter Queues

| Original Queue | Dead Letter Queue |
|---|---|
| notification.email.queue | notification.email.dlq |
| notification.sms.queue | notification.sms.dlq |
| notification.push.queue | notification.push.dlq |

### Intended failure flow

```text
Original Queue
      |
      v
Consumer Processing
      |
      v
Processing Failure
      |
      v
Retry Attempts
      |
      v
Retries Exhausted
      |
      v
Dead Letter Exchange
      |
      v
Dead Letter Queue
```

The purpose is to avoid losing messages after repeated processing failures.

The configured retry and DLQ behavior still needs end-to-end failure testing to confirm that messages are retried and routed to the correct DLQ.

---

## 11. REST API Implementation

### 11.1 Create Notification

**Method:** POST

**Endpoint:**

`/api/v1/notifications`

Purpose:

- Accept notification request.
- Check idempotency.
- Save Notification.
- Save OutboxEvent.
- Return notification ID and status.

Response uses the existing `NotificationResponse` DTO.

### 11.2 Get Notification Details

**Method:** GET

**Endpoint:**

`/api/v1/notifications/{id}`

Purpose:

Retrieve complete notification information.

Response DTO:

`NotificationDetailsResponse`

Fields include:

- notificationId
- status
- channel
- destination
- eventType
- sourceService
- createdAt
- updatedAt

### 11.3 Get Notification Status

**Method:** GET

**Endpoint:**

`/api/v1/notifications/{id}/status`

Purpose:

Retrieve the current notification status.

Response DTO:

`NotificationStatusResponse`

### 11.4 Get All Notifications

**Method:** GET

**Endpoint:**

`/api/v1/notifications?page=0&size=10`

Purpose:

Retrieve notifications using pagination.

Implemented using Spring Data JPA Pageable.

### Exception Handling

Implemented a ResourceNotFoundException and GlobalExceptionHandler for handling errors such as:

- Notification not found – HTTP 404.
- Invalid request – HTTP 400.

---

## 12. Dockerization

We containerized the application and its dependencies using Docker Compose.

### Dockerfile

Created a multi-stage Dockerfile.

Build stage:

- Uses Eclipse Temurin JDK 17.
- Copies Gradle wrapper and project source.
- Builds the executable Spring Boot JAR.

Runtime stage:

- Uses Eclipse Temurin JRE 17.
- Copies the generated JAR.
- Starts the application using `java -jar app.jar`.

### Build configuration

Configured Gradle bootJar:

```groovy
tasks.named('bootJar') {
    archiveFileName = 'app.jar'
}
```

### Environment-based configuration

Updated application.properties to support environment variables.

For example:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5433/notification_db}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:postgres}

spring.rabbitmq.host=${SPRING_RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${SPRING_RABBITMQ_PORT:5672}

spring.mail.host=${SPRING_MAIL_HOST:localhost}
spring.mail.port=${SPRING_MAIL_PORT:1025}
```

This allows the same application to run:

- Locally using localhost configuration.
- Inside Docker using Docker Compose service names.

### .dockerignore

Created `.dockerignore` to exclude unnecessary files from the Docker build context.

```text
.gradle
build
.git
.idea
*.iml
```

---

## 13. Docker Compose Implementation

Created `docker-compose.yml` to run all required services.

### Containers

| Service | Image | Container Port | Host Port |
|---|---|---:|---:|
| Notification Service | Locally built image | 8080 | 8080 |
| PostgreSQL | postgres:18.3 | 5432 | 5434 |
| RabbitMQ | rabbitmq:4-management | 5672, 15672 | 5672, 15672 |
| Mailpit | axllent/mailpit | 1025, 8025 | 1025, 8025 |

### Docker networking

Docker Compose creates a default network.

The Notification Service connects to other containers using service names rather than localhost.

Examples:

```text
jdbc:postgresql://postgres:5432/notification_db

RabbitMQ host: rabbitmq

Mailpit host: mailpit
```

### PostgreSQL volume

Configured a named volume:

```yaml
volumes:
  - postgres_data:/var/lib/postgresql
```

This preserves PostgreSQL data when containers are stopped or recreated.

The mount path is configured for PostgreSQL 18.

### Health checks

PostgreSQL:

```yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U postgres -d notification_db"]
```

RabbitMQ:

```yaml
healthcheck:
  test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
```

Notification Service depends on PostgreSQL and RabbitMQ becoming healthy before startup.

### Starting the application

From the project directory:

```cmd
cd /d D:\SystemDesign\NotificationSystem

docker compose up --build -d
```

### Checking containers

```cmd
docker compose ps
```

### Viewing application logs

```cmd
docker compose logs -f notification-service
```

### Stopping containers

```cmd
docker compose down
```

Do not use `docker compose down -v` unless intentionally deleting the named database volume.

---

## 14. Docker Deployment – Current Verified Result

Successfully built and started all four containers using Docker Compose.

Current status:

| Container | Status |
|---|---|
| notification-service | Up |
| postgres | Up (healthy) |
| rabbitmq | Up (healthy) |
| mailpit | Up (healthy) |

### Available URLs

| Service | URL |
|---|---|
| Notification Service | http://localhost:8080 |
| Actuator Health | http://localhost:8080/actuator/health |
| RabbitMQ Management | http://localhost:15672 |
| Mailpit Inbox | http://localhost:8025 |

RabbitMQ local credentials:

```text
Username: guest
Password: guest
```

Mailpit SMTP:

```text
Host: localhost
Port: 1025
```

Docker Compose is now successfully starting the complete application environment.

---

## 15. Architecture Decisions

### Why asynchronous processing?

The API does not need to wait for email, SMS, or push processing to finish.

It can accept the request, persist it, and return a response while consumers process the notification independently.

### Why RabbitMQ?

RabbitMQ decouples notification creation from delivery processing.

It supports:

- Message queues.
- Routing.
- Consumer-based processing.
- Acknowledgements.
- Retry and dead-letter handling.

### Why PostgreSQL?

PostgreSQL provides persistent storage for notification records and outbox events.

### Why Transactional Outbox?

It prevents notification records from being committed without a corresponding event record in the same database transaction.

### Why Docker Compose?

It allows the application, database, broker, and email testing server to run in a consistent local environment.

### Why no load balancing?

The current project intentionally runs a single Notification Service instance.

NGINX, multiple application instances, and load balancing are not part of the current implementation scope.

---

## 16. Current Implementation Checklist

### Implemented

- [x] Spring Boot application setup.
- [x] PostgreSQL integration.
- [x] Notification entity.
- [x] OutboxEvent entity.
- [x] Notification creation REST API.
- [x] Idempotency check.
- [x] Transactional Outbox Pattern.
- [x] Scheduled Outbox Publisher.
- [x] RabbitMQ direct exchange.
- [x] Email, SMS, and push queues.
- [x] Email consumer.
- [x] SMS consumer.
- [x] Push consumer.
- [x] Mailpit integration.
- [x] Notification status lifecycle.
- [x] Notification details API.
- [x] Notification status API.
- [x] Paginated notification API.
- [x] Exception handling.
- [x] Retry-related configuration.
- [x] Dead Letter Exchange and queues.
- [x] Dockerfile.
- [x] Docker Compose.
- [x] PostgreSQL persistent volume.
- [x] PostgreSQL and RabbitMQ health checks.
- [x] Successfully started all four Docker containers.

### Remaining verification and improvements

- [ ] Test complete notification creation and email delivery with Docker Compose running.
- [ ] Verify Notification and OutboxEvent records in Docker PostgreSQL.
- [ ] Verify RabbitMQ routing and consumer processing.
- [ ] Test duplicate notification requests.
- [ ] Test retry behavior using an intentional provider failure.
- [ ] Verify messages reach the correct DLQ after retries are exhausted.
- [ ] Test notification status transitions.
- [ ] Test not-found and invalid-request API responses.
- [ ] Add a comprehensive README with setup instructions and API examples.
- [ ] Review production concerns such as authentication, secrets management, observability, and database migration strategy.

---

## 17. Final Project Scope

The current project is a single-instance asynchronous notification system.

It includes:

- REST-based notification requests.
- PostgreSQL persistence.
- Transactional Outbox Pattern.
- RabbitMQ message routing.
- Separate channel consumers.
- Email testing through Mailpit.
- Notification status tracking.
- Retry and DLQ configuration.
- Docker-based deployment.

**Not included in the current scope:**

- NGINX.
- Load balancing.
- Multiple application instances.
- Real SMS provider integration.
- Real push notification provider integration.
- External email provider integration.
- Provider delivery webhooks.


