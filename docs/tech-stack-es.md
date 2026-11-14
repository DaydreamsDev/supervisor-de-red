# Pila Tecnológica y Estándares de Ingeniería

> [English](tech-stack.md)

Aplicación de escritorio Java 21 construida con Maven, Swing, pcap4j,
JFreeChart y FlatLaf, más los estándares de ingeniería que hacen que el código
sea de nivel portafolio.

## Pila

| Ámbito | Elección | Versión |
|--------|----------|---------|
| Lenguaje | Java | 21 (LTS) |
| Construcción | Maven (wrapper incluido) | wrapper `mvnw` |
| Captura | pcap4j (`pcap4j-core`, `pcap4j-packetfactory-static`) | 1.8.2 |
| Gráficas | JFreeChart | 1.5.6 |
| Apariencia | FlatLaf | 3.8 |
| API de registro | SLF4J | 2.0.20 |
| Backend de registro | Logback (`logback-classic`) | 1.6.7 |
| JSON | Jackson (`jackson-databind`, `jackson-datatype-jsr310`) | 2.23.0 |
| Pruebas | JUnit 5 (`junit-jupiter`) | 5.14.4 |
| Aserciones | AssertJ | 3.27.7 |
| Mocks | Mockito (`mockito-core`, `mockito-junit-jupiter`) | 5.24.0 |
| Empaquetado | maven-shade-plugin (jar ejecutable) | 3.6.2 |

Herramientas de construcción fijadas en `pom.xml`: maven-compiler-plugin 3.13.0
(release 21), maven-surefire-plugin 3.6.1. Compile y pruebe con
`./mvnw clean package` y `./mvnw test` — no se requiere Maven instalado localmente.

## Convenciones de nombres

Los nombres llevan la intención; los comentarios son el último recurso.

| Regla | Ejemplo |
|-------|---------|
| Nombres autodocumentados; **casi sin comentarios** | `startCapture`, no `startCapture` + explicación |
| **Sin abreviaturas** — salvo acrónimos universalmente entendidos | `temporaryDirectory`, no `tmpDir` |
| Nombres de protocolo — canónicos, se usan tal cual | `HTTP`, `HTTPS`, `FTP`, `SMTP`, `TCP`, `UDP`, `ARP`, `ICMP` — son los nombres de los protocolos, no abreviaturas de palabras comunes |
| Acrónimos técnicos estándar — se expanden en su primera aparición en prosa | `id` (identifier), `MAC` (Media Access Control), `BPF` (Berkeley Packet Filter), `UI` (user interface), `IO` (input/output) |
| Nombres de bibliotecas y herramientas — nombres propios, se usan tal cual | `pcap` (`libpcap`, `pcap4j`, `Npcap`) |
| **Sin variables de una letra** | `interfaceIndex`, `packetCount` |
| Clases: sustantivos en `PascalCase` | `CaptureService`, `ProtocolCounter` |
| Métodos: verbos en `camelCase` | `listDevices`, `isRunning` |
| Constantes: `SCREAMING_SNAKE_CASE` | `SNAPSHOT_LENGTH`, `READ_TIMEOUT_MILLIS` |
| Predicados booleanos: `isX` / `hasX` / `canX` | `isCaptureActive`, `hasDevices` |
| Paquetes: nombres de capa en minúsculas | `com.daydreamsdev.supervisor.capture` |
| Longitud máxima de línea | 100 columnas |

Si un nombre no lleva la intención, renombre — no añada un comentario. Los únicos
comentarios permitidos en archivos fuente son las cabeceras de licencia MIT.

## Disciplina de pruebas

TDD (desarrollo guiado por pruebas) en todo comportamiento: **RED → GREEN → REFACTOR**.

1. RED — escriba primero una prueba que falle para el comportamiento.
2. GREEN — implemente lo mínimo para que pase.
3. REFACTOR — limpie mientras las pruebas siguen en verde.

Nombrado de métodos de prueba: `methodUnderTest_condition_expectedOutcome`.

```
countsTcpPackets_whenRecordsAreTcp_expectsOneTcp
```

- Dominio y servicios: pruebas unitarias reales (JUnit 5 + AssertJ).
- Controlador: probado con un `CaptureService` falso; gráficas con `CaptureStatistics` sintéticos.
- Las pruebas nunca necesitan captura nativa ni root — las llamadas nativas
  quedan detrás de interfaces.

## Higiene de código

- **Tolerancia cero a avisos.** Sin avisos, sin imports sin usar, sin tipos crudos,
  sin conversiones sin comprobar, sin código comentado, sin TODOs sueltos.
- **SLF4J, nunca `System.out`.** El registro pasa por SLF4J + Logback;
  los errores de cara al usuario pasan por diálogos.
- Nunca tragar excepciones: registre o muestre, siempre con contexto.
- Las cadenas mágicas viven en constantes o en bundles de i18n, nunca en línea.

## Siguiente paso

- [Arquitectura](architecture-es.md)
- [Contribuir](../CONTRIBUTING-es.md)
