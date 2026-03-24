# Chat API & MCP Integration: Engineering Brief

**Date:** 2026-03-25
**Branch:** `fix/grn-n-plus-one-query`

---

## Overview

This brief documents every failure, root cause, and fix encountered while integrating a Spring AI-backed chat endpoint (`POST /api/chat`) with an Ollama LLM and an MCP tool layer that exposes BlueSupply's procurement data to the model. Issues are ordered by the sequence in which they surfaced.

---

## 1. Application Wouldn't Start

### 1.1 PostgreSQL Not Running

**Error:**
```
Error creating bean with name 'jpaSharedEM_entityManagerFactory':
Unable to create requested service [JdbcEnvironment]
```

**Cause:** The PostgreSQL Docker container was not running. Spring Data JPA could not obtain a connection, so the entire application context failed to load.

**Fix:**
```bash
docker-compose -f supply-compose.yaml up -d
```

---

### 1.2 JWT Secret Key Too Short — Three Attempts

**Error (first attempt):**
```
WeakKeyException: The signing key's size is 168 bits which is not secure enough
for the HS256 algorithm. The JWT specification requires a minimum of 256 bits.
```

**Cause:** `jwt.secretKey=ForgetMeNot1!2@3#` — 21 ASCII characters × 8 bits = 168 bits. JJWT 0.9.1 enforces the JOSE spec minimum of 256 bits for HMAC-SHA256.

**First fix attempt:** Extended to 30 characters = 240 bits. Still failed.

**Second fix attempt:** Extended to 32+ characters = 256 bits minimum. Application started.

**Key insight:** Key strength is measured in bits, not characters. 32 ASCII characters = 256 bits, which is the exact minimum for HS256. Anything shorter fails hard at startup.

---

### 1.3 Missing CORS Property

**Error:**
```
Could not resolve placeholder 'cors.allowed-origins' in value "${cors.allowed-origins}"
```

**Cause:** `WebSecurityConfig` injected `@Value("${cors.allowed-origins}")` but the property was absent from `application.properties`.

**Fix:** Added to `application.properties`:
```properties
cors.allowed-origins=http://localhost:4000,http://localhost:3000,http://localhost:8080
```

---

### 1.4 Database Schema Missing — Flyway Not Run

**Error:**
```
relation "employee" does not exist
```

**Cause:** The project uses a dedicated `supply-db` Maven module for Flyway migrations. Running the application alone does not execute migrations. The database existed but was empty.

**Fix:**
```bash
./mvnw flyway:migrate -pl supply-db
```

This ran migrations V1–V12, creating all tables.

---

## 2. CORS Preflight Failure on the Chat Endpoint

With the application running, the React frontend (`localhost:4000`) attempted `POST /api/chat`. The browser sent an `OPTIONS` preflight first. It failed with no content.

**Cause:** Spring Security's CORS configuration only listed these allowed headers:
```java
List.of("Authorization", "Content-Type", "X-Requested-With")
```

SSE clients explicitly set `Accept: text/event-stream` to negotiate the streaming content type. Because `Accept` was absent from the allowed headers list, the browser's preflight was rejected and the actual request was never sent.

**Fix:**
```java
configuration.setAllowedHeaders(
    List.of("Authorization", "Content-Type", "X-Requested-With", "Accept")
);
```

Standard REST calls rarely need `Accept` in the preflight — this gap only surfaces for SSE.

---

## 3. 401 Unauthorized After CORS Fix

With CORS resolved, the request reached the server but returned `401` with `WWW-Authenticate: Basic`.

**Cause:** Extending `jwt.secretKey` to meet the 256-bit minimum invalidated all previously issued tokens. HMAC signatures are derived from the key — any token signed with the old key is cryptographically invalid against the new key.

**Fix:** Re-authenticate to obtain a new token:
```bash
POST /auth/login
```

**Note:** Changing `jwt.secretKey` is a silent breaking change equivalent to a key rotation. All active sessions are immediately logged out. In production this requires a coordinated rotation strategy.

---

## 4. MCP Tool `getLpoFunnel` Silently Dropped

Requests reached the controller and Spring AI processed them, but the `getLpoFunnel` tool was never invoked and never appeared in tool listings. The log showed:

```
Method getLpoFunnel is annotated with @Tool but returns a functional type. This is not supported.
```

**Cause:** The method was declared as:
```java
public Object getLpoFunnel() {
    return lpoReportService.getFunnel().orElse(null);
}
```

Spring AI's `MethodToolCallbackProvider` checks whether the return type is a functional interface before registering a tool. The guard is:
```java
Object.class.isAssignableFrom(Supplier.class) // → true
```

