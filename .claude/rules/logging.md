# Estándares de Logging

Usar SLF4J + Logback. **Nunca `System.out.println`.**

```java
@Slf4j  // Lombok
@Service
public class UserServiceImpl implements UserService {

    public UserResponse findById(Long id) {
        log.debug("Buscando usuario con id={}", id);
        var user = userRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Usuario no encontrado id={}", id);
                return new ResourceNotFoundException("User", id);
            });
        log.info("Usuario encontrado id={} name={}", user.getId(), user.getName());
        return userMapper.toResponse(user);
    }
}
```

## Niveles de uso

| Nivel | Cuándo usarlo |
|---|---|
| `DEBUG` | Flujo interno, valores de variables durante desarrollo |
| `INFO` | Operaciones de negocio exitosas (creación, actualización) |
| `WARN` | Situaciones recuperables (recurso no encontrado, reintentos) |
| `ERROR` | Excepciones inesperadas que requieren atención inmediata |

**Reglas adicionales:**
- Nunca loguear contraseñas, tokens ni datos personales (PII).
- Usar parámetros estructurados `log.info("key={}", value)`, no concatenación de strings.