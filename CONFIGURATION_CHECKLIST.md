# ParkShare Kigali Configuration Checklist

Use this as the deployment checklist for the backend and the frontend integration points.

## Required environment variables

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`
- `APP_FRONTEND_URL`
- `FLUTTERWAVE_PUBLIC_KEY`
- `FLUTTERWAVE_SECRET_KEY`
- `FLUTTERWAVE_SECRET_HASH`
- `USSD_SERVICE_CODES`
- `RESET_TOKEN_TTL_MINUTES`

## Flutterwave

1. Set `FLUTTERWAVE_SECRET_KEY` in Render.
2. Set `FLUTTERWAVE_SECRET_HASH` to the webhook verification hash from Flutterwave.
3. Confirm `APP_FRONTEND_URL` points to the deployed frontend so redirect URLs resolve correctly.
4. Register the webhook URL as `POST /payments/webhook` on the backend.

Frontend integration:

- Create the reservation first.
- Call the payment initiation endpoint.
- Redirect the user to the returned checkout URL.
- Poll payment status before showing the QR pass.

## Africa's Talking

1. Put the Africa's Talking username and API key into the backend environment if SMS is being enabled beyond log output.
2. Configure the sender ID.
3. Point the USSD callback to `POST /api/ussd` or `POST /api/ussd/session`.

Frontend integration:

- Use the admin staff or notification screens to trigger SMS actions.
- The current implementation logs SMS payloads, so production SMS still needs a real provider wiring.

## USSD short codes

The backend accepts three configured USSD service codes by default:

- `*384#`
- `*385#`
- `*386#`

You can override them with `USSD_SERVICE_CODES=*384#,*385#,*386#` or your own values.

## Password recovery

1. Set `APP_FRONTEND_URL` so reset links point back into the frontend.
2. If you add email delivery later, wire your provider into the forgot-password path.
3. Keep `RESET_TOKEN_TTL_MINUTES` at a short value, such as 30.

## CORS

Add both your localhost and deployed frontend origins to `ALLOWED_ORIGINS` on Render.

Examples:

- `http://localhost:5173`
- `https://your-frontend.vercel.app`

## Database

- Do not edit old Flyway migrations.
- Add new migrations only.
- Deploy the new migration before enabling the new auth and admin screens in production.