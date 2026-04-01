---
name: Verify and Update Documentation
description: Guidelines to ensure that the documentation (README, architecture, etc.) is kept up to date with code modifications.
---

# Verify and Update Documentation

In this project, we aim to maintain high-quality and accurate documentation. Every code modification should trigger a review of the existing documentation to ensure it remains consistent with the implementation.

## Guidelines

1. **Check for Documentation Impact**: Before finalizing a task, ask yourself:
   - Does this change a public API or a REST endpoint?
   - Does this add, remove, or modify a configuration property in `application.properties`?
   - Does this change the underlying data model (Firestore, Beans)?
   - Does this add a new feature or dependency?

2. **Files to Monitor**:
   - `README.md`: Ensure installation steps, configuration, and feature descriptions are current.
   - `docs/architecture.excalidraw`: Update if the high-level architecture changes.
   - `CHANGELOG.md`: Record significant changes, bug fixes, or performance improvements.
   - `application.properties`: Ensure all new properties are documented or have sensible defaults.

3.  **Update Process**:
    - If a change is significant, update the relevant file immediately.
    - If you add a new configuration property, ensure it's explained in the `README.md` or a dedicated configuration section.
    - If you optimize a workflow (like the stats refresh), consider if the "How it works" section in the documentation needs a refresh.

4. **Self-Verification**:
   - Verify that the updated documentation is syntactically correct (Markdown).
   - Ensure that all links and references in the documentation are still valid.
