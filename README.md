# Decision Engine

A loan decision engine that evaluates loan applications and returns the maximum approvable loan amount based on a credit scoring algorithm.

---

## ⚠️ Before Reviewing the Code — Read This First

If the algorithm seems unusual because it does not start from the customer's requested period, or because it always maximizes the loan amount across all periods — this is intentional and explained below.

A reasonable interpretation of the assignment might lead to the following approach:

> *"If a suitable loan amount is not found within the selected period, the decision engine should also try to find a new suitable period."*

From this sentence, one could argue: first try the requested period, find the maximum amount there, and only extend the period if no suitable amount is found.

For example, if the credit modifier is 100 and the customer requests 20 months:
```
(100 / 2000) * 20 = 1.0 → approved → return €2000 at 20 months
```

This looks correct at first glance — a suitable amount was found within the selected period, so the engine stops.

But this directly conflicts with the assignment's core directive:

> *"The idea of the decision engine is to determine what would be the maximum sum, regardless of the person requested loan amount."*

Returning €2000 at 20 months means we are **not** returning the maximum sum — the same customer qualifies for €6000 at 60 months. The word "regardless" is used explicitly for the loan amount, but the assignment **never** says "respect the requested period" or "keep the period fixed." It only says to try a new period when no suitable amount is found — it **does not** say to stop maximizing when one is found.

Keeping the period fixed when a higher amount is achievable would directly violate the "maximum sum" requirement. The engine therefore always evaluates the full potential across all available periods to return the true maximum.

---

## Thought Process

**Core algorithm decision**

The assignment states:
> *"The idea of the decision engine is to determine what would be the maximum sum, regardless of the person requested loan amount."*

And the scoring rule is:
> *"If the result is larger or equal than 1 then we would approve this sum."*

A naive implementation would stop as soon as the score hits 1. For example, a customer with modifier 100 requests €4000 for 12 months:
```
(100 / 4000) * 12 = 0.3 → score < 1 → rejected
```

A common next step would be to loop through periods or amounts until score hits 1:
- €2000 over 20 months: `(100 / 2000) * 20 = 1.0` → approved, return €2000, 20 months
- or €4000 over 40 months: `(100 / 4000) * 40 = 1.0` → approved, return €4000, 40 months

Both are technically correct but both violate the assignment's core requirement. The assignment explicitly states:
> *"The idea of the decision engine is to determine what would be the maximum sum, regardless of the person requested loan amount."*

Even a score of 1.25 is not enough to stop. For example, a customer with modifier 100 requests €2000 for 25 months:
```
(100 / 2000) * 25 = 1.25 → score >= 1 → approved → return €2000, 25 months
```

This is technically approved but still wrong — the customer could have received more. The engine must always find the **maximum**, not just any approvable amount.

The scoring formula can be simplified algebraically:
```
(modifier / amount) * period >= 1
→ amount <= modifier * period
```

This means the maximum approvable amount for any given period is `modifier * period`. To maximize the amount, we maximize the period — always using 60 months (MAX_PERIOD).

**The engine therefore always calculates the maximum potential at 60 months, regardless of what the customer requested:**

1. **Full potential:** Always calculate `modifier * 60`
2. **Cap check:** If the result exceeds €10000, cap the amount at €10000 and keep the customer's requested period — since the maximum sum is already determined, there is no reason to override their preference
3. **Optimum:** If the result is between €2000–€10000, return that amount at 60 months
4. **Floor check:** If even 60 months yields less than €2000, reject

**Why keep the customer's requested period in the cap scenario?**

When the maximum potential exceeds €10000, the approved amount is capped at €10000 regardless of the period. Since extending or shortening the period does not change the approved sum, the customer's original period preference is preserved. Overriding it would serve no purpose — there is no interest rate or repayment calculation in the assignment, so a shorter period offers no financial advantage to either party.

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

If the applicant has existing debt, the request is rejected immediately.

---

## What I Would Improve

I would improve the depth of data management and the environment standards.

Currently, the assignment relies on a very small hardcoded dataset that does not fully test a candidate's data handling capabilities. I would suggest providing a larger realistic fake dataset (e.g. an SQL dump or CSV) that candidates must integrate into a database. This would allow the company to evaluate SQL proficiency, indexing strategies, and the ability to handle data at a more realistic scale.

Additionally, while Docker is an industry standard today, it is not required in the current task. I would improve this by requiring the project to be delivered as a containerized monorepo including frontend, backend, and database. This would better demonstrate the candidate's ability to manage production-ready environments and modern development workflows.