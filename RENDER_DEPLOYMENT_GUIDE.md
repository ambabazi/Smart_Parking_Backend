# Smart Parking - Render Deployment Guide

## Overview

This guide walks you through deploying your **Spring Boot backend** on [Render](https://render.com) and connecting it to your teammates' **React/Next.js frontend** on [Vercel](https://vercel.com).

**Architecture**:
- **Backend**: Spring Boot 3.2.5 + Java 21 + PostgreSQL → **Render**
- **Frontend**: React/Next.js → **Vercel** (teammate's repo)
- **Connection**: REST API with JWT authentication + CORS

---

## Prerequisites

✅ GitHub account (to authorize Render)  
✅ Render account (free tier available at https://render.com)  
✅ Access to teammates' frontend GitHub repo (you're already a collaborator)  
✅ JAR file built locally (`target/parking-0.0.1-SNAPSHOT.jar`)

---

## STEP 1: CORS Configuration (Already Done ✅)

Your backend now includes CORS headers to allow requests from Vercel.

**What changed**: `src/main/java/com/smart/parking/config/SecurityConfig.java`
- Added CORS bean that allows frontend domains
- Configured to use `ALLOWED_ORIGINS` environment variable
- Allows credentials (for JWT tokens)

**Local testing domains** (automatically allowed):
- `http://localhost:3000`
- `http://localhost:5173`

---

## STEP 2: Build JAR Locally

```bash
cd /home/aggie/IdeaProjects/Smart_Parking
mvn clean package -DskipTests
```

**Output location**: `target/parking-0.0.1-SNAPSHOT.jar` (~80-100MB)

This verifies your build is production-ready before pushing to Render.

---

## STEP 3: Push Code to GitHub

```bash
git add .
git commit -m "Add CORS configuration for Vercel frontend integration"
git push origin main
```

Render will watch your GitHub repo and auto-redeploy on push.

---

## STEP 4: Create PostgreSQL Database on Render

### 4.1 Sign Up & Create Database

1. Go to [https://render.com](https://render.com)
2. **Sign up with GitHub** (authorize access)
3. Go to **Dashboard** → **New+** → **PostgreSQL**

### 4.2 Configure Database

Fill in the form:

| Field | Value | Notes |
|-------|-------|-------|
| **Name** | `smart-parking-db` | For identification |
| **Database** | `smartparking` | Your app will use this DB name |
| **User** | `smartparking` | Database username |
| **Password** | Auto-generated | Copy and save this! |
| **Region** | Choose closest region | East Africa recommended |
| **Plan** | Free | 0.5GB RAM, 1GB storage |

### 4.3 Save Database Credentials

Once created, Render shows:
```
postgresql://smartparking:[password]@[host]:[port]/smartparking
```

**Copy and save**:
- ✅ Full connection URL
- ✅ Host
- ✅ Username
- ✅ Password
- ✅ Database name

You'll need these in Step 5.

---

## STEP 5: Create Web Service on Render
### Quick Deploy — step-by-step

Follow these exact steps to deploy the backend on Render. Copy/paste commands where shown.

Prerequisites
- GitHub repo connected to Render (Render should be authorized to your repo).
- The repo contains the `Dockerfile` (we use it to build the app).
- You have the Render Database External URL (or credentials) handy.

1) Create the Web Service
- Go to **Render Dashboard** → **New+** → **Web Service**
- Choose **Build and deploy from a Git repository**, connect your GitHub repo and select the branch (e.g. `main`).
- For **Runtime**, choose **Docker** (Render will use the repository `Dockerfile`).
- For **Name**, pick `smart-parking-api` or similar.
- Leave Build/Start Command blank — Dockerfile handles the build and run steps.

2) Configure Environment Variables (critical)
 - Open your Render Web Service → **Environment** → **Add Environment Variable** and add either Option A or Option B below.

  Option A — Quick (recommended)
  - Set `DATABASE_URL` to the External Database URL you copied from the Render Database page. Example:
    `postgres://user:password@host:5432/dbname`
  - The app will parse `DATABASE_URL` automatically and construct the JDBC connection.

  Option B — Explicit (optional)
  - Convert `DATABASE_URL` to explicit JDBC values and set these variables:
    - `DB_URL` = `jdbc:postgresql://host:5432/dbname`
    - `DB_USERNAME` = `user`
    - `DB_PASSWORD` = `password`
  - Use the helper script locally to convert (see step 3).

  Required additional env vars (add these too):
  - `JWT_SECRET` = a secure random string (minimum 32 chars)
  - `JWT_EXPIRATION` = `86400000` (default)
  - `ALLOWED_ORIGINS` = your frontend URL, for example `https://smart-parking-orpin.vercel.app`

  Important:
  - Use the origin only, without a trailing slash or any path.
  - If you have more than one frontend, separate origins with commas.

3) Convert `DATABASE_URL` (if using Option B)
 - Locally run the provided helper script to get the explicit values:

