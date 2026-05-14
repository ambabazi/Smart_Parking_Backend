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

### 5.1 Create New Web Service

1. Go to **Dashboard** → **New+** → **Web Service**
2. Choose **"Build and deploy from a Git repository"**
3. Click **"Connect"** next to your GitHub repo

### 5.2 Configure Web Service

Fill in the form:

| Field | Value | Notes |
|-------|-------|-------|
| **Name** | `smart-parking-api` | Your service name |
| **Region** | Same as database | Reduce latency |
| **Branch** | `main` | Default branch |
| **Runtime** | `Java 21` | Select from dropdown |
| **Build Command** | `mvn clean package -DskipTests` | Maven build |
| **Start Command** | `java -jar target/parking-0.0.1-SNAPSHOT.jar` | Run the JAR |
| **Instance Type** | `Free` (or Standard) | Free has limitations |

### 5.3 Deploy

Click **Deploy** and wait 2-5 minutes.

**View logs** in Render dashboard to monitor the deployment.

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
✅ **No Hardcoded Secrets**: Check `application.properties` uses `${VAR_NAME}`

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
