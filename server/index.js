const express = require('express');
const cors = require('cors');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// Health check endpoint
app.get('/health', (req, res) => {
    res.json({
        status: 'OK',
        service: 'InternShield Risk Analysis Engine',
        timestamp: new Date().toISOString(),
        aiKeyConfigured: Boolean(process.env.GEMINI_API_KEY || process.env.OPENAI_API_KEY)
    });
});

/**
 * POST /api/analyze
 * 
 * Request payload:
 * {
 *   "sender_status": "UNKNOWN",
 *   "message": "Congratulations! You have been selected...",
 *   "source": "WhatsApp"
 * }
 */
app.post('/api/analyze', async (req, res) => {
    try {
        const { sender_status, message, source } = req.body;

        if (!message) {
            return res.status(400).json({ error: "Missing required field 'message'" });
        }

        console.log(`[${new Date().toLocaleTimeString()}] Analyzing message via InternShield Risk Engine (Source: ${source || 'WhatsApp'}, SenderStatus: ${sender_status || 'UNKNOWN'})`);

        // Privacy Gate: Saved contacts are not analyzed
        if (sender_status === 'KNOWN') {
            return res.json({
                risk_score: 0,
                risk_level: "LOW",
                category: "SAVED_CONTACT",
                reasons: ["Sender is a saved contact", "Message skipped to preserve privacy"],
                recommendation: "Message from saved contact. No action required."
            });
        }

        const apiKey = process.env.GEMINI_API_KEY || process.env.OPENAI_API_KEY;

        if (apiKey) {
            try {
                const aiResult = await analyzeWithGemini(apiKey, message, sender_status, source);
                return res.json(aiResult);
            } catch (aiErr) {
                console.error("AI API call failed, using rule engine fallback:", aiErr.message);
            }
        } else {
            console.log("ℹ️ No API key set in server/.env — using InternShield 10-Point Rule Engine Fallback.");
        }

        // Fallback rule engine with 10 evaluation criteria
        const fallbackResult = analyzeWithRuleEngine(message, sender_status);
        return res.json(fallbackResult);

    } catch (err) {
        console.error("Error processing analysis request:", err);
        return res.status(500).json({ error: "Internal server error analyzing message" });
    }
});

/**
 * Calls Gemini API using the exact system prompt and 10 evaluation criteria specified by InternShield Risk Analysis Engine.
 */