Because `Object` is the root supertype of every class including `Supplier<T>`, declaring the return type as `Object` causes Spring AI to classify the method as returning a functional type and silently exclude it from registration.

**Fix:**
```java
public ProcurementFunnel getLpoFunnel() {
    return lpoReportService.getFunnel().orElse(null);
}
```

Declaring the concrete return type passes the guard. `@Tool` methods must always declare their specific return type — `Object` will always be dropped.

---

## 5. Ollama 404 — Model Not Found

With authentication and tools working, every LLM call failed:
```
404 Not Found from POST http://localhost:11434/api/chat
```

**Two root causes:**

**5a. No model configured.** Spring AI defaults to `llama3.2` when `spring.ai.ollama.chat.model` is absent. `application.properties` had no such property.

**5b. No model pulled.** Even with the correct model name configured, Ollama has no models installed by default. The model binary must be explicitly downloaded.

**Fix:**
```properties
# application.properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.model=qwen2.5:7b
```

```bash
docker exec $(docker ps -qf "name=ollama") ollama pull qwen2.5:7b
```

`qwen2.5:7b` was chosen as a capable 7B-parameter model that fits within the available RAM. A RAM gate in `supply-compose.yaml` ensures Ollama only starts when ≥8 GB is available; if RAM is insufficient only Ollama is skipped — the application and all other features remain available.

---

## 6. `AuthorizationDeniedException` on Async SSE Dispatch

Authenticated requests reached the controller and the SSE stream started, but immediately crashed:

```
AuthorizationDeniedException: Access Denied
at AuthorizationFilter.doFilter
at AsyncContextImpl$AsyncRunnable.run
Unable to handle the Spring Security Exception because the response is already committed.
```

**Cause:** Spring Security's `AuthorizationFilter` runs on every request, including internal Tomcat re-dispatches. When `SseEmitter` writes its first chunk and Tomcat completes the async context, it re-dispatches the request internally with `DispatcherType.ASYNC`. On the async thread, `SecurityContextHolder` is empty — there is no JWT, because this is an internal dispatch, not an HTTP request. The `anyRequest().authenticated()` rule fires and throws `AuthorizationDeniedException`. By this point the response headers are already committed, so Spring Security cannot write a 403 — it logs the error and the stream silently breaks.

**Fix:**
```java
.authorizeHttpRequests(auth -> auth
    .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
    .requestMatchers(AUTH_LIST).permitAll()
    .requestMatchers("/auth/**").permitAll()
    .anyRequest().authenticated())
```

`ASYNC` covers Tomcat's async completion dispatches. `ERROR` covers the subsequent error page dispatch via `ApplicationDispatcher.doInclude` (see issue 7 below). The original `REQUEST` dispatch is still fully authenticated — this does not open any security hole.

---

## 7. NPE in `OllamaChatModel.from()` on Stream Completion

After the async fix, a new error appeared at the end of every successful stream:

```
NullPointerException: Cannot invoke "java.time.Duration.plus(java.time.Duration)"
because "evalDuration" is null
at OllamaChatModel.from(OllamaChatModel.java:174)
```

**Cause:** Ollama's streaming protocol only includes timing statistics (`eval_duration`, `prompt_eval_duration`) in the final chunk where `done: true`. Spring AI's `OllamaChatModel.from()` method tries to compute usage metrics by calling `.plus()` on `evalDuration` without a null check, so it NPEs when processing any non-final chunk. This is a bug in the Spring AI Ollama integration.

All response tokens had already been streamed successfully by the time the NPE occurred. However, the error caused Tomcat to dispatch to `/error` via `DispatcherType.INCLUDE` — which triggered a second `AuthorizationDeniedException` (the reason `ERROR` was added in fix 6).

**Fix:** Recover the stream before the NPE reaches the emitter:
```java
.stream()
.content()
.onErrorResume(NullPointerException.class, e -> {
    log.debug("Ollama stream closed with null usage stats (Spring AI known issue): {}", e.getMessage());
    return Flux.empty();
})
.subscribe(...)
```

`Flux.empty()` triggers the `onComplete` handler (`emitter.complete()`), closing the SSE stream cleanly instead of with an error.

---

## 8. Garbled Response — SSE Space Stripping

With the stream completing cleanly, responses arrived but with all spaces removed:

```
Ityouthecurrentprocurementormetricsyoupleasemorewhatinformationforexample
```

