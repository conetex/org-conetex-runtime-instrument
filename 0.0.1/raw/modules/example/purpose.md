# Purpose of the `example` Module

The `example` module provides **demonstration code** that shows how the instrumentation system behaves in practice.  
Its responsibilities include:

- offering runnable examples for developers
- illustrating how counters and cost metrics appear in real code
- serving as a reference for testing and experimentation

In essence:

> The `example` module demonstrates how the instrumentation framework works when applied to real Java classes.

---

## Out of Scope (Handled by Other Modules)

- **Implementing instrumentation logic**  
  Implemented in: [`counter`](../counter/purpose.md), [`metrics-cost`](../../../metrics-cost/purpose.md), [`metrics-cost-unnamed`](../../../metrics-cost-unnamed/purpose.md)

- **Collecting or aggregating measurement data**  
  Implemented in: [`collection`](../collection/purpose.md)

- **Defining shared instrumentation interfaces**  
  Implemented in: [`interfaces`](../../../interfaces/purpose.md)

- **Activating the instrumentation system**  
  Implemented in: [`agent`](../agent/purpose.md)
