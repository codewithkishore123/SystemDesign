# Chat Application – Features & Technical Documentation

## 1. Project Overview

A real-time one-to-one chat application built using Java, Spring Boot, PostgreSQL, Redis, RabbitMQ, and WebSocket.

The application supports user authentication, conversation management, persistent messaging, real-time communication, online/offline presence tracking, and message delivery/read receipts.

The project demonstrates backend development concepts and system design principles, including asynchronous communication, event-driven architecture, authentication, caching, and real-time messaging.

### Main Objectives

- Build a scalable real-time messaging backend.
- Implement secure user authentication using JWT.
- Support one-to-one conversations.
- Persist chat messages in PostgreSQL.
- Deliver messages in real time using WebSocket and STOMP.
- Use RabbitMQ for asynchronous message processing.
- Track online/offline user presence using Redis.
- Implement SENT, DELIVERED, and READ message statuses.
- Handle application failures and duplicate messages.

---

## 2. Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Backend programming language |
| Spring Boot 4.1.2 | Application framework |
| Spring Web | REST API development |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Database persistence |
| PostgreSQL 18.3 | Relational database |
| Redis | User presence and heartbeat management |
| RabbitMQ 4 | Asynchronous messaging |
| WebSocket | Real-time communication |
| STOMP | WebSocket messaging protocol |
| JWT | Stateless authentication |
| Gradle | Build and dependency management |
| Docker Compose | Infrastructure management |
| Postman | REST API testing |
| Browser STOMP.js client | WebSocket testing |
| JMeter | Planned load testing |

---

## 3. Project Architecture

### High-Level Architecture

```text
                    CLIENT APPLICATIONS
                  (Browser / Postman)
                           |
             REST API / WebSocket (STOMP)
                           |
                           v
                  SPRING BOOT APPLICATION
                           |
          +----------------+----------------+
          |                |                |
          v                v                v
     PostgreSQL          Redis          RabbitMQ
          |                |                |
          |                |                |
     User Data        User Presence    Message Events
     Conversations    Heartbeats       Message Queue
     Messages         TTL              Async Processing
          |
          v
     Message History

                  WebSocket Message Broker
                           |
                           v
                   Connected Recipients
```

### Component Responsibilities

**Spring Boot**
- Handles REST API requests.
- Manages business logic.
- Performs JWT authentication.
- Manages conversations and messages.
- Handles WebSocket communication.

**PostgreSQL**
- Stores user information.
- Stores conversations and participants.
- Stores messages and their statuses.
- Maintains persistent application data.

**Redis**
- Tracks online/offline user status.
- Stores user presence with expiration.
- Refreshes presence through periodic heartbeats.

**RabbitMQ**
- Receives published chat message events.
- Processes events asynchronously.
- Forwards messages to connected recipients through WebSocket.

**WebSocket and STOMP**
- Establishes real-time communication.
- Authenticates WebSocket connections.
- Supports message publishing and subscriptions.
- Delivers messages and receipt notifications.

---

# 4. Implemented Features

## Phase 1 – Project Setup

**Status: Completed**

- Created Spring Boot project using Gradle.
- Configured Java 17.
- Added required dependencies.
- Configured PostgreSQL.
- Configured Redis.
- Configured RabbitMQ.
- Created Docker Compose configuration.
- Configured application properties.
- Verified infrastructure connectivity.

### Docker Services

| Service | Container | Port |
|---|---|---|
| PostgreSQL | chat-postgres | 5434 |
| Redis | chat-redis | 6379 |
| RabbitMQ | chat-rabbitmq | 5672 |
| RabbitMQ Management | chat-rabbitmq | 15672 |
| Spring Boot | Chat Application | 8080 |

### Infrastructure Management

Start services:

```bash
docker compose up -d
```

Check services:

```bash
docker compose ps
```

Stop services:

```bash
docker compose down
```

---

## Phase 2 – User Management

**Status: Implemented**

### Features

- User registration.
- User login.
- Password encryption using BCrypt.
- JWT token generation.
- JWT token validation.
- Stateless authentication.
- Spring Security integration.
- Authentication filter.
- Protected REST APIs.
- Global exception handling.

### User Entity

Stores:

- User ID (UUID).
- Username.
- Email.
- Encrypted password.
- Created timestamp.

### REST APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register a user |
| POST | `/api/v1/auth/login` | Authenticate a user |

### Security

- Passwords are encrypted using BCrypt.
- JWT tokens are generated after successful login.
- Protected endpoints require a valid Bearer token.
- User UUID is extracted from the authenticated JWT.
- Stateless session management is enabled.

