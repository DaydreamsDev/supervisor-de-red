# Supervisor de Red

> [English](README.md) | [Español](README-es.md)

Desktop network traffic visualizer for Linux, macOS, and Windows. Capture packets
from any network interface, filter them with preset or custom BPF (Berkeley Packet Filter) expressions, and
watch the traffic live as raw packet text, a packets-vs-time chart, and a protocol
distribution pie chart.

Originally developed in 2015 as a university project, updated and delivered in 2026.

![Supervisor de Red — live traffic charts](docs/screenshot-graphs.png)

## Features

- List network interfaces with description, link type, MAC (Media Access Control), IP (Internet Protocol), mask, and broadcast
- Capture with start/stop and a scrollable raw packet view
- Preset filters: HTTP, HTTPS, FTP, SMTP, ICMP, ARP — plus a free-form BPF filter
- Live charts: packets vs. time and protocol distribution (TCP / UDP / ARP / ICMP)
- Save and load capture sessions
- Modern FlatLaf interface (light and dark), Spanish UI by default with English available

## Requirements

- Java 21 or later
- A capture backend:
  - Linux: `libpcap` (usually preinstalled) and root or `CAP_NET_RAW` permission
  - macOS: root permission
  - Windows: [Npcap](https://npcap.com/)

## Build

```bash
./mvnw clean package
```

The shaded runnable jar is produced at `target/supervisor-de-red-0.1.0.jar`.

## Run

```bash
java -jar target/supervisor-de-red-0.1.0.jar
```

Capture requires the privileges listed above.

## Test

```bash
./mvnw test
```

The suite runs without native capture or root privileges.

## Documentation

- [User guide](docs/user-guide.md) — first capture, filters, charts, troubleshooting
- [Architecture](docs/architecture.md) — layers, EDT (Event Dispatch Thread) safety, package map
- [Tech stack and standards](docs/tech-stack.md) — versions, naming conventions, TDD (test-driven development)
- [Contributing](CONTRIBUTING.md)
- [Changelog](CHANGELOG.md)

## License

[MIT](LICENSE) — see [LICENSE-es.md](LICENSE-es.md) for the Spanish text.
