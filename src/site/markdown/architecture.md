# Architecture Overview

This document provides a high‑level architectural overview of the runtime instrumentation framework.  
It summarizes the purpose of each module, shows how they interact, and includes diagrams to visualize the system.

For detailed responsibilities and boundaries, each module has its own `purpose.md` file.

---

# 📐 System Architecture

The system is composed of several independent but cooperating modules.  
Together, they implement a modular Java runtime instrumentation framework based on the JVM Instrumentation API.

---

# 🧩 Module Responsibilities (Summary)

| Module | Purpose |
|--------|---------|
| [`agent`](agent/purpose.md) | Activates and configures the instrumentation system (JVM agent entry point). |
| [`collection`](collection/purpose.md) | Collects and aggregates measurement data. |
| [`counter`](counter/purpose.md) | Implements runtime counters and injects counter logic. |
| [`interfaces`](interfaces/purpose.md) | Defines shared APIs and contracts. |
| [`metrics-cost`](metrics-cost/purpose.md) | Implements cost and timing measurement for JPMS modules. |
| [`metrics-cost-unnamed`](metrics-cost-unnamed/purpose.md) | Cost instrumentation for unnamed/classpath modules. |
| [`example`](example/purpose.md) | Demonstrates how instrumentation behaves in real code. |
| [`tests`](tests/purpose.md) | Validates correctness and stability of the instrumentation system. |

---

# 🔗 Module Dependency Diagram

```plantuml
@startuml
title Module Overview — Class Instrumentation

left to right direction

component agent as A
component interfaces as B
component counter as C
component "metrics-cost" as D
component "metrics-cost-unnamed" as E
component collection as F

' Core modules
A --> B
A --> C
A --> D
A --> E
A --> F

' Instrumentation logic
C --> B
D --> B
E --> B

' Data flow
C --> F
D --> C
E --> C
@enduml
```

# 🧭 Module Roles Diagram

```mermaid
flowchart TB

    A[agent\nActivation Layer] --> B[interfaces\nShared API]
    A --> C[counter\nCounters]
    A --> D[metrics-cost\nCost Metrics]
    A --> E[metrics-cost-unnamed\nCost Metrics Unnamed]
    A --> F[collection\nData Collection]

    C --> F
    D --> F
    E --> F

    G[example\nDemonstration] --> A
    G --> C
    G --> D
    G --> E
    G --> F

    H[tests\nValidation] --> A
    H --> C
    H --> D
    H --> E
    H --> F
```

# 🔧 Class Instrumentation Sequence

```mermaid
sequenceDiagram
autonumber

    participant JVM as JVM ClassLoader
    participant Agent as agent<br/>premain/agentmain
    participant Trans as Instrumentation<br/>Transformer Registry
    participant MC as metrics-cost<br/>Bytecode Transformer
    participant Co as counter<br/>Data Collector
    participant App as Application Class

    JVM->>Agent: Start JVM with -javaagent<br/>or attach at runtime
    Agent->>Trans: Register ClassFileTransformer(s)
    Note right of Agent: agent does NOT transform bytecode<br/>only registers and configures

    JVM->>Trans: load / reload Classes not transformed yet (e.g. "Application Class")
    Note right of Trans: metrics-cost / counter<br/>modify bytecode
    Trans->>MC: delegate transform(classBytes)

    MC-->>Trans: return transformedClassBytes
    Trans-->>JVM: return transformedClassBytes

    JVM->>App: redefineClass("Application Class")

    App->>Co: emit measurement events<br/>(increase counters)
    Co-->>Co: calculate metrics
```

<script type="module" src="js/mermaid-init.js"></script>