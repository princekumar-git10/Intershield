# 🛡️ InternShield — AI-Powered Cyber Defense & Scam Detection for WhatsApp

**InternShield** is a real-time cybersecurity Android application designed to protect students and job seekers from fake internships, job scams, task scams, and malicious URLs delivered via WhatsApp. 

It features an on-device/cloud hybrid AI detection engine, 24/7 background notification monitoring, contact intelligence to ignore safe contacts, and a futuristic Cyber AI Guard design.

---

## ✨ Key Features

- 🤖 **AI-Powered Scam & Phishing Detection**: Multi-layer heuristics and LLM-powered semantic analysis detecting upfront fee demands, fake offer letters, Telegram redirect scams, and suspicious links.
- ⚡ **24/7 Real-Time Background Protection**: Runs via Android's `NotificationListenerService` to immediately inspect WhatsApp messages from unknown senders without requiring user intervention.
- 👥 **Smart Contact Intelligence**: Automatically skips trusted contacts and focuses exclusively on unknown numbers and potential threats.
- 🔥 **Cyber AI Guard UI/UX**: Solar orange & Obsidian glassmorphism theme, real-time safety scores, interactive threat cards, risk breakdowns, and manual scan console.
- 🌐 **Backend AI Analyzer**: Node.js microservice integrating Google Gemini AI and pattern detection heuristics for deep threat intelligence.

---

## 📱 Tech Stack

### Android App
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose & Material 3
- **Architecture**: MVVM + Clean Architecture with Coroutines & StateFlow
- **Background Service**: Android `NotificationListenerService`, `BroadcastReceiver` (Auto-rebind on boot)
- **Local Persistence**: Room Database / Android Keystore
- **Networking**: Retrofit & OkHttp

### Backend Server
- **Runtime**: Node.js / Express
- **AI Engine**: Google Gemini API & Regex Threat Pattern Matching
- **Security**: CORS, Helmet, Rate Limiting

---

## 🚀 Getting Started

### 1. Backend Server Setup
```bash
cd server
npm install
cp .env.example .env # Add your GEMINI_API_KEY
npm start
```

### 2. Android App Build
```bash
# Clone the repository
git clone https://github.com/princekumar-git10/Intershield.git
cd Intershield

# Build Debug APK
./gradlew assembleDebug
```

---

## 🔒 Permissions & Privacy
- `BIND_NOTIFICATION_LISTENER_SERVICE`: To read incoming WhatsApp notification text in real-time.
- `READ_CONTACTS`: Used strictly locally to verify if a sender is already in your address book (safe) or an unknown sender.
- **Privacy First**: Contact data never leaves the device. Only suspicious unknown sender message text is evaluated for safety indicators.

---

## 👨‍💻 Author
- GitHub: [@princekumar-git10](https://github.com/princekumar-git10)