async function analyzeWithGemini(apiKey, message, senderStatus, source) {
    const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${apiKey}`;

    const prompt = `You are the InternShield Risk Analysis Engine.

Analyze a message for potential internship, recruitment, job, phishing, or financial fraud.

Evaluate:
1. Payment requests (upfront fees, deposits, training charges)
2. Requests for OTP/password/bank information
3. Urgency or pressure (high-pressure language, tight deadlines)
4. Unrealistic salary/stipend (high pay for minimal effort)
5. Fake selection claims (offer letter without prior interview)
6. Suspicious URLs (typosquatted domains, suspicious shorteners)
7. Company/recruiter inconsistencies (unprofessional contact, generic claims)
8. Requests for identity documents (Aadhaar, PAN, bank statements via chat)
9. Common recruitment scam patterns (prepaid task, video like scam)
10. Unknown sender signal (sender is not in user's saved contacts)

Message Details:
- Source: ${source || 'WhatsApp'}
- Sender Status: ${senderStatus || 'UNKNOWN'}
- Content: "${message}"

Rules:
- Do not claim that a message is definitely a scam unless there is sufficient evidence.
- Use "potential scam" or "needs verification" when evidence is uncertain.
- Return ONLY valid JSON with no markdown formatting or extra text.

JSON Schema:
{
  "risk_score": <number 0-100>,
  "risk_level": "<LOW | MEDIUM | HIGH>",
  "category": "<short descriptive category e.g. POTENTIAL_INTERNSHIP_SCAM, PREPAID_TASK_SCAM, UNVERIFIED_OFFER, LEGITIMATE_OFFER>",
  "reasons": [
    "<reason 1>",
    "<reason 2>",
    "<reason 3>"
  ],
  "recommendation": "<actionable advice for the student>",
  "dos": [
    "<specific action to do, e.g., verify recruiter LinkedIn profile>",
    "<another specific action to do>"
  ],
  "do_nots": [
    "<specific action to avoid, e.g., do not pay security deposit>",
    "<another specific action to avoid>"
  ]
}`;

    const response = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            contents: [{
                parts: [{ text: prompt }]
            }]
        })
    });

    if (!response.ok) {
        throw new Error(`Gemini API HTTP Error ${response.status}: ${await response.text()}`);
    }

    const data = await response.json();
    const rawText = data.candidates?.[0]?.content?.parts?.[0]?.text || '';
    
    // Clean JSON markdown code blocks
    const cleanJson = rawText.replace(/```json/gi, '').replace(/```/g, '').trim();
    const parsed = JSON.parse(cleanJson);

    return {
        risk_score: Number(parsed.risk_score) || 50,
        risk_level: String(parsed.risk_level).toUpperCase() || 'MEDIUM',
        category: String(parsed.category) || 'POTENTIAL_RECRUITMENT_SCAM',
        reasons: Array.isArray(parsed.reasons) ? parsed.reasons : ["Suspicious message pattern detected"],
        recommendation: String(parsed.recommendation) || "Needs verification with official company sources.",
        dos: Array.isArray(parsed.dos) ? parsed.dos : ["Verify company info on official channels."],
        do_nots: Array.isArray(parsed.do_nots) ? parsed.do_nots : ["Do not share banking information or OTP."]
    };
}

/**
 * 10-Point Evaluation Rule Engine Fallback
 */
function analyzeWithRuleEngine(message, senderStatus) {
    const text = message.toLowerCase();

    const reasons = [];
    const dos = [];
    const doNots = [];
    let score = 0;

    // 10. Unknown sender signal
    if (senderStatus === 'UNKNOWN') {
        score += 15;
        reasons.push("Sender is an unknown number (not in saved contacts)");
    }

    // 1. Payment requests
    const hasPayment = text.includes('pay') || text.includes('fee') || text.includes('₹') || text.includes('rs.') || text.includes('deposit') || text.includes('registration');
    if (hasPayment) {
        score += 30;
        reasons.push("Upfront payment or registration fee requested");
        doNots.push("Pay registration fees, security deposits, or laptop charges");
        dos.push("Ask for a payment receipt or request a free alternative");
    }

    // 2. OTP/Password/Bank info requests
    const hasSensitiveData = text.includes('otp') || text.includes('password') || text.includes('bank account') || text.includes('card details') || text.includes('cvv') || text.includes('upi pin');
    if (hasSensitiveData) {
        score += 35;
        reasons.push("Request for sensitive OTP, password, or banking credentials");
        doNots.push("Share sensitive OTP, passwords, UPI PINs, or banking details");
        dos.push("Block this sender immediately and report the contact");
    }

    // 3. Urgency or pressure
    const hasUrgency = text.includes('today') || text.includes('immediately') || text.includes('now') || text.includes('urgent') || text.includes('limited time') || text.includes('24 hours');
    if (hasUrgency) {
        score += 20;
        reasons.push("High pressure urgency language used");
        doNots.push("Rush to reply or make hasty decisions under pressure");
        dos.push("Take your time to verify and discuss with a mentor or family");
    }

    // 4. Unrealistic salary/stipend
    const hasUnrealisticPay = text.includes('5000/day') || text.includes('per day') || text.includes('earn daily') || text.includes('part time earn');
    if (hasUnrealisticPay) {
        score += 25;
        reasons.push("Unrealistic salary or daily stipend promised for minimal effort");
        doNots.push("Believe high pay claims for simple tasks like video likes");
        dos.push("Search online for similar 'task scams' and block the sender");
    }

    // 5. Fake selection claims
    const hasFakeSelection = text.includes('selected') || text.includes('shortlisted') || text.includes('offer letter') || text.includes('congratulations');
    if (hasFakeSelection) {
        score += 15;
        reasons.push("Unverified selection claim without prior interview process");
        doNots.push("Accept the offer letter or share documents without formal interviews");
        dos.push("Verify the selection by calling the company's official HR");
    }

    // 6. Suspicious URLs
    const hasSuspiciousUrl = text.includes('http') || text.includes('bit.ly') || text.includes('t.me') || text.includes('.xyz') || text.includes('tinyurl');
    if (hasSuspiciousUrl) {
        score += 20;
        reasons.push("Contains suspicious external redirect link or shortened URL");
        doNots.push("Click on suspicious links or download files/apks from this URL");
        dos.push("Verify the link destination safely or check the domain name");
    }

    // 7. Company/recruiter inconsistencies
    const hasInconsistency = text.includes('telegram') || text.includes('whatsapp group') || text.includes('google form');
    if (hasInconsistency) {
        score += 15;
        reasons.push("Recruiter directs communication away from official channels to informal apps");
        doNots.push("Continue communication on Telegram/WhatsApp if redirected from formal sites");
        dos.push("Ask for an official company email address from their domain");
    }

    // 8. Identity document request
    const hasDocRequest = text.includes('aadhaar') || text.includes('pan card') || text.includes('id proof');
    if (hasDocRequest) {
        score += 15;
        reasons.push("Requests sensitive identity documents over messaging app");
        doNots.push("Send Aadhaar, PAN card, or bank statements via chat");
        dos.push("Ask why these documents are needed before official hiring");
    }

    // 9. Common scam patterns (task scam, like scam)
    const hasTaskScam = text.includes('like youtube') || text.includes('prepaid task') || text.includes('review rating');
    if (hasTaskScam) {
        score += 30;
        reasons.push("Matches common prepaid task / rating scam pattern");
        doNots.push("Pay money to unlock tasks or review requests");
        dos.push("Block this sender immediately and delete any links");
    }

    // Cap score at 100
    score = Math.min(score, 100);

    let riskLevel = "LOW";
    if (score >= 70) riskLevel = "HIGH";
    else if (score >= 35) riskLevel = "MEDIUM";

    let category = "UNVERIFIED_MESSAGE";
    if (riskLevel === "HIGH") {
        category = hasPayment ? "POTENTIAL_INTERNSHIP_SCAM" : "POTENTIAL_RECRUITMENT_FRAUD";
    } else if (riskLevel === "MEDIUM") {
        category = "NEEDS_VERIFICATION";
    } else {
        category = "LOW_RISK_MESSAGE";
    }

    let recommendation = "Message appears low risk. Practice standard verification before sharing details.";
    if (riskLevel === "HIGH") {
        recommendation = "High risk detected. Do not pay any fee, click external links, or share personal documents. Verify directly with official HR.";
    } else if (riskLevel === "MEDIUM") {
        recommendation = "Potential scam or unverified offer. Verify recruiter identity on LinkedIn and official company careers portal before responding.";
    }

    // Default fallbacks if empty
    if (dos.length === 0) {
        dos.push("Verify the company on official channels");
        dos.push("Cross-check the sender's identity");
    }
    if (doNots.length === 0) {
        doNots.push("Share sensitive personal information");
        doNots.push("Pay any upfront fees");
    }

    return {
        risk_score: score,
        risk_level: riskLevel,
        category: category,
        reasons: reasons.length > 0 ? reasons : ["Analyzed via InternShield 10-Point Engine"],
        recommendation: recommendation,
        dos: dos,
        do_nots: doNots
    };
}

app.listen(PORT, '0.0.0.0', () => {
    console.log(`====================================================`);
    console.log(`🛡️ InternShield Risk Analysis Engine running on port ${PORT}`);
    console.log(`📍 Endpoint: http://localhost:${PORT}/api/analyze`);
    console.log(`====================================================`);
});
