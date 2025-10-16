# Sentinel — Backend (Spring Boot): WebSocket Ingestion & Grog AI Streaming

<div align="center">
  <img src="assets/sentinel-backend-hero.png" alt="Sentinel Backend Banner" width="780">
</div>

<div align="center">
  <img src="https://img.shields.io/badge/Runtime-Spring%20Boot-2F933A">
  <img src="https://img.shields.io/badge/Transport-WebSockets-black">
  <img src="https://img.shields.io/badge/AI-Grog%20API-111111">
  <img src="https://img.shields.io/badge/License-MIT-blue">
</div>

> **TL;DR**  
> This backend accepts a **WebSocket** connection from the client, receives **screen captures** (images or short clips), forwards them to the **Grog API** for analysis, and **streams results back** over the same socket in real time.  
> Use it as the ingestion + orchestration layer for the Sentinel client.

---

## 🎬 Demo / Docs

- **Product Demo:** _add your video link here_  
- **Frontend (Sentinel client):** _add link here_  
- **This Backend (Spring Boot):** you’re here

> The demo should show: client connects via WS → sends screen frames → backend calls Grog → backend streams findings back → user sees live insights.

---

## 🧠 What This Backend Does

- **WebSocket Gateway**
  - Upgrades client connections and authenticates sessions (token/header).
  - Receives **screen data** (frames, snippets, or references to uploaded media).
  - Applies flow control (max FPS, max payload) and backpressure.

- **AI Orchestrator (Grog)**
  - Sends incoming frames/snippets to **Grog API**.
  - Requests: visual OCR, topic detection, key entities/links, action item extraction, and short summaries.
  - Streams partial and final **AI findings** back to the same WebSocket.

- **Session Timeline (Optional)**
  - Emits timestamped “findings” events (summary/entity/task/quote/link/keyframe).
  - (If you enable storage) can persist minimal metadata for later search.

- **Operational Guardrails**
  - Per-client rate limits and quotas.
  - Size/FPS caps, content-type checks, and timeouts on AI calls.
  - Graceful degradation: when AI is slow, keep socket alive with heartbeats.

---

## 🏗️ Architecture Overview

**Client** ⟷ **Spring Boot (WS Gateway + Orchestrator)** ⟶ **Grog API**

1. **Connect:** Client opens a WS to the backend and authenticates.  
2. **Ingest:** Client sends screen frames/snippets (binary or base64 payloads).  
3. **Process:** Backend forwards to **Grog** and receives streaming analysis.  
4. **Stream Back:** Backend sends incremental **events** to the client (findings + statuses).  
5. **Close/Save:** Client ends session; optional metadata is stored for history.

---

## 🔌 WebSocket Contract (Conceptual)

**Connection**
- URL: `wss://<your-domain>/ws/sentinel`
- Auth: bearer token (recommended) or signed query.

**Inbound message types (client → server)**
- **`session.start`** — starts a capture session (title/tags/consent flags, capture mode, optional retention policy).
- **`frame.push`** — sends a frame or snippet (timestamp, media type/size; payload may be binary or an object storage URL).
- **`session.note`** — optional user note/context to bias AI analysis.
- **`session.end`** — ends the session and flushes any pending analysis.

**Outbound message types (server → client)**
- **`session.ack`** — session accepted (session id, limits, negotiated features).
- **`analysis.progress`** — percent/progress text; heartbeat when idle.
- **`analysis.finding`** — structured insight (summary, entity, task, quote, link, keyframe) with timestamps.
- **`analysis.error`** — recoverable error with reason and suggested next step.
- **`session.final`** — final rollup (short summary, entities, action items, references).

> Message bodies should include `type`, `ts` (server timestamp), and `data`.  
> Payloads must respect your configured **max frame size** and **max FPS**.

---

## 🔄 Processing Flow (Simplified)

1. **Start:** Client sends `session.start`. Backend validates token, returns `session.ack`.  
2. **Stream In:** Client sends `frame.push` messages as the screen updates.  
3. **AI Calls:** Backend batches/debounces frames (to control cost) and calls **Grog**.  
4. **Stream Out:** As Grog yields results, backend emits `analysis.finding` events.  
5. **Finalize:** Client sends `session.end` or socket closes; backend emits `session.final`.

---

## 🔐 Privacy, Safety & Consent

- **Explicit Consent:** Client must indicate consent flags in `session.start`; the server rejects sessions without them.
- **Scoping:** Encourage clients to limit to a specific window/monitor; support “pause” and “mask” (blur regions) client-side.
- **Security:** TLS for transport; token-based auth; server-side validation on all payloads.
- **Retention:** By default, treat frames as transient (analyze-and-discard). If you enable storage, use signed URLs and per-user access policies.
- **PII Minimization:** Optional on-device redaction before sending frames.

---

## ⚙️ Configuration (Key Settings)

- **Auth:** accepted issuers/audiences, token header/query name.  
- **WebSocket:** max connections per user, max FPS, max payload size, heartbeat/ping interval, idle timeout.  
- **Grog API:** endpoint base URL, API key/secret, model/feature flags, request timeouts, concurrency.  
- **Storage (optional):** object store credentials for keyframe uploads; signed URL TTL.  
- **Observability:** structured logging, per-event metrics, sampling for large sessions.

(Define these as environment variables in your deployment; keep secrets out of the repo.)

---

## 🧭 Client Expectations

- Open a single **WebSocket** per active session.  
- Respect backpressure: if the server signals slow-down, reduce FPS or batch frames.  
- Send small deltas or compressed frames to reduce bandwidth/cost.  
- Always end sessions with `session.end` to receive a `session.final` rollup.  
- Reconnect logic: resume with a new session or reattach by id if you implement it.

---

## 🗂 Optional Minimal Data Model (If You Persist Metadata)

- **sessions** — id, user_id, title/tags, consent flags, started_at, ended_at.  
- **events** — id, session_id, kind (`analysis.finding|progress|error|final`), payload, ts.  
- **keyframes** — id, session_id, uri, ts, hash (for dedupe), redaction mask.  

> Keep frames ephemeral unless you’ve explicitly chosen to store them and updated your privacy notice.

---

## 🛡️ Reliability & Ops

- **Backoff & Retry:** Wrap Grog calls with timeouts and exponential backoff.  
- **Circuit Breaker:** Trip when external errors spike to protect the socket pool.  
- **Quota Controls:** Per-user caps (daily minutes, max frames) to control spend.  
- **Audit:** Log session lifecycle and analysis summaries (not full frame contents).

---

## 🔗 Links

- **Frontend (Sentinel client):** _add link here_  
- **Demo Video:** _add link here_  

---

## 📄 License

MIT © You
