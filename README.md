# Vortex-VM: Distributed Bytecode Orchestrator

> A custom Java runtime framework that intercepts method calls at the JVM level and executes them transparently across remote machines — making distributed execution invisible to the developer.

---

## What Is Vortex-VM?

Vortex-VM is a runtime-level distributed execution engine built entirely from scratch in Java 21.

Instead of executing a method locally, Vortex-VM:

1. Intercepts the method call at the JVM level
2. Serializes the execution context into a network packet
3. Routes it to a remote worker node via TCP
4. Executes the method there using reflection
5. Returns the result transparently to the caller

The developer writes normal Java code. Vortex-VM handles everything else.

```java
// Developer writes this
@Distributed
public int compute(int x, int y) {
    return x + y;
}

// Vortex-VM intercepts, routes to worker, returns result
int result = service.compute(5, 10);  // returns 15, executed remotely
```

---

## Why This Project Exists

Vortex-VM was built to deeply understand:

- JVM internals — class loading, method invocation, reflection
- How Spring AOP and @Transactional work under the hood
- Dynamic proxy generation and bytecode manipulation
- Real distributed systems — serialization, TCP networking, orchestration
- Fault tolerance — heartbeat monitoring, retry logic, node recovery
- Java Instrumentation API and runtime bytecode interception

This is not a tutorial project. It is a systems engineering project that touches every layer of the JVM stack.

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                      CLIENT JVM                          │
│                                                          │
│   service.add(5, 10)                                     │
│         │                                                │
│         ▼                                                │
│   $Proxy0 / ByteBuddy Subclass                          │
│         │                                                │
│         ▼                                                │
│   DistributedInvocationHandler                           │
│         │                                                │
│         ▼                                                │
│   OrchestratorStrategy ──────────────────────────────┐  │
└──────────────────────────────────────────────────────┼──┘
                                                       │ TCP
                                                       ▼
┌─────────────────────────────────────────────────────────┐
│                   ORCHESTRATOR JVM                       │
│                                                          │
│   ┌─────────────┐        ┌───────────────────────────┐  │
│   │ NodeRegistry│        │      LoadBalancer          │  │
│   │             │        │   (Round Robin)            │  │
│   │ Worker 1 ✅ │───────▶│   picks best worker       │  │
│   │ Worker 2 ✅ │        │   forwards packet         │  │
│   │ Worker 3 ❌ │        │   returns result          │  │
│   └─────────────┘        └───────────────────────────┘  │
│                                                          │
│   HeartbeatMonitor (background daemon thread)            │
│   pings every worker every 5 seconds                     │
└──────────────────────────┬──────────────────────────────┘
                           │ TCP
             ┌─────────────┴────────────┐
             ▼                          ▼
┌────────────────────┐      ┌────────────────────┐
│   WORKER JVM 1     │      │   WORKER JVM 2     │
│   port 9091        │      │   port 9092        │
│                    │      │                    │
│  WorkerExecutor    │      │  WorkerExecutor    │
│  - resolves class  │      │  - resolves class  │
│  - invokes method  │      │  - invokes method  │
│  - returns result  │      │  - returns result  │
└────────────────────┘      └────────────────────┘
```

---

## Project Phases

Vortex-VM was built incrementally across six phases, each building on the previous.

### Phase 1 — Method Interception Engine
Built the core interception layer using JDK Dynamic Proxy and a custom `InvocationHandler`. Implemented `@Distributed` annotation detection on the implementation class (not the interface), null-safe argument logging, and dual-strategy routing — annotated methods go remote, non-annotated stay local.

**Key concepts:** JDK Dynamic Proxy, InvocationHandler, Runtime annotation processing, Reflection

### Phase 2 — Serialization Layer
Transformed method calls into serializable `ExecutionPacket` objects containing class name, method name, parameter types, arguments, and a UUID correlation ID. Simulated network transmission using Jackson JSON serialization and deserialization. Built `WorkerExecutor` with primitive type resolution via a static lookup map.

**Key concepts:** Jackson JSON, Serialization, Primitive type resolution, Correlation IDs

### Phase 3 — Real Distributed Execution
Replaced simulation with real TCP socket communication across two separate JVM processes. Built `NetworkSerializer` with length-prefixed framing (`DataOutputStream.writeInt` + `readFully`) to prevent TCP stream fragmentation. `WorkerServer` uses Java 21 virtual threads to handle concurrent connections.

**Key concepts:** TCP sockets, Length-prefix protocol, Message framing, Virtual threads (Java 21)

### Phase 4 — Orchestration Layer
Introduced a three-tier architecture with a dedicated Orchestrator process. Built `NodeRegistry` (thread-safe with `CopyOnWriteArrayList`), `LoadBalancer` (round-robin via `AtomicInteger`), and `OrchestratorServer` that routes every client request to the best available worker. Client never communicates with workers directly.

**Key concepts:** Three-tier architecture, Round-robin load balancing, Thread-safe data structures, Separation of concerns

### Phase 5 — Fault Tolerance
Added a `HeartbeatMonitor` daemon thread that pings every worker (including dead ones) every 5 seconds. Dead workers are automatically marked and skipped by the LoadBalancer. Orchestrator retries failed requests up to 3 times, cycling through available workers. Dead workers that recover are automatically marked ALIVE and re-enter the routing pool.

**Key concepts:** Heartbeat monitoring, Dead node detection, Recovery detection, Retry logic, Graceful degradation

### Phase 6 — Advanced Runtime Features
Replaced JDK Dynamic Proxy with ByteBuddy subclass generation — no interface required. Built a `DynamicClassLoader` that exposes `defineClass()` for runtime class definition. Added `classBytes` field to `ExecutionPacket` for bytecode transfer. Built a Java Instrumentation Agent (`VortexAgent`) that attaches to the JVM before `main()` and detects `@Distributed` methods at class load time without modifying source code.

**Key concepts:** ByteBuddy, Subclass proxy, Java Instrumentation API, ClassFileTransformer, Dynamic class loading, Parent delegation model

---

## Key Technical Concepts

### How Method Interception Works
```
service.add(5, 10)
        │
        ▼
