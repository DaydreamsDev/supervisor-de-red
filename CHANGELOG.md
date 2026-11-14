# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-11-14

### Added

- Network traffic visualizer published under the MIT license.
- Packet capture with pcap4j: device listing (description, link type, MAC,
  IP, mask, broadcast) and graceful start/stop lifecycle.
- Preset BPF filters — HTTP (80), HTTPS (443), FTP (21), SMTP (25), ICMP, ARP —
  plus a validated free-form BPF special filter with enable/disable state.
- Live charts: packets vs. time and protocol distribution pie
  (TCP / UDP / ARP / ICMP).
- Save and load capture sessions as JSON via file chooser.
- FlatLaf look and feel with light (Claro) and dark (Oscuro) themes.
- Bilingual UI — Spanish by default, English available from the language menu.
- Bilingual documentation: every in-project document ships as an
  English + Spanish (`-es`) pair.
- Layered, EDT-safe architecture: immutable domain records, structured
  statistics (no parsing of rendered text), controller/view separation.
- Full unit test suite (JUnit 5, AssertJ, Mockito) with TDD
  RED → GREEN → REFACTOR and zero-warning tolerance.