---

## Phase 3 – Conversation Management

**Status: Implemented**

### Features

- Create one-to-one conversations.
- Retrieve user conversations.
- Validate conversation participants.
- Prevent users from creating conversations with themselves.
- Prevent duplicate direct conversations.
- Use UUID-based conversation identifiers.

### Database Entities

**Conversation**
- Conversation ID.
- Conversation type.
- Direct chat key.
- Created timestamp.

**ConversationParticipant**
- Participant ID.
- Conversation reference.
- User reference.
- Joined timestamp.

### REST APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/conversations` | Create or retrieve a direct conversation |
| GET | `/api/v1/conversations` | Retrieve authenticated user's conversations |

### Design Considerations

A sorted UUID pair is used to generate a unique direct chat key.

This ensures that the same two users cannot accidentally create multiple direct conversations.

---

## Phase 4 – Message Persistence

**Status: Implemented**

### Features

- Send messages through REST API.
- Persist messages in PostgreSQL.
- Retrieve conversation message history.
- Validate conversation membership.
- Validate message content.
- Store message creation and update timestamps.
- Maintain message status.

### Message Entity

Stores:

- Message ID.
- Conversation ID.
- Sender ID.
- Message content.
- Message status.
- Created timestamp.
- Updated timestamp.

### Message Status

- SENT
- DELIVERED
- READ

### REST APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/conversations/{conversationId}/messages` | Send a message |
| GET | `/api/v1/conversations/{conversationId}/messages` | Retrieve message history |

### Validation

- Message cannot be empty.
- Maximum message length: 5000 characters.
- Only conversation participants can send or retrieve messages.

---

## Phase 5 – WebSocket Implementation

**Status: Implemented**

### Features

- Real-time messaging using WebSocket.
- STOMP protocol integration.
- JWT authentication during STOMP CONNECT.
- User-specific message subscriptions.
- Message publishing through application destinations.
- Authenticated WebSocket communication.

### WebSocket Configuration

| Configuration | Value |
|---|---|
| WebSocket endpoint | `/ws` |
| Application prefix | `/app` |
| User destination prefix | `/user` |
| Simple broker prefixes | `/queue`, `/topic` |

### WebSocket Destinations

**Send message**

```text
/app/chat.send
```

**Receive messages**

```text
/user/queue/messages
```

### Authentication

JWT is passed in the STOMP CONNECT native header:

```text
Authorization: Bearer <JWT_TOKEN>
```

A custom channel interceptor validates the token and establishes the authenticated principal.

---

## Phase 6 – RabbitMQ Integration

**Status: Implemented**

### Features

- RabbitMQ integration with Spring AMQP.
- Asynchronous message event publishing.
- Message event consumption.
- Queue, exchange, and binding configuration.
- Jackson JSON message conversion.
- WebSocket delivery through asynchronous event processing.

### RabbitMQ Configuration

| Component | Name |
|---|---|
| Exchange | `chat.message.exchange` |
| Queue | `chat.message.queue` |
| Routing Key | `chat.message` |

### Message Processing Flow

1. User sends a message through WebSocket.
2. Spring Boot persists the message in PostgreSQL.
3. Message event is created.
4. Event is published to RabbitMQ.
5. RabbitMQ delivers the event to the consumer.
6. Consumer processes participant IDs.
7. Messages are forwarded through WebSocket to recipients.

### Architecture Benefits

- Separates message persistence from message delivery.
- Supports asynchronous processing.
- Reduces direct coupling between the WebSocket controller and message delivery.
- Provides a foundation for retry and failure-handling mechanisms.

---

## Phase 7 – Redis Presence Management

**Status: Implemented**

### Features

- Online user tracking.
- Offline user tracking.
- Redis-based presence storage.
- Heartbeat implementation.
- Automatic key expiration.
- Presence status REST API.
- WebSocket connect/disconnect event handling.

### Presence Status

- ONLINE
- OFFLINE

### Redis Key Structure

```text
user:presence:{userId}
```

Example:

```text
user:presence:550e8400-e29b-41d4-a716-446655440000
```

### Configuration

- Presence TTL: 60 seconds.
- Heartbeat interval: 20 seconds.
- Redis key expiration is refreshed during heartbeat processing.

### WebSocket Heartbeat

```text
/app/presence.heartbeat
```

Heartbeat payload:

```json
{
  "type": "HEARTBEAT"
}
```

