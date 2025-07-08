# Bitespeed Customer Identity Tracking Solution

A Spring Boot application that identifies and tracks customer identities across multiple purchases using email and phone number matching.

## Features

- Identity resolution via email/phone matching
- Primary/secondary contact linking
- Contact merging logic
- REST API endpoint for identity resolution

## API Documentation

### Base URL
`https://your-app-name.onrender.com/api` (replace with your actual deployed URL)

### Endpoints

#### POST `/identify`
Identify or create a customer contact

**Request:**
```json
{
  "email": "string (optional)",
  "phoneNumber": "string (optional)"
}
