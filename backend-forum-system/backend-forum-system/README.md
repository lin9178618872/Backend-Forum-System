# Backend Forum System

Java / Spring Boot / Redis / Elasticsearch forum backend Spring Boot project.

## Tech Stack

- Java 17, Spring Boot 3
- Spring MVC, Spring Data JPA, Bean Validation
- MySQL for transactional forum data
- Redis for post-list caching, hot-post ranking, and buffered view counts
- Elasticsearch for full-text post search
- Async thread pool for non-blocking search indexing
- React frontend for feed, compose, thread detail, comments, and search


## Run Locally

Prerequisites:

- JDK 17+
- Maven 3.9+
- Docker Desktop

Start dependencies:

```bash
docker compose up -d
```

Run the app:

```bash
mvn spring-boot:run
```

The service starts on:

```text
http://localhost:8080
```

For a quick local demo without Docker/MySQL, use the H2-backed profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.arguments=--server.port=18080
```

## Run Frontend

The React frontend lives in `frontend/`. It is intentionally zero-build so it can run even when npm is not installed.

Start the frontend server:

```bash
cd frontend
node server.mjs
```

Open:

```text
http://localhost:5173
```

The frontend server proxies `/api` requests to `http://localhost:18080` by default. To point it at another backend:

```bash
API_TARGET=http://localhost:8080 node server.mjs
```

## API Examples

Create a user:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"alice\",\"email\":\"alice@example.com\"}"
```

Create a post:

```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d "{\"authorId\":1,\"title\":\"Spring Boot forum\",\"content\":\"Redis cache and Elasticsearch search are enabled.\"}"
```

List posts:

```bash
curl "http://localhost:8080/api/posts?page=0&size=10"
```

Read one post and record a Redis-backed view:

```bash
curl http://localhost:8080/api/posts/1
```

Flush buffered views from Redis into MySQL:

```bash
curl -X POST http://localhost:8080/api/posts/1/flush-views
```

Add a comment:

```bash
curl -X POST http://localhost:8080/api/posts/1/comments \
  -H "Content-Type: application/json" \
  -d "{\"authorId\":1,\"content\":\"Nice implementation.\"}"
```

Search posts:

```bash
curl "http://localhost:8080/api/search/posts?keyword=Spring&page=0&size=10"
```

Get hot posts:

```bash
curl http://localhost:8080/api/posts/hot
```

## Design Notes

- MySQL owns durable data: users, posts, and comments.
- Redis is used for data that changes frequently or is requested often:
  - `posts` cache stores paginated post lists.
  - `post:views:{id}` buffers repeated views before writing to MySQL.
  - `post:hot` is a sorted set for hot-post ranking.
- Elasticsearch stores denormalized `PostDocument` records for search. The app can still start if Elasticsearch is temporarily unavailable; search/indexing calls require Elasticsearch to be running.
- `SearchIndexService` updates Elasticsearch asynchronously so post creation does not block on indexing latency.
