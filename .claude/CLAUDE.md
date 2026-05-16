# Guía de Desarrollo Backend — Spring Boot

Microservicio de integración SFTP. Claude debe seguir estrictamente las reglas importadas a continuación.

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje principal | Java 21 |
| Lenguaje de tests | Groovy |
| Framework | Spring Boot 4.x |
| Build | Gradle (Wrapper) |
| Mapeo | MapStruct |
| Documentación API | SpringDoc OpenAPI 3 |
| Tests unitarios | Spock Framework + Groovy |
| Tests de aceptación | Cucumber + Gherkin |
| Tests de rendimiento | JMeter (.jmx) |

## Checklist antes de entregar código

- [ ] Tests Spock pasan (`./gradlew test`).
- [ ] No hay lógica de negocio en Controller ni en Repository.
- [ ] Excepciones de dominio usan `ProblemDetail` (RFC 7807).
- [ ] Nuevos endpoints documentados con OpenAPI.
- [ ] Migración Flyway creada si hubo cambio de esquema.
- [ ] Sin `System.out.println` ni credenciales hardcodeadas.

---

@rules/commands.md

@rules/architecture.md

@rules/code-style.md

@rules/exceptions.md

@rules/testing.md

@rules/logging.md

@rules/api-docs.md

@rules/database.md

@rules/security.md

@rules/git.md