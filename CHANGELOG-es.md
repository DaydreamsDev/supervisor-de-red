# Registro de Cambios

Todos los cambios notables de este proyecto se documentan en este archivo.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
y este proyecto sigue [Versionado Semántico](https://semver.org/spec/v2.0.0.html).

## [0.1.1] - 2026-11-14

### Corregido

- Se estabilizó la prueba asíncrona del bucle de captura para eliminar un fallo
  que solo ocurría en CI.

### Cambiado

- README: se añadieron secciones de descarga de la release, uso y contribución.

## [0.1.0] - 2026-11-14

### Añadido

- Visualizador de tráfico de red publicado bajo la licencia MIT.
- Captura de paquetes con pcap4j: listado de dispositivos (descripción, tipo
  de enlace, MAC, IP, máscara, broadcast) y ciclo de vida de inicio/detención
  elegante.
- Filtros BPF predefinidos — HTTP (80), HTTPS (443), FTP (21), SMTP (25),
  ICMP, ARP — más un filtro especial BPF libre validado, con estado
  activado/desactivado.
- Gráficas en vivo: paquetes contra tiempo y gráfica circular de distribución
  de protocolos (TCP / UDP / ARP / ICMP).
- Guardar y cargar sesiones de captura como JSON mediante selector de archivos.
- Apariencia FlatLaf con temas claro (Claro) y oscuro (Oscuro).
- UI bilingüe — español por defecto, inglés disponible desde el menú de idioma.
- Documentación bilingüe: cada documento del proyecto se publica como un par
  inglés + español (`-es`).
- Arquitectura por capas segura para el EDT: registros de dominio inmutables,
  estadísticas estructuradas (sin analizar texto renderizado), separación
  controlador/vista.
- Suite completa de pruebas unitarias (JUnit 5, AssertJ, Mockito) con TDD
  RED → GREEN → REFACTOR y tolerancia cero a avisos.
