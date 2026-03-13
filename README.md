# Leadsgen Store

Monorepo fullstack project with a Spring Boot backend and Next.js frontend.

## Project Structure
- `Leadsgen/be`: Backend (Spring Boot)
- `Leadsgen/frontend`: Frontend (Next.js 14)

## Tech Stack
- Backend: Java 17, Spring Boot 3.x, Spring Web, Spring Data JPA, Validation, Lombok
- Frontend: Next.js 14 (App Router), React 18
- Database: H2 in-memory (backend)

## Run Backend

```bash
cd Leadsgen/be
mvnw.cmd spring-boot:run
```

Backend URLs:
- API base: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- H2 Console: `http://localhost:8080/h2-console`

## Run Frontend

```bash
cd Leadsgen/frontend
npm install
npm run dev
```

Frontend URL:
- `http://localhost:3000`

## Frontend Environment

Copy `.env.local.example` to `.env.local` in `Leadsgen/frontend`.

Current default:
- `NEXT_PUBLIC_API_BASE_URL=https://dummyjson.com`
- `NEXT_PUBLIC_USER_ID=1`

If you want frontend to call local backend instead, change:
- `NEXT_PUBLIC_API_BASE_URL=http://localhost:8080`