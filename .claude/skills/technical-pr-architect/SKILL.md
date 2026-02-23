---
name: technical-pr-architect
description: >
  Generate structured, professional PR descriptions from a code fix or problem
  description. Use when the user shares a diff, snippet, or describes a bug fix
  / feature and wants a ready-to-paste GitHub PR body.
---

# Technical PR Architect

Generate a high-quality pull request description from a code fix or problem description.

## Trigger

Activate when the user:
- Pastes a code diff, snippet, or commit
- Describes a bug fix or feature implementation
- Says something like "write a PR for this", "generate a PR description", or "PR body"

---

## Step 1 — Gate: Ensure Code Is Present

**Before generating anything**, check whether actual code (a diff, snippet, or implementation detail) has been provided.

- ✅ **Code is present** → proceed to Step 2.
- ❌ **Only a problem description** → ask:

  > "Got it! To write an accurate PR description — especially the Implementation Plan and Trade-offs — could you share the code change or diff? Even a rough snippet works."

  Wait for the user to provide code before continuing.

---

## Step 2 — Generate the PR Description

Produce a **single fenced Markdown code block** containing the raw PR body, ready to paste into GitHub/GitLab/Bitbucket.

Use this exact template structure inside the code block:

~~~
```markdown
## Summary

<!-- 2–4 sentence high-level overview. What changed and why. -->

## Implementation Plan

<!-- Numbered, step-by-step walkthrough of the fix logic.
     Be concrete: mention specific functions, files, or modules when visible. -->

1. 
2. 
3. 

## Technical Choices & Trade-offs

<!-- Explain key decisions. For each choice, state what was preferred and why,
     and what alternatives were considered or rejected. -->

| Decision | Chosen Approach | Alternative(s) Considered | Rationale |
|----------|----------------|--------------------------|-----------|
|          |                |                          |           |

## Risk & Rollback Plan

<!-- Identify potential failure modes and how to recover. -->

**Risks:**
- 

**Rollback:**
- 

## Verification

<!-- How to test this change. Include unit tests, manual steps, or commands. -->

- [ ] 
- [ ] 
```
~~~

---

## Content Guidelines

### Summary
- State *what* changed and *why* in plain language.
- Avoid implementation jargon — this section is for anyone reading the PR.

### Implementation Plan
- Number each step; each step should correspond to a logical unit of work.
- Reference filenames, functions, or classes visible in the provided code.
- If code is minimal or high-level, infer reasonable steps and flag assumptions with `<!-- assumed -->`.

### Technical Choices & Trade-offs
- Fill at least one row in the table.
- Common trade-off axes: performance vs. readability, correctness vs. speed, new dependency vs. inline solution, defensive vs. optimistic error handling.
- If the user's code clearly had only one reasonable approach, note that and explain why alternatives were ruled out.

### Risk & Rollback Plan
- List at least one concrete risk (e.g., "Edge case X may surface if Y is true").
- Rollback should be actionable: revert command, feature flag toggle, or migration down step.

### Verification
- Use GitHub task-list checkboxes (`- [ ]`).
- Include at minimum: one automated test step and one manual smoke-test step.
- If test commands are visible from the repo context, include them literally.

---

## Constraints

- **Never include secrets, tokens, passwords, or credentials** in the output — not even as placeholders like `YOUR_API_KEY`. If the code contains secrets, redact them and add a comment: `<!-- ⚠️ Secret redacted — do not commit credentials -->`.
- Output must be a **single fenced code block** containing raw Markdown. No prose before or after the block except a one-line intro like "Here's your PR description:".
- If information for a section cannot be inferred, leave the placeholder comment in place rather than fabricating details.

---

## Example Invocation

**User input:**
> Here's my fix for the N+1 query bug in `user_service.py` — I replaced the loop with a bulk `select_related` call.

**Expected behavior:**
- Populate Summary with the N+1 fix context.
- Implementation Plan lists: identified loop, replaced with `select_related`, verified queryset output.
- Trade-offs table: eager loading vs. lazy loading.
- Risk: potential memory pressure on large result sets.
- Verification: Django test + manual admin page load check.
