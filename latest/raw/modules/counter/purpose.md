# Purpose of the `counter` Module

The `counter` module provides **runtime counters** that can be injected into instrumented code.  
Its responsibilities include:

- defining counter types (e.g., invocation counters)
- implementing the logic for incrementing and tracking counts
- integrating with the instrumentation transformer to inject counter operations

In essence:

> The `counter` module supplies the logic for counting events and method invocations during runtime.

---

## Out of Scope (Handled by Other Modules)

- **Collecting and aggregating counter results**  
  Implemented in: [`collection`](../collection/purpose.md)

- **Activating the instrumentation system**  
  Implemented in: [`agent`](../agent/purpose.md)

- **Defining shared instrumentation interfaces**  
  Implemented in: [`interfaces`](../../../interfaces/purpose.md)

- **Injecting cost or timing measurements**  
  Implemented in: [`metrics-cost`](../../../metrics-cost/purpose.md), [`metrics-cost-unnamed`](../../../metrics-cost-unnamed/purpose.md)

- **Providing example usage**  
  Implemented in: [`example`](../../../example/purpose.md)
