# Convenciones de Git

## Branches

Prefijo + descripción en kebab-case:

| Prefijo | Uso |
|---|---|
| `feature/` | Nueva funcionalidad |
| `fix/` | Corrección de bug |
| `refactor/` | Refactorización sin cambio de comportamiento |
| `chore/` | Mantenimiento, dependencias, config |

Ejemplo: `feature/user-password-reset`

## Commits (Conventional Commits en inglés)

```
feat(user): add password reset endpoint
fix(auth): handle expired token correctly
refactor(service): extract file sanitization to helper
test(user): add spock spec for UserService
chore(deps): upgrade spring-boot to 4.0.6
```

## Pull Requests

- Un PR = una funcionalidad o fix.
- Sin commits de merge — usar **rebase**.
- Título en inglés, ≤ 70 caracteres.