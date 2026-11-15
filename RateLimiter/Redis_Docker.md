# Docker and Redis Setup for Rate Limiter Project

## 1. Install Docker Desktop on Windows

1. Download and install Docker Desktop for Windows.
2. Configure Docker Desktop to use the **WSL 2 backend**.
3. If Docker Desktop fails to start, open CMD and execute:

```cmd
wsl --update
wsl --shutdown
```

4. Restart Docker Desktop.
5. Wait until Docker Desktop displays that the Docker engine is running.

## 2. Verify Docker Installation

Open CMD and execute:

```cmd
docker --version
```

Verify Docker is running:

```cmd
docker ps
```

If Docker is running successfully, the command displays the running containers.

## 3. Download the Redis Docker Image

Execute:

```cmd
docker pull redis:latest
```

This downloads the official Redis Docker image.

## 4. Create and Start Redis Container

Execute:

```cmd
docker run -d --name redis -p 6379:6379 redis:latest
```

Explanation:

- `docker run`: Creates and starts a container.
- `-d`: Runs the container in detached mode.
- `--name redis`: Assigns the container name as redis.
- `-p 6379:6379`: Maps Windows host port 6379 to Redis container port 6379.
- `redis:latest`: Uses the Redis Docker image.

## 5. Verify Redis Container

Execute:

```cmd
docker ps
```

Expected output should show:

```text
CONTAINER ID   IMAGE          STATUS        PORTS
xxxxxxxx       redis:latest   Up            0.0.0.0:6379->6379/tcp
```

## 6. Connect to Redis CLI

Execute:

```cmd
docker exec -it redis redis-cli
```

This opens the Redis command-line interface inside the container.

### Verify Redis Connectivity

```redis
PING
```

Expected:

```text
PONG
```

## 7. Test Basic Redis Operations

### Store a key-value pair

```redis
SET username kishore
```

### Retrieve the value

```redis
GET username
```

Expected:

```text
kishore
```

### Check whether a key exists

```redis
EXISTS username
```

Expected:

```text
1
```

### Delete a key

```redis
DEL username
```

### Verify deletion

```redis
GET username
```

Expected:

```text
(nil)
```

Exit Redis CLI:

```redis
EXIT
```

## 8. Configure Redis in Spring Boot

### Add Redis dependency in build.gradle

```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

Refresh Gradle dependencies.

### Configure application.properties

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.timeout=2000ms
```

Since the Spring Boot application runs directly on Windows, it connects to Redis using localhost and port 6379.

## 9. Configure RedisTemplate

Create `RedisConfig.java`.

Configure:

- RedisConnectionFactory
- RedisTemplate<String, String>
- StringRedisSerializer for keys and values
- RedisScript<Long> bean for the Fixed Window Lua script

Use RedisTemplate to perform Redis operations from Spring Boot.

If multiple compatible RedisTemplate beans exist, use `@Qualifier("redisTemplate")` to select the custom bean.

## 10. Implement Redis Service

Create `RedisService.java` to encapsulate Redis operations.

Implement:

- `setValue()` – Store data in Redis.
- `getValue()` – Retrieve data from Redis.
- `deleteValue()` – Delete data from Redis.

Create temporary REST endpoints to test SET, GET and DELETE operations.

Verify these operations using Postman.

## 11. Start and Stop Redis

### Start an existing Redis container

```cmd
docker start redis
```

### Stop Redis

```cmd
docker stop redis
```

### Check running containers

```cmd
docker ps
```

### View all containers, including stopped ones

```cmd
docker ps -a
```

### View Redis logs

```cmd
docker logs redis
```

### Open Redis CLI again

```cmd
docker exec -it redis redis-cli
```

## 12. Integrate Redis with Distributed Rate Limiter

The final architecture uses:

- Three Spring Boot application instances.
- One shared Redis container.
- One NGINX load balancer.

All Spring Boot instances connect to the same Redis server:

```text
Application 1 (8080) ──┐
                       │
Application 2 (8082) ──┼──> Redis (localhost:6379)
                       │
Application 3 (8083) ──┘
```

Redis stores the shared rate-limit counters, ensuring that all application instances use the same request count for each user.

**Important:** Do not start separate Redis containers for each Spring Boot instance. All instances must connect to the same Redis server.

## Final Architecture

```text
                 Client / Postman
                        |
                        v
                 NGINX (8085)
                        |
              Load Balancer
                 /     |     \
                /      |      \
               v       v       v
           App 1    App 2    App 3
           8080     8082     8083
              \       |       /
               \      |      /
                v     v     v
                Shared Redis
                  6379
                    |
                    v
                PostgreSQL
                  5433
```

**Result:** Docker Desktop runs Redis in a container, and Spring Boot connects to Redis through Spring Data Redis. All three application instances share the same Redis data, enabling distributed rate limiting.