# Tech Stack and Engineering Standards

> [Español](tech-stack-es.md)

Java 21 desktop application built with Maven, Swing, pcap4j, JFreeChart and
FlatLaf, plus the engineering standards that make the codebase portfolio-grade.

## Stack

| Concern | Choice | Version |
|---------|--------|---------|
| Language | Java | 21 (LTS) |
| Build | Maven (wrapper included) | wrapper `mvnw` |
| Capture | pcap4j (`pcap4j-core`, `pcap4j-packetfactory-static`) | 1.8.2 |
| Charts | JFreeChart | 1.5.6 |
| Look and feel | FlatLaf | 3.8 |
| Logging API | SLF4J | 2.0.20 |
| Logging backend | Logback (`logback-classic`) | 1.6.7 |
| JSON | Jackson (`jackson-databind`, `jackson-datatype-jsr310`) | 2.23.0 |
| Tests | JUnit 5 (`junit-jupiter`) | 5.14.4 |
| Assertions | AssertJ | 3.27.7 |
| Mocks | Mockito (`mockito-core`, `mockito-junit-jupiter`) | 5.24.0 |
| Packaging | maven-shade-plugin (runnable jar) | 3.6.2 |

Build helpers pinned in `pom.xml`: maven-compiler-plugin 3.13.0 (release 21),
maven-surefire-plugin 3.6.1. Build and test with `./mvnw clean package` and
`./mvnw test` — no local Maven install required.

## Naming conventions

Names carry intent; comments are a last resort.

| Rule | Example |
|------|---------|
| Self-documenting names; **almost no comments** | `startCapture`, not `startCapture` + explanation |
| **No abbreviations** — except universally understood acronyms | `temporaryDirectory`, not `tmpDir` |
| Protocol names — canonical, used as-is | `HTTP`, `HTTPS`, `FTP`, `SMTP`, `TCP`, `UDP`, `ARP`, `ICMP` — these are the protocol names themselves, not abbreviations of prose words |
| Standard technical acronyms — expanded on first use in prose | `id` (identifier), `MAC` (Media Access Control), `BPF` (Berkeley Packet Filter), `UI` (user interface), `IO` (input/output) |
| Library and tool names — proper names, used as-is | `pcap` (`libpcap`, `pcap4j`, `Npcap`) |
| **No single-letter variables** | `interfaceIndex`, `packetCount` |
| Classes: `PascalCase` nouns | `CaptureService`, `ProtocolCounter` |
| Methods: `camelCase` verbs | `listDevices`, `isRunning` |
| Constants: `SCREAMING_SNAKE_CASE` | `SNAPSHOT_LENGTH`, `READ_TIMEOUT_MILLIS` |
| Boolean predicates: `isX` / `hasX` / `canX` | `isCaptureActive`, `hasDevices` |
| Packages: lowercase layer names | `com.daydreamsdev.supervisor.capture` |
| Maximum line length | 100 columns |

If a name does not carry the intent, rename — do not add a comment. The only
comments allowed on source files are MIT license headers.

## Testing discipline

TDD (test-driven development) on all behavior: **RED → GREEN → REFACTOR**.

1. RED — write a failing test for the behavior first.
2. GREEN — implement the minimum to pass.
3. REFACTOR — clean up while the tests stay green.

Test method naming: `methodUnderTest_condition_expectedOutcome`.

```
countsTcpPackets_whenRecordsAreTcp_expectsOneTcp
```

- Domain and services: real unit tests (JUnit 5 + AssertJ).
- Controller: tested with a fake `CaptureService`; charts with synthetic `CaptureStatistics`.
- Tests never need native capture or root — native calls stay behind interfaces.

## Code hygiene

- **Zero-warning tolerance.** No warnings, no unused imports, no raw types,
  no unchecked conversions, no commented-out code, no TODO litter.
- **SLF4J, never `System.out`.** Logging goes through SLF4J + Logback;
  user-facing errors go through dialogs.
- Never swallow exceptions: log or surface, always with context.
- Magic strings live in constants or i18n resource bundles, not inline.

## Next step

- [Architecture](architecture.md)
- [Contributing](../CONTRIBUTING.md)