```bash
chmod +x scripts/convert_database_url.sh
./scripts/convert_database_url.sh 'paste-your-external-db-url-here'
```

 - The script prints `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`. Copy those into Render Environment if you chose Option B.

4) Deploy
 - Trigger a Manual Deploy from Render UI or push a commit to the branch Render watches.
 - Open **Logs** for the Web Service and watch startup.

5) What to expect in logs
 - Successful: Flyway connects, runs migrations, and you see Spring Boot start with an UP health endpoint.
 - Failure (common): If you see "Connection to localhost:5432 refused" or Flyway/Hikari errors, it means the service did not receive correct DB connection details. Common causes:
   - `DB_URL` not set and `DATABASE_URL` not set (app defaulted to `jdbc:postgresql://localhost:5432/...`).
   - `DATABASE_URL` value was not correctly pasted.
   - Network or permission issue to the DB host.

6) Quick troubleshooting checklist
 - Confirm Render Web Service → Environment contains either `DATABASE_URL`, or `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` exactly spelled.
 - Confirm `JWT_SECRET` is present.
- Confirm `ALLOWED_ORIGINS` matches the frontend origin exactly.
 - If still failing, try connecting to the DB from your machine (if publicly reachable):

```bash
psql 'postgres://user:password@host:5432/dbname'
```

 - As a temporary debug only: disable Flyway migrations so the app can start and you can run checks manually. Add env var:
   - `SPRING_FLYWAY_ENABLED=false`
   Then redeploy (do not leave this disabled in production).

7) Verify deployment (once logs show started)
 - Health check:

```bash
curl https://your-service-on-render.onrender.com/actuator/health
```

 - API test: Try a simple API endpoint (e.g., `/parking-spaces/nearby` depending on your routes).

Notes
 - The repository's `Dockerfile` builds a Java 21 image and runs the JAR; Render's Docker runtime is the correct choice.
 - The included `scripts/convert_database_url.sh` prints the explicit env vars if you prefer to set them individually.


---

## STEP 6: Set Environment Variables

Once deployed, go to **Web Service Settings** → **Environment**.

⚠️ **All database & JWT secrets must be in environment variables** (never hardcoded).

### Add These Variables

```env
# Database (from Step 4)
DB_URL=postgresql://smartparking:PASSWORD@HOST:5432/smartparking
DB_USERNAME=smartparking
DB_PASSWORD=YOUR_DATABASE_PASSWORD

# JWT (Security)
JWT_SECRET=your-random-32-character-string-here-minimum
JWT_EXPIRATION=86400000

# Frontend CORS
ALLOWED_ORIGINS=https://yourteam-frontend.vercel.app

# Africa's Talking (SMS service)
AT_USERNAME=sandbox
AT_API_KEY=your_sandbox_key
AT_SENDER_ID=SmartPark
AT_ENV=sandbox
```

### Troubleshooting: "Connection to localhost:5432 refused" during startup

If your Render deployment fails during startup with errors like "Connection to localhost:5432 refused" (Flyway or HikariCP stack traces), the app is unable to reach the PostgreSQL instance. Common causes:

- The database environment variables are missing or in the wrong format.
- `DB_URL` is not a JDBC URL. The application expects a JDBC URL like `jdbc:postgresql://HOST:PORT/DB`.
- The database host/port are unreachable from the Render service (wrong host, firewall, or wrong region).

Render-managed Postgres commonly exposes a `DATABASE_URL` value in this form:

```
postgres://user:password@host:5432/dbname
```

Spring Boot's JDBC driver needs a URL starting with `jdbc:postgresql://`. On Render you should set these environment variables for the web service (Environment → Add Variable):

- `DB_URL`: `jdbc:postgresql://host:5432/dbname`
- `DB_USERNAME`: `user`
- `DB_PASSWORD`: `password`

If you only have `DATABASE_URL` from Render, you can either convert it locally (or use the included helper script) and then paste the three values into Render's Environment editor. Example (bash):

