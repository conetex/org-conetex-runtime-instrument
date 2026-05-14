# Purpose of the `metrics-cost-unnamed` Module

The `metrics-cost-unnamed` module provides **cost measurement instrumentation for unnamed modules** (i.e., non‑JPMS classpath environments).  
Its responsibilities include:

- adapting cost‑measurement logic for environments without explicit module metadata
- providing a transformer compatible with unnamed modules
- producing measurement events identical to those from `metrics-cost`

In essence:

> The `metrics-cost-unnamed` module ensures that cost instrumentation works in traditional classpath‑based JVM applications.

---

## Out of Scope (Handled by Other Modules)

- **Collecting or aggregating cost data**  
  Implemented in: [`collection`](../collection/purpose.md)

- **Instrumentation for named JPMS modules**  
  Implemented in: [`metrics-cost`](../metrics-cost/purpose.md)

- **Counter‑based instrumentation**  
  Implemented in: [`counter`](../counter/purpose.md)

- **Defining shared instrumentation interfaces**  
  Implemented in: [`interfaces`](../interfaces/purpose.md)

- **Activating the instrumentation system**  
  Implemented in: [`agent`](../agent/purpose.md)

- **Providing example usage**  
  Implemented in: [`example`](../example/purpose.md)
