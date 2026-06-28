#  VoiceIDE — AI-Powered, Speech-First Developer Environment

> Write code by talking. Powered by Google Gemini LLM + Java Spring Boot + React.

VoiceIDE Banner

Java
Spring Boot
Gemini

##  What is VoiceIDE?

VoiceIDE is a full-stack, AI-powered IDE where you write, review, test, and run code **entirely using your voice**. Instead of typing, you speak natural language commands — and a Google Gemini LLM orchestrated through a Java Spring Boot backend does the rest.

This isn't autocomplete. This is a **chained LLM agent** that understands developer intent.

```
Voice Input → Spring Boot REST API → Google Gemini LLM → Structured Code Output → React IDE
```

---

##  Features

| Voice Command | What Happens |
|---|---|
| `"Write a binary search in Python"` | LLM generates code, streamed to Monaco Editor |
| `"Review my code for bugs"` | Gemini returns severity-tagged issues with explanations |
| `"Write unit tests for this function"` | LLM infers context, generates a full test file |
| `"Translate this to Java"` | Multi-language code translation via LLM |
| `"Run it, then explain the output"` | Chained execution: code runs, output explained by AI |
| `"Hey Volt"` | Wake word activates always-listening mode |

---

##  Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT (React)                        │
│   Monaco Editor │ xterm.js Terminal │ Web Speech API (STT)  │
└───────────────────────────┬─────────────────────────────────┘
                            │ WebSocket / REST
┌───────────────────────────▼─────────────────────────────────┐
│                  BACKEND (Java Spring Boot)                   │
│   Voice Command Parser │ LLM Orchestrator │ Code Runner       │
│   Async Command Queue  │ WebSocket Handler │ MySQL Persistence│
└───────────────────────────┬─────────────────────────────────┘
                            │ API Call
┌───────────────────────────▼─────────────────────────────────┐
│                    AI LAYER (Google Gemini)                   │
│   Code Generation │ Bug Review │ Test Writing │ Translation   │
│   Structured JSON Prompting │ Chained Agent Pattern           │
└─────────────────────────────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│              COLLABORATION (Y.js CRDT)                       │
│         Real-time multi-user conflict-free sync              │
└─────────────────────────────────────────────────────────────┘
```

---

##  LLM Integration (The Core)

VoiceIDE's intelligence lives in how Spring Boot orchestrates the Gemini LLM:

### Prompt Engineering
All LLM calls use **structured JSON prompts** to ensure deterministic, parseable responses:

```java
// Example: Bug review prompt structure
String prompt = """
    You are a senior code reviewer. Analyze the following code and return ONLY a JSON array.
    Each item must have: { "severity": "HIGH|MEDIUM|LOW", "line": <number>, "issue": "...", "fix": "..." }
    
    Code:
    %s
    """.formatted(userCode);
```

### Chained Agent Pattern
Multi-step commands like *"Run it, then explain the output"* are broken into a sequential async queue:

```
[EXECUTE_CODE] → [CAPTURE_OUTPUT] → [EXPLAIN_OUTPUT via Gemini] → [STREAM_TO_CLIENT]
```

### Latency Strategy
LLMs have inherent latency — VoiceIDE hides it with:
- **Optimistic UI updates** — progress shown before Gemini responds
- **Token-by-token streaming** — user sees output appearing in real time
- **Async command queuing** — no blocking between chained steps

---

##  Tech Stack

### Backend
| Technology | Purpose |
|---|---|
| Java 21 + Spring Boot 3 | REST API, WebSocket server, LLM orchestration |
| Google Gemini API | Code generation, debugging, testing, translation |
| WebSocket (STOMP) | Real-time bidirectional communication |
| MySQL | User sessions, code history, command logs |
| Docker | Containerized deployment |

### Frontend
| Technology | Purpose |
|---|---|
| React 18 + TypeScript | UI framework |
| Monaco Editor | VS Code-grade code editor in browser |
| xterm.js | Embedded terminal for code execution output |
| Y.js (CRDT) | Real-time multi-user collaboration |
| Web Speech API | Browser-native STT — zero cost, zero setup |

### Infrastructure
| Service | Purpose |
|---|---|
| Netlify | Frontend hosting (free tier) |
| Render | Backend + MySQL hosting (free tier) |

---

##  Getting Started

### Prerequisites
- Java 21+
- Node.js 18+
- MySQL 8+
- Google Gemini API Key → [Get one free](https://makersuite.google.com/app/apikey)

### 1. Clone the repo

```bash
git clone https://github.com/yourusername/voiceide.git
cd voiceide
```

### 2. Backend Setup

```bash
cd backend

