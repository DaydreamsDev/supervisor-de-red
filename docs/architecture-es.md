# Arquitectura

> [English](architecture.md)

Supervisor de Red es una aplicación de escritorio por capas con un **núcleo sin UI,
completamente testeable** y un **único controlador seguro para el EDT (Event Dispatch Thread, hilo de
despacho de eventos) como único pegamento entre Swing y los servicios. Los paquetes fluyen desde un hilo de captura
hacia registros inmutables; la UI lee instantáneas, nunca estado mutable compartido.
Cada capa se prueba de forma independiente.

## Las capas

```
Capa de UI — EDT (Event Dispatch Thread, hilo de despacho de eventos) solamente
  MainFrame (vista) · paneles ui.chart · ui.Messages (bundles de i18n)
  SupervisorController (mediador)

  eventos de acción ▲                      │ Swing Timer / invokeLater
                    │                      ▼

Servicios — sin Swing, testeables con pruebas unitarias
  capture     CaptureService → Pcap4jCaptureService
  filter      FilterPreset, FilterValidator
  statistics  ProtocolCounter → instantáneas CaptureStatistics
  io          SessionStore → JsonSessionStore

  PacketRecords inmutables ▲               │ callbacks del listener
                           │               ▼

Hilo de trabajo de captura — bucle pcap4j, nunca toca Swing
  domain — ProtocolType, PacketRecord, CaptureStatistics (registros inmutables)
```

## Mapa de paquetes

| Paquete | Responsabilidad | Tipos clave |
|---------|-----------------|-------------|
| `domain` | Datos centrales inmutables compartidos por todas las capas | `ProtocolType`, `PacketRecord`, `CaptureStatistics` |
| `statistics` | Conteo de protocolos dirigido por eventos | `ProtocolCounter` |
| `filter` | Filtros predefinidos y validación de BPF (Berkeley Packet Filter) | `FilterPreset`, `FilterValidator`, `FilterValidationResult` |
| `capture` | Listado de dispositivos y ciclo de vida de la captura | `CaptureService`, `Pcap4jCaptureService`, `DeviceInformation`, `PacketListener`, `PacketRecordMapper` |
| `io` | Guardar y cargar sesiones de captura | `SessionStore`, `JsonSessionStore` |
| `ui` | Vista Swing, controlador, i18n | `MainFrame`, `SupervisorController`, `Messages` |
| `ui.chart` | Gráficas en vivo renderizadas desde `CaptureStatistics` | `TimeSeriesChartPanel`, `ProtocolPieChartPanel` |

Cada paquete depende solo de `domain` (y de su propia capa); ningún paquete
conoce Swing salvo `ui` y `ui.chart`.

## Seguridad del EDT (Event Dispatch Thread, hilo de despacho de eventos)

El hilo de trabajo de captura ejecuta el bucle de pcap4j y es el único lugar
que toca la captura nativa. Nunca toca Swing:

```
hilo de captura (fuera del EDT — Event Dispatch Thread, hilo de despacho de eventos)
   │ PacketRecord inmutable
   ├──► ProtocolCounter (sincronizado) ──► instantánea CaptureStatistics
   └──► deque concurrente acotado (búfer de sesión, descarta lo más antiguo en 10 000)
             │ refrescado cada 1000 ms por un javax.swing.Timer
             ▼
   Swing Timer se ejecuta en el EDT (Event Dispatch Thread, hilo de despacho de eventos) ──► área de salida, barra de estado, gráficas
```

- Un único hilo de captura (`ExecutorService`); la detención usa `breakLoop()`
  con un tiempo de espera acotado — sin hilos hechos a mano, sin espera activa.
- Un **Swing Timer de 1000 ms** refresca la pantalla desde el deque acotado:
  muestra los últimos 500 registros en el área de salida, actualiza el contador
  de paquetes en la barra de estado y refresca las gráficas desde una
  instantánea `CaptureStatistics` recién tomada. El deque conserva los paquetes, de
  modo que **Guardar sesión** persiste toda la captura almacenada en lugar de
  una cola ya consumida.
- El deque está acotado para que una UI lenta nunca agote la memoria.

## Separación controlador / vista

| Parte | Hace | Nunca hace |
|-------|------|------------|
| `MainFrame` | Renderiza controles, paneles, diálogos; emite eventos | Lógica de negocio, captura, estadísticas |
| `SupervisorController` | Escucha eventos de la vista, llama a los servicios, devuelve resultados | Maquetación, estilos |
| Servicios | Capturan, filtran, cuentan, persisten | Swing |

El controlador se prueba con un `CaptureService` falso y `CaptureStatistics`
sintéticos; la vista se prueba con esos mismos dobles.

## Estadísticas desde registros estructurados, nunca desde texto

Las estadísticas por protocolo se calculan a partir de `PacketRecord`s
estructurados producidos en el momento de la captura. `ProtocolCounter` los
acumula en instantáneas de `CaptureStatistics` y las gráficas renderizan esas
instantáneas.

El texto renderizado es solo presentación: el área de salida puede cambiar de
formato sin afectar las estadísticas, y ningún camino del código convierte texto
renderizado de vuelta en datos.

La reconstrucción calcula las estadísticas desde `PacketRecord`s estructurados
producidos en el momento de la captura. `ProtocolCounter` los acumula en
instantáneas `CaptureStatistics`. El texto renderizado es solo presentación y puede
cambiar sin romper las estadísticas.

## Registros de dominio inmutables

`PacketRecord` y `CaptureStatistics` son registros de Java con constructores validados:

- Seguros para compartir entre el hilo de captura y el EDT (Event Dispatch Thread, hilo de despacho de eventos) — sin bloqueos para lecturas.
- Las instantáneas son valores puntuales; la UI siempre renderiza una imagen consistente.
- `PacketRecord` contiene `capturedAt`, `protocol`, `sizeInBytes` y `summaryLine`.

## Decisiones de un vistazo

| Decisión | Por qué |
|----------|---------|
| Paquetes por capas, `domain` abajo del todo | Núcleo testeable, una sola dirección de dependencias |
| Un controlador como único pegamento | Sin lógica en las vistas, sin Swing en los servicios |
| Registros para datos capturados y estadísticas | La inmutabilidad simplifica el uso entre hilos |
| Swing Timer + deque acotado | Actualizaciones de UI en el EDT (Event Dispatch Thread, hilo de despacho de eventos) sin memoria ilimitada |
| SLF4J para registro, diálogos para errores accionables | Nunca tragar errores, nunca `System.out` |

## Siguiente paso

- [Pila tecnológica y estándares](tech-stack-es.md)
- [Guía de usuario](user-guide-es.md)
