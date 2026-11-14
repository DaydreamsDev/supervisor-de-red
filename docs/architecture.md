# Architecture

> [Español](architecture-es.md)

Supervisor de Red is a layered desktop application with a **UI-free, fully testable core**
and a **single controller that is safe for the EDT (Event Dispatch Thread)** as the only glue
between Swing and the services.
Packets flow from one capture worker thread into immutable records; the UI reads
snapshots, never shared mutable state. Every layer is independently testable.

## The layers

```
UI layer — EDT (Event Dispatch Thread) only
  MainFrame (view) · ui.chart panels · ui.Messages (i18n bundles)
  SupervisorController (mediator)

  action events ▲                          │ Swing Timer / invokeLater
                │                          ▼

Services — Swing-free, unit-testable
  capture     CaptureService → Pcap4jCaptureService
  filter      FilterPreset, FilterValidator
  statistics  ProtocolCounter → CaptureStatistics snapshots
  io          SessionStore → JsonSessionStore

  immutable PacketRecords ▲                │ listener callbacks
                          │                ▼

Capture worker thread — pcap4j loop, never touches Swing
  domain — ProtocolType, PacketRecord, CaptureStatistics (immutable records)
```

## Package map

| Package | Responsibility | Key types |
|---------|----------------|-----------|
| `domain` | Immutable core data shared by every layer | `ProtocolType`, `PacketRecord`, `CaptureStatistics` |
| `statistics` | Event-driven protocol counting | `ProtocolCounter` |
| `filter` | Preset filters and BPF (Berkeley Packet Filter) validation | `FilterPreset`, `FilterValidator`, `FilterValidationResult` |
| `capture` | Device listing and capture lifecycle | `CaptureService`, `Pcap4jCaptureService`, `DeviceInformation`, `PacketListener`, `PacketRecordMapper` |
| `io` | Save and load capture sessions | `SessionStore`, `JsonSessionStore` |
| `ui` | Swing view, controller, i18n | `MainFrame`, `SupervisorController`, `Messages` |
| `ui.chart` | Live charts rendered from `CaptureStatistics` | `TimeSeriesChartPanel`, `ProtocolPieChartPanel` |

Every package depends only on `domain` (and its own layer); no package knows
Swing except `ui` and `ui.chart`.

## EDT (Event Dispatch Thread) safety

The capture worker thread runs the pcap4j loop and is the only place that
touches native capture. It never touches Swing:

```
capture worker — not the EDT (Event Dispatch Thread)
   │ immutable PacketRecord
   ├──► ProtocolCounter (synchronized) ──► CaptureStatistics snapshot
   └──► bounded concurrent deque (session buffer, drop-oldest at 10 000)
             │ refreshed every 1000 ms by a javax.swing.Timer
             ▼
   Swing Timer runs on the EDT (Event Dispatch Thread) ──► MainFrame output area, status bar, charts
```

- One capture worker thread (`ExecutorService`), stop via `breakLoop()` with a
  bounded shutdown timeout — no hand-rolled threads, no busy-wait.
- A **1000 ms Swing Timer** refreshes the display from the bounded deque: it
  renders the last 500 records into the output area, updates the packet count
  in the status bar, and refreshes the charts from a fresh `CaptureStatistics`
  snapshot. The deque retains packets, so **Save session** persists the whole
  buffered capture instead of an already-consumed queue.
- The deque is bounded so a slow UI can never exhaust memory.

## Controller / view separation

| Part | Does | Never does |
|------|------|------------|
| `MainFrame` | Renders controls, panels, dialogs; fires events | Business logic, capture, statistics |
| `SupervisorController` | Listens to view events, calls services, routes results back | Layout, styling |
| Services | Capture, filter, count, persist | Swing |

The controller is unit-tested with a fake `CaptureService` and synthetic
`CaptureStatistics`; the view is tested with those same fakes.

## Statistics from structured records, never from text

Protocol statistics are computed from structured `PacketRecord`s produced at
capture time. `ProtocolCounter` accumulates them into `CaptureStatistics` snapshots,
and the charts render those snapshots.

Rendered text is presentation only: the output area can change format without
affecting statistics, and no code path turns rendered text back into data.

## Immutable domain records

`PacketRecord` and `CaptureStatistics` are Java records with validated constructors:

- Safe to share between the capture worker and the EDT (Event Dispatch Thread) — no locks needed for reads.
- Snapshots are point-in-time values; the UI always renders a consistent picture.
- `PacketRecord` holds `capturedAt`, `protocol`, `sizeInBytes`, and `summaryLine`.

## Decisions at a glance

| Decision | Why |
|----------|-----|
| Layered packages, `domain` at the bottom | Testable core, single dependency direction |
| One controller as the only glue | No logic in views, no Swing in services |
| Records for captured data and statistics | Immutability makes cross-thread sharing trivial |
| Swing Timer + bounded deque | UI updates on the EDT (Event Dispatch Thread) without unbounded memory |
| SLF4J for logging, dialogs for actionable errors | Never swallow, never `System.out` |

## Next step

- [Tech stack and standards](tech-stack.md)
- [User guide](user-guide.md)