# Configure environment variables
cp .env.example .env
# Add your GEMINI_API_KEY, DB credentials to .env

# Run with Maven
./mvnw spring-boot:run
```

**`application.properties`**
```properties
gemini.api.key=${GEMINI_API_KEY}
gemini.model=gemini-pro

spring.datasource.url=jdbc:mysql://localhost:3306/voiceide
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
```

### 3. Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

**`.env.local`**
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws
```

### 4. Open in browser

```
http://localhost:5173
```

Say **"Hey Volt"** to activate wake word mode, or click the mic button and start speaking.

##  API Reference

### Voice Command Endpoint
```http
POST /api/voice/command
Content-Type: application/json

{
  "transcript": "Write a binary search in Python",
  "language": "python",
  "sessionId": "abc123"
}
```

**Response:**
```json
{
  "type": "CODE_GENERATION",
  "code": "def binary_search(arr, target):\n    ...",
  "explanation": "Binary search with O(log n) complexity",
  "executionReady": true
}
```

### Code Review Endpoint
```http
POST /api/voice/review
Content-Type: application/json

{
  "code": "...",
  "language": "python"
}
```

**Response:**
```json
{
  "issues": [
    { "severity": "HIGH", "line": 12, "issue": "...", "fix": "..." },
    { "severity": "LOW",  "line": 5,  "issue": "...", "fix": "..." }
  ]
}
```

### WebSocket Events
```
/topic/code-update     → Real-time code sync (Y.js)
/topic/ai-stream       → Streaming LLM output tokens
/topic/terminal-output → Code execution results
```

##  Real-Time Collaboration

VoiceIDE uses **Y.js CRDT (Conflict-free Replicated Data Type)** for multi-user editing:

- Multiple developers can edit the same file simultaneously
- No conflicts — changes merge automatically
- Works over WebSocket via Spring Boot STOMP broker
- Every keystroke (and voice command output) syncs in under 100ms

##  Project Structure

```
voiceide/
├── backend/
│   ├── src/main/java/com/voiceide/
│   │   ├── controller/        # REST + WebSocket controllers
│   │   ├── service/
│   │   │   ├── GeminiService.java      # LLM orchestration
│   │   │   ├── VoiceCommandParser.java # Intent detection
│   │   │   ├── CodeExecutionService.java
│   │   │   └── CommandQueueService.java # Async chaining
│   │   ├── model/             # JPA entities
│   │   └── config/            # WebSocket, CORS, security config
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── Editor/        # Monaco Editor wrapper
│   │   │   ├── Terminal/      # xterm.js terminal
│   │   │   ├── VoicePanel/    # Mic UI + transcript display
│   │   │   └── ReviewPanel/   # AI bug review output
│   │   ├── hooks/
│   │   │   ├── useVoice.ts    # Web Speech API hook
│   │   │   └── useCollaboration.ts  # Y.js hook
│   │   └── services/
│   │       ├── api.ts         # REST client
│   │       └── websocket.ts   # STOMP WebSocket client
│   └── package.json
│
└── docker-compose.yml
```

---

##  Supported Languages

VoiceIDE can generate, review, test, and execute code in:

`Python` · `Java` · `C` · `C++` · `JavaScript` · `TypeScript`

##  Roadmap

- [ ] Support for OpenAI GPT-4o and Claude as alternative LLMs
- [ ] GitHub integration (commit by voice)
- [ ] Custom wake word training
- [ ] Mobile PWA support
- [ ] VS Code extension

##  Contributing

Pull requests are welcome! For major changes, please open an issue first.

```bash
git checkout -b feature/your-feature
git commit -m "feat: add your feature"
git push origin feature/your-feature
```

  Built with ☕ Java, 🧠 Gemini, and 🎙️ a lot of talking to my laptop
</p>
