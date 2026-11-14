# Contributing

> [Español](CONTRIBUTING-es.md)

Contributions follow one rule: **tests first, evidence last**. Every change
ships with tests that fail first and pass after, keeps zero warnings, and keeps
every document bilingual.

## Quick path

1. Build and test the project (see below).
2. Pick or open an issue.
3. Write the failing test (RED), implement (GREEN), clean up (REFACTOR).
4. Open a pull request with a conventional-commit title.

## Build and test

```bash
./mvnw test            # run the full test suite
./mvnw clean package   # build the shaded runnable jar
```

No Maven install is required — the wrapper downloads what it needs. Tests run
without native capture or root privileges.

## Commit conventions

Conventional commits: `<type>(<scope>): <description>`

| Type | Use for |
|------|---------|
| `feat` | new user-visible behavior |
| `fix` | bug fix |
| `docs` | documentation only |
| `test` | tests only |
| `refactor` | code change without behavior change |
| `chore` | build, tooling, maintenance |

Examples: `feat(filter): add HTTPS preset`, `fix(capture): stop capture on
device removal`, `docs: add troubleshooting to user guide`.

## Pull request flow

1. Fork and branch from the default branch.
2. Keep the change focused on one behavior or document.
3. Add tests for every rule the change introduces; keep existing tests green.
4. Update help text, bundles, and docs when a command, option, or message changes.
5. Verify zero warnings before opening the PR.
6. Describe what changed and why; reviewers verify intent, not detective work.

## Code standards

- **Naming**: self-documenting names, almost no comments, no abbreviations
  (except `id`, `http`, `tcp`, `udp`, `arp`, `icmp`, `bpf`), no single-letter
  variables; `PascalCase` classes, `camelCase` methods,
  `SCREAMING_SNAKE_CASE` constants, `isX`/`hasX`/`canX` predicates.
- **Tests**: `methodUnderTest_condition_expectedOutcome`; TDD (test-driven development) RED → GREEN → REFACTOR.
- **Hygiene**: max 100 columns, zero warnings, SLF4J — never `System.out`.

Full details in [Tech stack and standards](docs/tech-stack.md).

## Bilingual documentation rule

Every in-project document ships as an English + Spanish pair: `X.md` and
`X-es.md` (README, `docs/*`, CONTRIBUTING, CHANGELOG, LICENSE). Code and
identifiers are English only; Spanish is neutral and professional. A change to
one language must update the other in the same commit.

## Next step

- [Architecture](docs/architecture.md)
- [User guide](docs/user-guide.md)
