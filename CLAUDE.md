# Guía de Desarrollo

Las reglas de desarrollo están divididas por dominio en `.claude/rules/`:

| Archivo | Contenido |
|---|---|
| [`architecture.md`](.claude/rules/architecture.md) | Arquitectura 3 capas y estructura de paquetes |
| [`code-style.md`](.claude/rules/code-style.md) | Inyección, DTOs, entidades, MapStruct |
| [`testing.md`](.claude/rules/testing.md) | Spock, Cucumber, JMeter |
| [`exceptions.md`](.claude/rules/exceptions.md) | Jerarquía de excepciones y ProblemDetail |
| [`logging.md`](.claude/rules/logging.md) | SLF4J, niveles y reglas |
| [`api-docs.md`](.claude/rules/api-docs.md) | OpenAPI / SpringDoc |
| [`database.md`](.claude/rules/database.md) | Migraciones Flyway |
| [`security.md`](.claude/rules/security.md) | Reglas de seguridad |
| [`git.md`](.claude/rules/git.md) | Conventional Commits, branches, PRs |
| [`commands.md`](.claude/rules/commands.md) | Comandos Gradle frecuentes |

Claude Code carga las reglas automáticamente desde [`.claude/CLAUDE.md`](.claude/CLAUDE.md).