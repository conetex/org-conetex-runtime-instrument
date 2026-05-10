# Purpose of the `interfaces` Module

The `interfaces` module defines the **shared APIs and contracts** used across all instrumentation modules.  
Its responsibilities include:

- defining common interfaces for counters, cost metrics, and data collectors
- providing stable abstractions for instrumentation components
- ensuring loose coupling between modules

In essence:

> The `interfaces` module is the shared contract layer that all instrumentation modules depend on.

---

## Out of Scope (Handled by Other Modules)

- **Implementing counters or cost metrics**  
  Implemented in: [`counter`](../counter/purpose.md), [`metrics-cost`](../../../metrics-cost/purpose.md)

- **Collecting or aggregating measurement data**  
  Implemented in: [`collection`](../collection/purpose.md)

- **Performing bytecode transformation**  
  Implemented in: [`metrics-cost`](../../../metrics-cost/purpose.md), [`metrics-cost-unnamed`](../../../metrics-cost-unnamed/purpose.md), [`counter`](../counter/purpose.md)

- **Activating the instrumentation system**  
  Implemented in: [`agent`](../agent/purpose.md)

- **Providing example usage**  
  Implemented in: [`example`](../example/purpose.md)