$Proxy0.add(5, 10)                  ← JVM-generated class
        │  [static Method pre-resolved in <clinit>]
        ▼
DistributedInvocationHandler.invoke()
        │
        ├── resolves method on IMPL class (not interface)
        ├── detects @Distributed annotation
        ├── logs method name and arguments
        └── routes to ExecutionStrategy
                │
                ├── @Distributed → OrchestratorStrategy → TCP → Worker
                └── no annotation → LocalExecutionStrategy → method.invoke()
```

### TCP Framing Protocol
Raw TCP is a stream protocol with no message boundaries. Vortex-VM uses length-prefixed framing:
```
┌─────────────────┬──────────────────────────┐
│  4 bytes (int)  │  N bytes (JSON payload)  │
│  packet length  │  ExecutionPacket as JSON  │
└─────────────────┴──────────────────────────┘
```
The receiver reads 4 bytes first, then reads exactly that many bytes using `readFully()`. This guarantees complete message delivery regardless of TCP fragmentation.

### Annotation Lookup — Why Implementation, Not Interface
JDK Dynamic Proxy gives you the `Method` object from the interface. Annotations on the implementation class are invisible unless explicitly resolved:
```java
// Correct — looks up method on MathServiceImpl
Method targetMethod = target.getClass()
        .getMethod(method.getName(), method.getParameterTypes());
boolean isDistributed = targetMethod.isAnnotationPresent(Distributed.class);
```

### Primitive Type Resolution
`Class.forName("int")` throws `ClassNotFoundException` — primitives are not classes. Vortex-VM maintains a static lookup map:
```java
PRIMITIVE_TYPES.put("int", int.class);
PRIMITIVE_TYPES.put("long", long.class);
// ... all 8 primitives
```

### ByteBuddy vs JDK Proxy
| | JDK Dynamic Proxy | ByteBuddy |
|--|--|--|
| Requires interface | Yes | No |
| Generates | Class implementing interface | Subclass of target |
| Works on concrete classes | No | Yes |
| Works on final classes | No | No |
| Used by | Standard Java | Spring CGLIB, Mockito |

---

## Project Structure

```
src/main/java/com/vortexvm/
 ├── annotation/
 │    └── Distributed.java              @Distributed annotation
 ├── model/
 │    ├── ExecutionPacket.java           serializable unit of work
 │    └── ExecutionResult.java           serializable response
 ├── network/
 │    └── NetworkSerializer.java         TCP framing + JSON serialization
 ├── proxy/
 │    ├── handler/
 │    │    └── DistributedInvocationHandler.java
 │    ├── factory/
 │    │    └── ProxyFactory.java
 │    └── strategy/
 │         ├── ExecutionStrategy.java    interface
 │         ├── LocalExecutionStrategy.java
 │         ├── SimulatedRemoteStrategy.java
 │         ├── TcpRemoteStrategy.java
 │         └── OrchestratorStrategy.java
 ├── orchestrator/
 │    ├── NodeStatus.java
 │    ├── WorkerNode.java
 │    ├── NodeRegistry.java
 │    ├── LoadBalancer.java
 │    ├── HeartbeatMonitor.java
 │    └── OrchestratorServer.java
 ├── worker/
 │    ├── WorkerExecutor.java
 │    └── WorkerServer.java
 ├── byteBuddy/
 │    └── ByteBuddyProxyFactory.java
 ├── classloader/
 │    └── DynamicClassLoader.java
 ├── agent/
 │    └── VortexAgent.java
 └── service/
      ├── MathService.java
      ├── MathServiceImpl.java
      └── Main.java