**Cause:** The SSE specification states: *"if the field value starts with a U+0020 SPACE character, remove it."* LLM tokenizers (BPE/SentencePiece) encode the inter-word space as a prefix on the following token — e.g., the word "provide" in a sentence arrives as the token `" provide"` (space + word). Spring's `SseEmitter.send(String)` writes `data: provide\n\n` (one space total between colon and word). The SSE parser strips that leading space, leaving `provide`. Every space in the entire response is lost.

**Attempted fix (incorrect):** Passing `MediaType.APPLICATION_JSON` to `SseEmitter.event().data()` was tried first. It failed because `StringHttpMessageConverter` takes priority over Jackson for `String + APPLICATION_JSON` and writes the string bytes raw — no JSON quoting, so spaces still stripped.

**Correct fix:** Use `ObjectMapper.writeValueAsString()` to produce a proper JSON string literal before sending:

```java
// Before (broken)
emitter.send(chunk);

// After (correct)
emitter.send(SseEmitter.event()
    .data(objectMapper.writeValueAsString(Map.of("content", chunk))));
```

`objectMapper.writeValueAsString(Map.of("content", " provide"))` produces `{"content":" provide"}`. The first character on the wire is `{`, not a space, so the SSE parser strips nothing. The space is preserved inside the JSON value.

Each event on the wire:
```
data:{"content":" provide"}

data:{"content":" information"}

data:[DONE]
```

Client-side handling:
```javascript
eventSource.onmessage = (e) => {
    if (e.data === '[DONE]') { eventSource.close(); return; }
    const { content } = JSON.parse(e.data);
    output += content;
};
```

---

## Summary of All Fixes

| # | Problem | Root Cause | Fix |
|---|---------|-----------|-----|
| 1 | App wouldn't start | PostgreSQL container not running | `docker-compose up -d` |
| 2a | `WeakKeyException: 168 bits` | JWT secret 21 chars (168 bits) | Extended to 32+ chars |
| 2b | `WeakKeyException: 240 bits` | Second attempt still 30 chars | Re-extended to exactly 32 chars |
| 3 | `cors.allowed-origins` not found | Property missing | Added to `application.properties` |
| 4 | `relation "employee" does not exist` | Flyway migrations never ran | `./mvnw flyway:migrate -pl supply-db` |
| 5 | CORS preflight rejected | `Accept` not in allowed headers | Added `"Accept"` to `setAllowedHeaders` |
| 6 | 401 on all requests | JWT key rotation invalidated tokens | Re-login to get new token |
| 7 | `getLpoFunnel` never registered | `Object` return type triggers functional-type guard | Changed return type to `ProcurementFunnel` |
| 8 | Ollama 404 | No model configured, no model pulled | Added `spring.ai.ollama.chat.model`, pulled model |
| 9 | `AuthorizationDeniedException` on async dispatch | Tomcat ASYNC re-dispatch lacks security context | Added `DispatcherType.ASYNC, ERROR` to `permitAll()` |
| 10 | NPE in `OllamaChatModel.from()` | Spring AI doesn't null-check `evalDuration` | Added `.onErrorResume(NullPointerException.class, ...)` |
| 11 | All spaces stripped from response | SSE spec strips leading space; LLM tokens are space-prefixed | Send `{"content":"..."}` JSON objects instead of raw strings |

---

## Key Takeaways

1. **JWT secret changes are session-breaking.** Every active token is invalidated immediately. Production key rotation requires coordinated re-authentication.
2. **Spring AI silently drops `@Tool` methods with `Object` return type.** Always declare the concrete return type.
3. **SSE requires `Accept` in CORS allowed headers.** Standard REST configurations miss this because non-SSE clients don't send `Accept` in preflights.
4. **Tomcat async re-dispatches run through Spring Security with an empty context.** Any `SseEmitter` usage requires `DispatcherType.ASYNC` to be explicitly permitted. Error-handling dispatches require `DispatcherType.ERROR`.
5. **SSE strips one leading space from every `data:` field value.** LLM tokens are space-prefixed by design. The only reliable fix is to JSON-encode the content so the first character is `"`, not a space.
6. **Spring AI's `OllamaChatModel` NPEs on non-final stream chunks.** `eval_duration` is only present on the final `done: true` chunk — intermediate chunks have null timing fields. This is a Spring AI bug; work around it with `.onErrorResume(NullPointerException.class, ...)`.
7. **Flyway in a separate Maven module does not run automatically.** A fresh database will have no schema unless `./mvnw flyway:migrate -pl supply-db` is run explicitly.
8. **The default Ollama model is `llama3.2`.** It must be overridden in `application.properties` and the model must be pulled before first use.
