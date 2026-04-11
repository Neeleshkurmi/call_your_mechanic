# Call Your Mechanic Backend

## Local Docker Run

`Step 1 :` niche bali command ko run kar terminal me
```bash
cd backend
```
`Step 2 :` niche bali command ko run kar terminal me
```bash
docker compose up --build
```

ab tumhe otp console par dikh jayegi jab bhi tum request karoge kuch is format me 
`>>> OTP: [424617] | MOBILE: [***2157] <<<`

This starts: 2nd (swagger-ui) url pe cursor le ja kar control click kar 

- Spring Boot API on `http://localhost:8080`
- Swagger UI on `http://localhost:8080/swagger-ui/index.html`
- PostgreSQL on `localhost:5432`
- Redis on `localhost:6379`

## Redis Cache Notes

- Spring cache falls back to fresh database/service reads if Redis contains stale or incompatible cache payloads.
- The local `dev` profile uses a simple in-memory Spring cache instead of Redis-backed caching to avoid DevTools cache drift during development.
- For local IDE or terminal runs, set `SPRING_PROFILES_ACTIVE=dev` so the dev cache settings are applied.
- After serializer or DTO cache shape changes, clear old Redis cache entries once before rollout.
- For local Docker, a safe reset is `docker compose exec redis redis-cli FLUSHDB` to remove stale cache data from the current Redis database.
- The current Spring cache namespace prefix is `cym:v6:`. If you inspect Redis manually, only keys in the active namespace are used.

## Frontend Team Notes

- The database is seeded automatically from `src/main/resources/data.sql`
- Mock OTP is enabled by default in `application.yml`
- Use OTP `123456` for local login/verification flows
- No Twilio setup is required for local Docker usage
