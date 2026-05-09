# Purpose of the `metrics-cost` Module

The `metrics-cost` module implements **cost and timing measurement** for instrumented code.  
Its responsibilities include:

- defining cost metrics (e.g., execution time, resource usage)
- injecting measurement logic into bytecode via a transformer
- producing measurement events consumed by the `collection` module

In essence:

> The `metrics-cost` module provides the logic and bytecode transformation required to measure execution cost at runtime.

---

## Out of Scope (Handled by Other Modules)

- **Collecting or aggregating cost data**  
  Implemented in: [`collection`](../collection/purpose.md)

- **Providing counter‑based instrumentation**  
  Implemented in: [`counter`](../counter/purpose.md)

- **Defining shared instrumentation interfaces**  
  Implemented in: [`interfaces`](../interfaces/purpose.md)

- **Activating the instrumentation system**  
  Implemented in: [`agent`](../agent/purpose.md)

- **Providing example usage**  
  Implemented in: [`example`](../example/purpose.md)