```bash
# Example: convert Render's DATABASE_URL into JDBC pieces
RENDER_DATABASE_URL='postgres://user:pass@host:5432/dbname'
RENDER_NO_PREFIX=${RENDER_DATABASE_URL#postgres://}
USERPASS=${RENDER_NO_PREFIX%%@*}
HOST_PORT_DB=${RENDER_NO_PREFIX#*@}
DB_USER=${USERPASS%%:*}
DB_PASS=${USERPASS#*:}
DB_HOST=${HOST_PORT_DB%%/*}
DB_NAME=${HOST_PORT_DB#*/}
DB_URL="jdbc:postgresql://${DB_HOST}/${DB_NAME}"
echo "DB_URL=$DB_URL"
echo "DB_USERNAME=$DB_USER"
echo "DB_PASSWORD=$DB_PASS"
```

Copy the printed `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` into Render's web service Environment variables and redeploy.

Note: The application now accepts `DATABASE_URL` directly — you can set `DATABASE_URL` to Render's External URL and the app will parse it at startup. If you prefer explicit vars, set `DB_URL` (JDBC), `DB_USERNAME`, and `DB_PASSWORD` instead.

Additional checklist if problem persists:

- Confirm the DB host/port are correct and reachable (same region reduces latency).
- Ensure `DB_USERNAME`/`DB_PASSWORD` are correct and the user has permission to run migrations.
- If using a private DB or VPC, make sure your Render service has network access to it.
- Temporarily disable Flyway (`spring.flyway.enabled=false`) to let the app start while you debug connectivity (not a production fix).

Flyway runs at application startup, so any unreachable DB will cause the boot to fail fast and exit with status 1. Fixing the env var format and connectivity normally resolves the error.


### Generate JWT_SECRET

```bash
# On your local machine, generate a secure random string
openssl rand -base64 32
```

Example output:
```
a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0=
```

**Save this value as `JWT_SECRET`** in Render environment variables.

---

## STEP 7: Verify Deployment

### 7.1 Get Your Render URL

Once deployed successfully, Render assigns you a public URL:

```
https://smart-parking-api-xxxxx.onrender.com
```

(Replace `xxxxx` with your auto-generated ID)

### 7.2 Test Backend

**Test health check** (public endpoint):
```bash
curl https://smart-parking-api-xxxxx.onrender.com/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

**Test login endpoint**:
```bash
curl -X POST https://smart-parking-api-xxxxx.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test@example.com","password":"password123"}'
```

**Test protected endpoint** (requires JWT token from login):
```bash
curl https://smart-parking-api-xxxxx.onrender.com/api/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## STEP 8: Share Backend URL with Teammates

Send your teammates:
```
Backend URL: https://smart-parking-api-xxxxx.onrender.com
```

They'll add it to their frontend environment variables.

---

## STEP 9: Frontend Integration (For Your Teammates)

Your teammates need to update their frontend `.env.local` file:

### For Vite/React:
```env
VITE_API_URL=https://smart-parking-api-xxxxx.onrender.com
```

### For Next.js:
```env
NEXT_PUBLIC_API_URL=https://smart-parking-api-xxxxx.onrender.com
```

### Update API Calls

**Before** (hardcoded localhost):
```javascript
const API_URL = "http://localhost:8080";
```

**After** (uses environment variable):
```javascript
const API_URL = process.env.VITE_API_URL || process.env.REACT_APP_API_URL || "http://localhost:8080";
```

### Example API Call:
```javascript
const loginUser = async (username, password) => {
  const response = await fetch(`${API_URL}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include", // Important for cookies
    body: JSON.stringify({ username, password })
  });
  
  const data = await response.json();
  
  // Store JWT token
  localStorage.setItem("token", data.token);
  
  return data;
};
```

### Store & Use JWT Token:
```javascript
// When making authenticated requests
const fetchUserProfile = async () => {
  const token = localStorage.getItem("token");
  
  const response = await fetch(`${API_URL}/api/users/profile`, {
    headers: {
      "Authorization": `Bearer ${token}`,
      "Content-Type": "application/json"
    },
    credentials: "include"
  });
  
  return response.json();
};
```

---

## STEP 10: Deploy Frontend to Vercel

Your teammates should:

1. Go to [Vercel.com](https://vercel.com)
2. **Import Project** → Select their GitHub repo
3. Add the same environment variables:
   ```
   VITE_API_URL=https://smart-parking-api-xxxxx.onrender.com
   ```
4. **Deploy**

---

## STEP 11: Test End-to-End Integration

### Check Network Traffic

1. Open frontend in browser
2. Open **Developer Tools** → **Network** tab
3. Try to login
4. Verify requests go to:
   ```
   https://smart-parking-api-xxxxx.onrender.com/api/auth/login
   ```
   (NOT localhost:8080)

### Verify CORS Works

If you see CORS errors in console:
```
Access to XMLHttpRequest blocked by CORS policy
```

**Solution**: 
- Double-check `ALLOWED_ORIGINS` env var matches your Vercel domain
- Verify SecurityConfig.java CORS bean is included

---

## Troubleshooting

### Backend Won't Start

**Check logs** in Render dashboard:
```bash
# Common issues:
# 1. Database connection failed
#    → Verify DB_URL, DB_USERNAME, DB_PASSWORD are correct
#    → Check PostgreSQL is running on Render

