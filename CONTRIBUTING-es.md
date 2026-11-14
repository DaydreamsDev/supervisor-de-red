# Contribuir

> [English](CONTRIBUTING.md)

Las contribuciones siguen una regla: **primero las pruebas, al final la
evidencia**. Cada cambio incluye pruebas que fallan primero y pasan después,
mantiene cero avisos y mantiene cada documento bilingüe.

## Camino rápido

1. Compile y pruebe el proyecto (véase abajo).
2. Elija o abra un issue.
3. Escriba la prueba que falla (RED), implemente (GREEN), limpie (REFACTOR).
4. Abra una pull request con un título de commit convencional.

## Compilar y probar

```bash
./mvnw test            # ejecutar la suite completa
./mvnw clean package   # construir el jar ejecutable con sombreado
```

No se requiere Maven instalado — el wrapper descarga lo necesario. Las pruebas
se ejecutan sin captura nativa ni privilegios de root.

## Convenciones de commits

Commits convencionales: `<tipo>(<ámbito>): <descripción>`

| Tipo | Uso |
|------|-----|
| `feat` | comportamiento nuevo visible para el usuario |
| `fix` | corrección de error |
| `docs` | solo documentación |
| `test` | solo pruebas |
| `refactor` | cambio de código sin cambio de comportamiento |
| `chore` | construcción, herramientas, mantenimiento |

Ejemplos: `feat(filter): add HTTPS preset`, `fix(capture): stop capture on
device removal`, `docs: add troubleshooting to user guide`.

## Flujo de pull requests

1. Haga fork y rama desde la rama por defecto.
2. Mantenga el cambio enfocado en un comportamiento o documento.
3. Añada pruebas para cada regla que introduzca el cambio; mantenga verdes las existentes.
4. Actualice textos de ayuda, bundles y documentos cuando cambie un comando,
   una opción o un mensaje.
5. Verifique cero avisos antes de abrir la PR.
6. Describa qué cambió y por qué; los revisores verifican la intención, no hacen
   trabajo de detective.

## Estándares de código

- **Nombres**: nombres autodocumentados, casi sin comentarios, sin abreviaturas
  (salvo `id`, `http`, `tcp`, `udp`, `arp`, `icmp`, `bpf`), sin variables de una
  letra; clases `PascalCase`, métodos `camelCase`, constantes
  `SCREAMING_SNAKE_CASE`, predicados `isX`/`hasX`/`canX`.
- **Pruebas**: `methodUnderTest_condition_expectedOutcome`; TDD (desarrollo guiado por pruebas) RED → GREEN → REFACTOR.
- **Higiene**: máximo 100 columnas, cero avisos, SLF4J — nunca `System.out`.

Detalles completos en [Pila tecnológica y estándares](docs/tech-stack-es.md).

## Regla de documentación bilingüe

Cada documento del proyecto se publica como un par en inglés + español: `X.md`
y `X-es.md` (README, `docs/*`, CONTRIBUTING, CHANGELOG, LICENSE). El código y
los identificadores van solo en inglés; el español es neutro y profesional. Un
cambio en un idioma debe actualizar el otro en el mismo commit.

## Siguiente paso

- [Arquitectura](docs/architecture-es.md)
- [Guía de usuario](docs/user-guide-es.md)
