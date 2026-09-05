# InternShield AI Backend Service

Lightweight Node.js Express backend server for InternShield threat analysis.

## Security Architecture
The Android app **never** embeds the AI API key inside the APK binary. Instead, the Android app calls this backend server, which securely communicates with Gemini / OpenAI API using server-side environment variables.

## How to Run

1. Navigate to the `server` directory:
   ```bash
   cd server
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. (Optional) Create `.env` file and set your `GEMINI_API_KEY`:
   ```bash
   cp .env.example .env
   ```
4. Start the server:
   ```bash
   npm start
   ```

## Endpoint Specification

- **URL**: `POST /api/analyze`
- **Request Body**:
  ```json
  {
    "sender_status": "UNKNOWN",
    "message": "Congratulations! You have been selected for Google Android Internship. Pay ₹999 registration fee today.",
    "source": "WhatsApp"
  }
  ```
- **Response Body**:
  ```json
  {
    "risk_score": 94,
    "risk_level": "HIGH",
    "category": "INTERNSHIP_REGISTRATION_SCAM",
    "reasons": [
      "Upfront payment requested",
      "Urgent language",
      "Unverified selection claim"
    ],
    "recommendation": "Do not pay any fee, click links, or share personal documents."
  }
  ```