# 2. Flyway migration failed
#    → Check database schema exists
#    → Verify V1__init_schema.sql ran successfully

# 3. Port already in use
#    → Render assigns PORT automatically, no need to set it
```

### Frontend Can't Reach Backend

**Check browser console** for CORS errors:
1. Verify `ALLOWED_ORIGINS` includes your Vercel domain
2. Restart backend after changing env vars (Render auto-redeploys)
3. Clear browser cache: `Ctrl+Shift+Delete`

### JWT Token Not Working

**Verify JWT_SECRET**:
1. In Render env vars, check `JWT_SECRET` is set
2. Ensure whitespace/special chars didn't get corrupted
3. Restart backend after changing JWT_SECRET

**Check token format**:
```javascript
// Token should be: "Bearer eyJhbGciOiJIUzI1NiIs..."
const token = localStorage.getItem("token");
console.log("Full token string:", token);
// Should NOT include "Bearer" prefix in localStorage
```

---

## Security Checklist

✅ **Database Credentials**: Stored as environment variables (not in code)  
✅ **JWT Secret**: 32+ characters, random, in env vars  
✅ **CORS Origins**: Only allows Vercel domain (not wildcard)  
✅ **Credentials**: `allowCredentials(true)` for JWT tokens  
✅ **HTTPS Only**: Render backend is HTTPS by default  
✅ **No Hardcoded Secrets**: Check `application.yaml` and `application-prod.yaml` use `${VAR_NAME}`

---

## Important Render Limitations (Free Plan)

⚠️ **Cold Starts**: Service spins down after 15 minutes of inactivity → first request takes ~30 seconds  
⚠️ **Storage**: Limited to 1GB for database  
⚠️ **Traffic**: Fair usage policy applies  
⚠️ **Uptime**: 99% SLA (may have brief outages)

**Upgrade to Standard ($7/month) to avoid cold starts**.

---

## Environment Variables Reference

| Variable | Purpose | Example |
|----------|---------|---------|
| `PORT` | Server port | Leave blank (auto-assigned) |
| `DB_URL` | PostgreSQL connection | `postgresql://user:pass@host:5432/db` |
| `DB_USERNAME` | Database user | `smartparking` |
| `DB_PASSWORD` | Database password | Safe random string |
| `JWT_SECRET` | JWT signing key | 32+ char random string |
| `JWT_EXPIRATION` | Token expiry (ms) | `86400000` (24 hours) |
| `ALLOWED_ORIGINS` | CORS allowed domains | `https://vercel-domain.vercel.app` |
| `AT_USERNAME` | Africa's Talking user | `sandbox` or your username |
| `AT_API_KEY` | Africa's Talking API key | Your API key |
| `AT_SENDER_ID` | SMS sender ID | `SmartPark` |
| `AT_ENV` | AT environment | `sandbox` or `production` |

---

## Next Steps

1. ✅ CORS config updated (done)
2. ⏳ Commit & push code to GitHub
3. ⏳ Create PostgreSQL database on Render
4. ⏳ Create Web Service on Render
5. ⏳ Set environment variables
6. ⏳ Test backend endpoints
7. ⏳ Share URL with teammates
8. ⏳ Teammates update frontend `.env`
9. ⏳ Teammates deploy to Vercel
10. ⏳ Test end-to-end integration

---

## Quick Reference Commands

```bash
# Build locally
mvn clean package -DskipTests

# Test backend health
curl https://smart-parking-api-xxxxx.onrender.com/actuator/health

# Generate JWT secret
openssl rand -base64 32

# View Render logs
# → Open Render dashboard → Web Service → Logs
```

---

## Support

- **Render Docs**: https://render.com/docs
- **Spring Boot CORS**: https://spring.io/guides/gs/rest-service-cors/
- **Vercel Docs**: https://vercel.com/docs
- **Flyway Migrations**: https://flywaydb.org/documentation

---

**Last Updated**: May 14, 2026  
**Backend**: Spring Boot 3.2.5 + Java 21  
**Database**: PostgreSQL  
**Status**: ✅ Ready for deployment
