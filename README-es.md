# Supervisor de Red

[![CI](https://github.com/DaydreamsDev/supervisor-de-red/actions/workflows/ci.yml/badge.svg)](https://github.com/DaydreamsDev/supervisor-de-red/actions/workflows/ci.yml)
[![Licencia: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> [English](README.md) | [Español](README-es.md)

Visualizador de tráfico de red de escritorio para Linux, macOS y Windows.
Capture paquetes desde cualquier interfaz de red, fíltrelos con expresiones BPF (Berkeley Packet Filter)
predefinidas o personalizadas, y observe el tráfico en vivo como texto crudo de
paquetes, una gráfica de paquetes contra tiempo y una gráfica circular de
distribución de protocolos.

Desarrollado originalmente en 2015 como proyecto universitario, actualizado y entregado en 2026.

![Supervisor de Red — gráficas de tráfico en vivo](docs/screenshot-graphs-es.png)

## Funcionalidades

- Listar interfaces de red con descripción, tipo de enlace, MAC (Media Access Control), IP (Internet Protocol), máscara y broadcast
- Captura con inicio/detención y vista desplazable de paquetes en crudo
- Filtros predefinidos: HTTP, HTTPS, FTP, SMTP, ICMP, ARP — más un filtro BPF libre
- Gráficas en vivo: paquetes contra tiempo y distribución de protocolos (TCP / UDP / ARP / ICMP)
- Guardar y cargar sesiones de captura
- Interfaz moderna FlatLaf (claro y oscuro), en español por defecto y con inglés disponible

## Requisitos

- Java 21 o superior
- Un backend de captura:
  - Linux: `libpcap` (normalmente preinstalada) y permisos de root o `CAP_NET_RAW`
  - macOS: permisos de root
  - Windows: [Npcap](https://npcap.com/)

## Ejecutar

Descargue el jar precompilado desde la [última release](https://github.com/DaydreamsDev/supervisor-de-red/releases/latest)
y ejecútelo con permisos de captura:

```bash
sudo -E java -jar supervisor-de-red-0.1.1.jar
```

En Linux, `sudo -E` conserva el entorno de su sesión de escritorio al elevar
privilegios, de modo que la interfaz se renderiza correctamente. Alternativas:
conceder `CAP_NET_RAW` al binario de Java y ejecutar como su usuario, o ejecutar
como root. Consulte la [solución de problemas de la guía de usuario](docs/user-guide-es.md#solución-de-problemas)
para más detalles.

### Compilar desde el código fuente

```bash
./mvnw clean package
java -jar target/supervisor-de-red-0.1.1.jar
```

El jar ejecutable se genera en `target/supervisor-de-red-0.1.1.jar`.

## Uso

1. Inicie la aplicación con permisos de captura (véase [Ejecutar](#ejecutar)).
2. Elija una interfaz de red en el panel Interfaz y pulse **Actualizar** si la lista está vacía.
3. Inicie el video o servicio que desea observar, pulse **CAPTURA** y observe el área de salida y las gráficas.

Recorrido completo: [guía de usuario](docs/user-guide-es.md).

## Probar

```bash
./mvnw test
```

La suite se ejecuta sin captura nativa ni privilegios de root.

## Contribuir

Las contribuciones son bienvenidas. Lea primero [CONTRIBUTING-es.md](CONTRIBUTING-es.md):

- Trabaje en una rama `tipo/descripcion` creada desde `develop` (por ejemplo `fix/filter-recovery`).
- Abra un pull request contra `develop` con un título de commit convencional y una lista corta de cambios clave.
- Todas las pruebas deben pasar (`./mvnw verify`).

## Documentación

- [Guía de usuario](docs/user-guide-es.md) — primera captura, filtros, gráficas,
  solución de problemas
- [Arquitectura](docs/architecture-es.md) — capas, seguridad del EDT (Event Dispatch Thread, hilo de despacho de eventos), mapa de paquetes
- [Pila tecnológica y estándares](docs/tech-stack-es.md) — versiones, convenciones de nombres, TDD (desarrollo guiado por pruebas)
- [Contribuir](CONTRIBUTING-es.md)
- [Registro de cambios](CHANGELOG-es.md)

## Licencia

[MIT](LICENSE) — consulte [LICENSE-es.md](LICENSE-es.md) para el texto en español.
