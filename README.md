# Decision Engine

A loan decision engine that evaluates loan applications based on a credit scoring algorithm.
It determines the maximum approvable loan amount and period for a given applicant.

## Tech Stack

- **Backend:** Java 21, Spring Boot 4.0.4, Maven
- **Frontend:** TypeScript, SCSS
- **Container:** Docker

## Project Structure
```
decision-engine/
├── backend/        ← Spring Boot REST API
├── frontend/       ← Vanilla TypeScript + SCSS
└── docker-compose.yml
```

## Running with Docker (Recommended)

Make sure Docker Desktop is installed and running.

**Windows / Linux:**
```bash
docker compose up --build
```

**macOS (Apple Silicon - M1/M2/M3):**
```bash
docker compose up --build
```
> If you get a platform error on Apple Silicon, add this to `docker-compose.yml` under each service:
> ```yaml
> platform: linux/amd64
> ```

Once running:
- Frontend: http://localhost:3000
- Backend: http://localhost:8080

## Running Manually

**Backend:**
```bash
cd backend
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npx sass src/styles/main.scss src/styles/main.css
npx tsc
```
Then open `frontend/index.html` in your browser.

## How It Works

The engine takes three inputs:
- Personal code
- Loan amount (€2000 - €10000)
- Loan period (12 - 60 months)

It calculates a credit score using the formula:
```
credit score = (credit modifier / loan amount) * loan period
```

If the score is below 1, the engine tries to find the highest approvable amount within the
requested period. If no suitable amount is found, it extends the period until a valid
combination is found.

If the applicant has existing debt, the request is rejected immediately.

## Test Personal Codes

| Personal Code | Status |
|---|---|
| 49002010965 | Debt — always rejected |
| 49002010976 | Segment 1 (modifier: 100) |
| 49002010987 | Segment 2 (modifier: 300) |
| 49002010998 | Segment 3 (modifier: 1000) |

## What I Would Improve

Currently the applicant data (credit modifier, debt status) is hardcoded. In a real-world
scenario this would connect to external registries to fetch a comprehensive user profile
dynamically.

I would also add input validation on the frontend side to give users immediate feedback
before the request is sent to the backend.