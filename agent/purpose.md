# Purpose of the `agent` Module

The `agent` module provides the entry point for enabling runtime instrumentation inside the JVM.  
Its responsibilities are limited to:

- registering the project as a Java Instrumentation Agent (`premain` / `agentmain`)
- initializing the instrumentation framework at JVM startup or attachment time
- activating and configuring the measurement subsystems provided by other modules

In essence:

> The `agent` module starts and configures the instrumentation system, but does not perform any bytecode transformation or measurement itself.

---

## Out of Scope (Handled by Other Modules)

- **Bytecode transformation**  
  Implemented in: [`metrics-cost`](../metrics-cost/purpose.md), [`metrics-cost-unnamed`](../metrics-cost-unnamed/purpose.md), [`counter`](../counter/purpose.md)

- **Injection of counters, timers, or cost‑measurement logic**  
  Implemented in: [`counter`](../counter/purpose.md), [`metrics-cost`](../metrics-cost/purpose.md)

- **Data collection and aggregation**  
  Implemented in: [`collection`](../collection/purpose.md)

- **Instrumentation APIs and shared interfaces**  
  Implemented in: [`interfaces`](../interfaces/purpose.md)

- **Instrumentation rules and transformation strategies**  
  Implemented in: [`metrics-cost`](../metrics-cost/purpose.md), [`counter`](../counter/purpose.md)

- **Example usage and demonstration code**  
  Implemented in: [`example`](../example/purpose.md)
