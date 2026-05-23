# USSD and Notifications Guide

This document explains how the current USSD flow works, how the callback is reached, and which notifications are emitted by the backend.

## USSD flow

The backend exposes a form-encoded callback endpoint:

- `POST /api/ussd`
- `POST /api/ussd/session`

The request should include:

- `sessionId`
- `phoneNumber`
- `serviceCode`
- `text`

The backend currently responds with a text menu using the Africa's Talking USSD format:

- `CON` means continue the session.
- `END` means terminate the session.

## Supported short codes

By default, the backend accepts these three service codes:

- `*384#`
- `*385#`
- `*386#`

You can replace them by setting `USSD_SERVICE_CODES` in the environment.

## Accessing the USSD app

1. Configure the short code with your telco or Africa's Talking account.
2. Point the callback URL to the backend endpoint.
3. Dial the short code from a phone.
4. The backend returns the next step based on the `text` chain.

## Current menu flow

1. Welcome screen.
2. View nearby parking.
3. Choose a parking option.
4. Choose number of slots.
5. Confirm booking.

This is still a lightweight MVP menu. It should be expanded later with real reservation and payment calls if you want end-to-end USSD booking.

## Notifications emitted by the backend

The notification service currently logs SMS output and exposes these methods conceptually:

- booking confirmation
- overtime warning
- overtime charge notice
- host booking notice

The admin SMS endpoint is:

- `POST /api/ussd/sms`

It is protected with `ADMIN` authority.

## What to wire next

- Replace log-only SMS delivery with Africa's Talking SMS or another provider.
- Send a password reset message or email from the forgot-password flow.
- Add scheduled reminders before reservation expiry.
- Add webhook-driven notification events for payment success and checkout completion.