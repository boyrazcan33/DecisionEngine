# Decision Engine

A loan decision engine that evaluates loan applications and returns the maximum approvable loan amount based on a credit scoring algorithm.

---

## Project Structure
```
decision-engine/
├── backend/         ← Java 21 + Spring Boot 4.0.4
├── frontend/        ← Vanilla TypeScript + SCSS
└── docker-compose.yml
```

---

## Running the Project

### Option 1: Docker (Recommended)

**Prerequisites**
- [Docker Desktop](https://www.docker.com/products/docker-desktop) installed and running

**Steps**

1. Clone the repository
```bash
git clone https://github.com/boyrazcan33/DecisionEngine.git
cd DecisionEngine
```

2. Start all services
```bash
docker compose up --build
```

3. Open the application
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080

> **Apple Silicon (M1/M2/M3) Users**
> If you encounter platform-related errors, enable **"Use Rosetta for x86/amd64 emulation"** in Docker Desktop → Settings → General.
> Or add the following to each service in `docker-compose.yml`:
> ```yaml
> platform: linux/amd64
> ```

---

### Option 2: Manual Setup

**Prerequisites**
- Java 21
- Maven 3.9+
- Node.js 20+

**Backend**

1. Clone the repository
```bash
git clone https://github.com/boyrazcan33/DecisionEngine.git
cd DecisionEngine
```

2. Navigate to backend folder
```bash
cd backend
```

3. Run the application
```bash
./mvnw spring-boot:run
```
> Windows users: use `mvnw.cmd spring-boot:run`

Backend will start at http://localhost:8080

**Frontend**

1. Open a new terminal and navigate to frontend folder
```bash
cd frontend
```

2. Install dependencies
```bash
npm install
```

3. Compile SCSS
```bash
npx sass src/styles/main.scss src/styles/main.css
```

4. Compile TypeScript
```bash
npx tsc
```

5. Open `frontend/index.html` in your browser

---

## Test Personal Codes

| Personal Code | Status |
|---|---|
| 49002010965 | Debt — always rejected |
| 49002010976 | Segment 1 (credit modifier: 100) |
| 49002010987 | Segment 2 (credit modifier: 300) |
| 49002010998 | Segment 3 (credit modifier: 1000) |

**Constraints**
- Loan amount: €2000 – €10000
- Loan period: 12 – 60 months

---

## How It Works

The engine takes three inputs — personal code, loan amount, and loan period — and returns the maximum approvable amount regardless of what the applicant requested.

Credit score is calculated as:
```
credit score = (credit modifier / loan amount) * loan period
```

If the score is below 1, the engine extends the period up to 60 months and recalculates. If the maximum amount exceeds €10000, the cap is applied and the shortest possible period to reach €10000 is returned.

If the applicant has existing debt, the request is rejected immediately.

---

## Thought Process

**Core algorithm decision**

The assignment states:
> *"The idea of the decision engine is to determine what would be the maximum sum, regardless of the person requested loan amount."*

This means the requested amount is irrelevant to the decision — we always calculate the maximum approvable amount. For example, a customer requests €4000 for 12 months with a credit modifier of 100. We could have offered €2000 over 20 months or €4000 over 40 months, but the assignment asks for the maximum sum — so we fix the period at 60 months and return €6000.

For customers where the maximum exceeds €10000 (e.g. credit modifier 1000): `1000 * 60 = €60000` exceeds the cap. We apply the €10000 limit and find the shortest period to reach it — in this case 12 months (`1000 * 12 = 12000 > 10000`). This is better for both the customer (shorter debt) and the bank (faster return).

**Why Vanilla TypeScript instead of a framework?**
The task required a simple form with a single API call. Introducing a framework like Vue.js or Angular would have been over-engineering for this scope. Vanilla TypeScript keeps the frontend minimal, readable, and appropriate for the problem size.

**Why a mono repo?**
Backend and frontend are part of the same project and share the same lifecycle. Keeping them in a single repository makes it easier to run, maintain, and review together — especially with Docker Compose orchestrating both services.

**Why Global Exception Handler?**
Although the current scope only has one endpoint, `@ControllerAdvice` keeps the architecture flexible. As the application grows with new endpoints, all exception handling stays centralized in one place rather than scattered across controllers.

**Why unit tests only on the service layer?**
All decision logic is contained within `DecisionService`. There was no meaningful logic on the frontend worth unit testing.

**Two-layer validation**
Frontend enforces `min`/`max` constraints via HTML attributes for better UX. Backend independently validates all inputs and rejects anything out of range — the API protects itself regardless of the client.

---

## What I Would Improve

I would improve the depth of data management and the environment standards.

Currently, the assignment relies on a very small hardcoded dataset that does not fully test a candidate's data handling capabilities. I would suggest providing a larger realistic fake dataset (e.g. an SQL dump or CSV) that candidates must integrate into a database. This would allow the company to evaluate SQL proficiency, indexing strategies, and the ability to handle data at a more realistic scale.

Additionally, while Docker is an industry standard today, it is not required in the current task. I would improve this by requiring the project to be delivered as a containerized monorepo including frontend, backend, and database. This would better demonstrate the candidate's ability to manage production-ready environments and modern development workflows.