```

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Java 21 | Core language, Virtual Threads (Project Loom) |
| ByteBuddy 1.14 | Runtime subclass generation |
| Jackson 2.17 | JSON serialization of execution packets |
| Java Instrumentation API | JVM agent, class load time interception |
| TCP Sockets | Real network communication across JVM processes |
| Maven | Build and dependency management |

---

## How to Run

### Prerequisites
- Java 21+
- Maven 3.6+

### Step 1 — Build
```bash
mvn clean package
mvn dependency:copy-dependencies
```

### Step 2 — Build Agent JAR
```bash
jar cfm target/vortex-agent-thin.jar src/main/resources/agent-manifest.txt \
    -C target/classes com/vortexvm/agent/VortexAgent.class
```

### Step 3 — Start Worker 1 (Terminal 1)
```bash
mvn exec:java -Dexec.mainClass=com.vortexvm.worker.WorkerServer -Dexec.args=9091
```

### Step 4 — Start Worker 2 (Terminal 2)
```bash
mvn exec:java -Dexec.mainClass=com.vortexvm.worker.WorkerServer -Dexec.args=9092
```

### Step 5 — Start Orchestrator (Terminal 3)
```bash
mvn exec:java -Dexec.mainClass=com.vortexvm.orchestrator.OrchestratorServer
```

### Step 6 — Run Client (Terminal 4)
```bash
# Without agent
mvn exec:java -Dexec.mainClass=com.vortexvm.Main

# With Java agent
java -javaagent:target/vortex-agent-thin.jar \
     --class-path "target/classes:target/dependency/*" \
     com.vortexvm.Main
```

### Expected Output

**Orchestrator:**
```
[NodeRegistry] Current workers:
  - localhost:9091 [ALIVE] (0 jobs)
  - localhost:9092 [ALIVE] (0 jobs)
[Orchestrator] Heartbeat monitor started
[Orchestrator] Started on port 9090
[LoadBalancer] Selected worker: localhost:9091 [ALIVE] (0 jobs)
[Orchestrator] Forwarded result: 15
[LoadBalancer] Selected worker: localhost:9092 [ALIVE] (0 jobs)
[Orchestrator] Forwarded result: 42
```

**Client:**
```
[VortexAgent] Agent attached to JVM
[VortexAgent] Detected @Distributed in class: com.vortexvm.service.MathServiceImpl

=== Test 1: add() ===
[Interceptor] Distributed method detected: add
[Interceptor] Arguments: 5 10
[Orchestrator] Result received: 15
Result: 15

=== Test 2: multiply() ===
Result: 12

=== Test 3: randomNumber() ===
[Interceptor] Distributed method detected: randomNumber
[Interceptor] No arguments passed
[Orchestrator] Result received: 42
Result: 42

=== Phase 6: ByteBuddy Proxy (No Interface) ===
[Interceptor] Distributed method detected: add
[Interceptor] Arguments: 20 30
[Orchestrator] Result received: 50
ByteBuddy Result: 50
ByteBuddy Multiply (local): 42
```

---

## Fault Tolerance Demo

To demonstrate fault tolerance:

1. Start both workers and the orchestrator
2. Run the client — both workers receive requests (round robin)
3. Kill Worker 9091 (`Ctrl+C` in Terminal 1)
4. Wait 6 seconds — heartbeat detects failure:
   ```
   [Heartbeat] Worker DEAD: localhost:9091 [DEAD] (0 jobs)
   ```
5. Run the client again — all requests route to Worker 9092 automatically
6. Restart Worker 9091 — heartbeat detects recovery:
   ```
   [Heartbeat] Worker recovered: localhost:9091 [ALIVE] (0 jobs)
   ```

---

## What This Project Taught

Building Vortex-VM from scratch produced deep understanding of:

- Why `Class.forName()` fails for primitives and how to solve it
- Why JDK proxy requires an interface and how ByteBuddy removes that constraint
- Why TCP needs framing and how length-prefix protocol solves fragmentation
- Why `Class.forName()` must never be called inside a `ClassFileTransformer`
- Why `CopyOnWriteArrayList` and `AtomicInteger` are necessary for thread safety
- Why parent delegation in `ClassLoader` prevents `ClassCastException`
- How Spring AOP, @Transactional, and Mockito work at the implementation level

---

## Comparison With Real Systems

| Vortex-VM Concept | Real World Equivalent |
|---|---|
| `@Distributed` annotation | Spring `@Transactional`, `@Async` |
| `InvocationHandler` | Spring AOP `MethodInterceptor` |
| `ExecutionPacket` | gRPC `proto` message, Java RMI stub |
| Length-prefix framing | HTTP `Content-Length`, Redis protocol |
| `NodeRegistry` | Consul service registry, Kubernetes etcd |
| `HeartbeatMonitor` | Kubernetes liveness probe, Consul health check |
| `LoadBalancer` | Nginx round-robin, AWS ALB |
| `VortexAgent` | Java profilers, APM agents (Datadog, NewRelic) |
| `DynamicClassLoader` | OSGi bundle loading, Tomcat webapp isolation |

---

## Author

Built as a deep-dive systems engineering project to understand JVM internals, distributed systems, and framework design from first principles.