### REST API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/presence/{userId}` | Retrieve user presence |

### Presence Flow

1. User connects through WebSocket.
2. Application marks the user ONLINE.
3. Browser sends periodic heartbeat messages.
4. Redis TTL is refreshed.
5. On disconnect, the presence key is deleted.
6. If heartbeat stops unexpectedly, the Redis key expires automatically.

---

## Phase 8 – Delivery and Read Receipts

**Status: Implemented – End-to-end testing**

### Features

- SENT message status.
- DELIVERED message status.
- READ message status.
- Recipient-side delivery acknowledgement.
- Recipient-side read acknowledgement.
- Receipt notifications to the original sender.
- Validation of conversation membership.
- Validation that sender cannot acknowledge their own message.

### Message Status Flow

```text
       SENT
         |
         v
     DELIVERED
         |
         v
       READ
```

### WebSocket Destinations

**Mark message as delivered**

```text
/app/chat.delivered
```

**Mark message as read**

```text
/app/chat.read
```

**Receive receipt notifications**

```text
/user/queue/receipts
```

### Receipt Payload

```json
{
  "messageId": "MESSAGE_UUID",
  "conversationId": "CONVERSATION_UUID"
}
```

### Processing Flow

1. Sender sends a message.
2. Message is persisted with SENT status.
3. Recipient receives the message.
4. Recipient sends a DELIVERED acknowledgement.
5. Database status changes to DELIVERED.
6. Recipient reads the message.
7. Recipient sends a READ acknowledgement.
8. Database status changes to READ.
9. Original sender receives a receipt notification.

---

# 5. Skipped Feature

## Phase 9 – Offline Message Delivery

**Status: Skipped**

This feature is intentionally excluded from the current project scope.

The current implementation does not provide guaranteed automatic retrieval and delivery of all pending messages after a recipient reconnects.

The application currently focuses on real-time messaging between connected users.

---

# 6. Remaining Improvements

## Phase 10 – Reliability and Testing

**Status: Planned**

### 1. Idempotency

Prevent duplicate messages when clients retry requests.

Planned implementation:

- Generate a unique client message ID.
- Add a unique constraint for sender and client message ID.
- Check existing messages before inserting new ones.
- Return the previously created message for duplicate requests.

### 2. RabbitMQ Retry Mechanism

Handle temporary consumer processing failures.

Planned implementation:

- Configure retry attempts.
- Configure retry intervals.
- Handle message processing exceptions.
- Prevent endless message requeueing.

### 3. Dead Letter Queue

Handle messages that fail after all retry attempts.

Planned implementation:

- Create a Dead Letter Exchange.
- Create a Dead Letter Queue.
- Configure dead-letter routing.
- Inspect failed messages.

### 4. WebSocket Reliability

Planned improvements:

- Automatic reconnection.
- Connection lifecycle handling.
- Duplicate event handling.
- Multiple WebSocket session management.
- Connection recovery.

### 5. Failure Scenarios

Test application behavior when:

- PostgreSQL is unavailable.
- Redis is unavailable.
- RabbitMQ is unavailable.
- WebSocket disconnects unexpectedly.
- Consumer processing fails.
- Duplicate messages are submitted.
- Multiple users send messages simultaneously.

### 6. Load Testing

Use Apache JMeter to test:

- User registration and login.
- Conversation creation.
- Message sending APIs.
- Message history retrieval.
- Concurrent API requests.
- Response time.
- Throughput.
- Error percentage.

### 7. Database Optimization

Planned improvements:

- Add appropriate database indexes.
- Analyze frequently executed queries.
- Use pagination for message history.
- Optimize database access patterns.
- Evaluate connection pool configuration.

---

# 7. Phase 11 – HLD Interview Preparation

**Status: Planned**

Prepare the complete system design explanation.

### Architecture Topics

- High-Level Design.
- Low-Level Design.
- REST vs WebSocket.
- WebSocket connection lifecycle.
- STOMP protocol.
- JWT authentication.
- RabbitMQ exchange, queue, and routing key.
- Asynchronous messaging.
- Redis TTL and presence management.
- Database indexing.
- Message delivery guarantees.
- Idempotency.
- Retry mechanisms.
- Dead Letter Queues.
- Horizontal scaling.
- WebSocket connection management.
- Database partitioning and sharding.
- Caching strategies.
- Event-driven architecture.
- CAP theorem.
- Consistency and availability trade-offs.

### Interview Questions to Prepare

