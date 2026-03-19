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

This starts: 2nd (swagger-ui) url pe cursor le ja kar control click kar 

- Spring Boot API on `http://localhost:8080`
- Swagger UI on `http://localhost:8080/swagger-ui/index.html`
- PostgreSQL on `localhost:5432`
- Redis on `localhost:6379`

## Frontend Team Notes

- The database is seeded automatically from `src/main/resources/data.sql`
- Mock OTP is enabled by default in `application.yml`
- Use OTP `123456` for local login/verification flows
- No Twilio setup is required for local Docker usage
