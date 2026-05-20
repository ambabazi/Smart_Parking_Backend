# Smart_Parking_Backend

Swagger UI: /swagger-ui/index.html
OpenAPI spec: /api-docs
Runtime config: YAML files under src/main/resources/

The backend now uses Redis for cache, refresh tokens, and rate limiting; it also exposes paginated public reads and timing metrics for slower requests.

The backend now includes Redis-backed caching, refresh tokens, paginated list endpoints, async notifications, rate limiting, and request/endpoint timing metrics.