1. Why did you choose WebSocket instead of REST polling?
2. Why did you introduce RabbitMQ?
3. What happens if RabbitMQ is unavailable?
4. How do you prevent duplicate messages?
5. How do you track online users?
6. Why did you choose Redis for presence?
7. How does Redis TTL work?
8. How do you handle unexpected WebSocket disconnections?
9. How would you support millions of concurrent connections?
10. How would you scale the database?
11. How do you ensure message ordering?
12. How do you handle failed message delivery?
13. How would you implement group chat?
14. How would you implement message search?
15. How would you handle multiple devices for one user?

---

# 8. API Summary

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/auth/register` | Register user |
| POST | `/api/v1/auth/login` | Authenticate user |
| POST | `/api/v1/conversations` | Create direct conversation |
| GET | `/api/v1/conversations` | Retrieve conversations |
| POST | `/api/v1/conversations/{conversationId}/messages` | Send message |
| GET | `/api/v1/conversations/{conversationId}/messages` | Retrieve message history |
| GET | `/api/v1/presence/{userId}` | Retrieve presence status |

---

# 9. WebSocket API Summary

| Destination | Purpose |
|---|---|
| `/ws` | WebSocket connection endpoint |
| `/app/chat.send` | Publish chat message |
| `/user/queue/messages` | Receive chat messages |
| `/app/presence.heartbeat` | Send presence heartbeat |
| `/app/chat.delivered` | Acknowledge delivery |
| `/app/chat.read` | Acknowledge read receipt |
| `/user/queue/receipts` | Receive receipt updates |

---

# 10. Database Overview

### User

Stores application user details and authentication information.

### Conversation

Stores one-to-one conversation information.

### ConversationParticipant

Maintains the relationship between users and conversations.

### Message

Stores chat message content, sender, conversation, status, and timestamps.

### Relationships

```text
User
 |
 +------ ConversationParticipant
 |                  |
 |                  |
 |             Conversation
 |                  |
 |                  |
 +------ Message ---+
```

---

# 11. Project Directory Structure

```text
ChatApplication
|
+--- src/main/java/com/chat
|    |
|    +--- config
|    +--- controller
|    +--- dto
|    +--- entity
|    +--- enums
|    +--- exception
|    +--- repository
|    +--- security
|    +--- service
|    +--- websocket
|
+--- src/main/resources
|    |
|    +--- application.properties
|
+--- ws-test
|    |
|    +--- index.html
|
+--- docker-compose.yml
+--- build.gradle
+--- settings.gradle
+--- feature.md
```

---

# 12. Running the Application

### Prerequisites

- Java 17
- Docker Desktop
- Gradle Wrapper
- Postman
- Modern web browser

### Step 1: Start Infrastructure

```bash
docker compose up -d
```

### Step 2: Verify Containers

```bash
docker compose ps
```

### Step 3: Start Spring Boot

```bash
gradlew bootRun
```

### Step 4: Check Application Health

```http
GET http://localhost:8080/actuator/health
```

### Step 5: Open WebSocket Test Client

Open:

```text
ws-test/index.html
```

Provide the JWT token and conversation UUID to establish a WebSocket connection.

---

# 13. Project Scope and Current Completion

| Phase | Description | Status |
|---|---|---|
| 1 | Project Setup | Completed |
| 2 | User Management | Completed |
| 3 | Conversation Management | Completed |
| 4 | Message Persistence | Completed |
| 5 | WebSocket Implementation | Implemented |
| 6 | RabbitMQ Integration | Completed |
| 7 | Redis Presence | Completed |
| 8 | Delivery and Read Receipts | Implemented – testing |
| 9 | Offline Message Delivery | Skipped |
| 10 | Reliability and Testing | Planned |
| 11 | HLD Interview Preparation | Planned |

---

# 14. Future Enhancements

Features that can be added after completing the core project:

- Group conversations.
- Typing indicators.
- Message editing and deletion.
- File and image sharing.
- Message search.
- Push notifications.
- Multiple-device support.
- Message pagination improvements.
- User blocking.
- Conversation archiving.
- Message reactions.
- End-to-end encryption.

---

## Conclusion

This project demonstrates a real-time messaging backend using Java, Spring Boot, PostgreSQL, Redis, RabbitMQ, JWT, and WebSocket.

It covers important backend engineering concepts such as authentication, relational data modelling, asynchronous event processing, real-time communication, presence tracking, and message state management.

The remaining focus is reliability, performance testing, failure handling, and explaining the architecture through High-Level Design and Low-Level Design discussions.