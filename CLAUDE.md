# Guía de Desarrollo Backend - Spring Boot (Senior Standards)

Este documento define las convenciones, arquitectura y comandos que Claude debe seguir estrictamente al trabajar en este proyecto.

---

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje principal | Java 17+ |
| Lenguaje de tests | Groovy |
| Framework | Spring Boot 3.x |
| Persistencia | Spring Data JPA + Hibernate |
| Migración de BD | Flyway |
| Mapeo | MapStruct |
| Documentación API | SpringDoc OpenAPI 3 |
| Tests unitarios | Spock Framework + Groovy |
| Tests de aceptación | Cucumber + Gherkin |
| Tests de rendimiento | JMeter (.jmx) |
| Build | Gradle (Wrapper) |

---

## Arquitectura de 3 Capas

```
Client → [DTO entrada] → Controller → Service → Repository → DB
                                   ↘ Mapper ↗        ↘ Entity ↗
         [DTO salida]  ← Controller ← Service
```

### Responsabilidades por capa

**Controller** (`controller/`)
- Solo maneja HTTP: status codes, headers, routing.
- Recibe y devuelve DTOs, nunca entidades JPA.
- Valida entrada con `@Valid` / `@Validated`.
- No contiene lógica de negocio bajo ninguna circunstancia.

**Service** (`service/`)
- Toda la lógica de negocio y orquestación vive aquí.
- Define siempre una interfaz (`UserService`) y su implementación (`UserServiceImpl`).
- Gestiona transacciones con `@Transactional`. Solo anota en el impl, no en la interfaz.
- Lanza excepciones de dominio propias (ver sección Excepciones).

**Repository** (`repository/`)
- Extiende `JpaRepository` o `CrudRepository`.
- Consultas complejas con `@Query` (JPQL preferido sobre SQL nativo).
- Nunca retorna entidades mapeadas a lógica; eso es responsabilidad del Service.

---

## Comandos Frecuentes

```bash
# Build completo (compila + tests)
./gradlew build

# Ejecutar aplicación
./gradlew bootRun

# Tests unitarios e integración (Spock + Cucumber)
./gradlew test

# Solo compilar sin tests
./gradlew classes -x test

# Test específico por clase
./gradlew test --tests "com.example.sftp_sample.service.SftpServiceSpec"

# Test específico por método
./gradlew test --tests "com.example.sftp_sample.service.SftpServiceSpec.debería subir archivo*"

# Tests de rendimiento (JMeter — requiere JMeter instalado)
jmeter -n -t src/test/jmeter/sftp_upload_load_test.jmx \
  -Jhost=localhost -Jport=8080 \
  -l build/reports/jmeter/results.jtl -e -o build/reports/jmeter/html/
```

---

## Estructura de Paquetes

```text
src/
├── main/
│   ├── java/com/example/sftp_sample/
│   │   ├── SftpSampleApplication.java
│   │   ├── controller/
│   │   │   └── SftpController.java        ← Solo HTTP, delega a SftpService
│   │   ├── service/
│   │   │   ├── SftpService.java           ← Interfaz
│   │   │   └── impl/
│   │   │       └── SftpServiceImpl.java   ← Lógica de transferencia
│   │   ├── dto/
│   │   │   └── UploadResponse.java        ← Record (inmutable)
│   │   ├── exception/
│   │   │   ├── SftpTransferException.java
│   │   │   └── GlobalExceptionHandler.java ← ProblemDetail (RFC 7807)
│   │   └── config/
│   │       ├── SftpConfig.java            ← Spring Integration channels
│   │       ├── SftpProperties.java        ← @ConfigurationProperties
│   │       ├── SecurityConfig.java
│   │       └── OpenApiConfig.java
│   └── resources/
│       └── application.yaml
└── test/
    ├── groovy/com/example/sftp_sample/
    │   ├── SftpFlowSpec.groovy            ← Integración con SFTP embebido
    │   ├── controller/
    │   │   └── SftpControllerSpec.groovy  ← Unit test del controller
    │   └── service/
    │       └── SftpServiceSpec.groovy     ← Unit test del service
    ├── java/com/example/sftp_sample/
    │   └── acceptance/
    │       ├── CucumberIT.java            ← Runner Cucumber
    │       ├── CucumberSpringConfiguration.java
    │       └── steps/
    │           └── SftpUploadSteps.java
    ├── resources/
    │   └── features/
    │       └── sftp_upload.feature
    └── jmeter/
        └── sftp_upload_load_test.jmx
```

---

## Reglas de Código y Buenas Prácticas

### Inyección de Dependencias
Usar siempre inyección por constructor. Nunca `@Autowired` en campos.

```java
// Correcto
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }
}
```

### DTOs como Records (Java 17+)
Los DTOs son inmutables. Usar `record` en lugar de clases con getters/setters.

```java
public record UserRequest(
    @NotBlank @Size(max = 100) String name,
    @Email @NotBlank String email
) {}

public record UserResponse(Long id, String name, String email, LocalDateTime createdAt) {}
```

### Entidades JPA
- Usar `@Getter`, `@Setter`, `@NoArgsConstructor` de Lombok. Evitar `@Data` (rompe `equals`/`hashCode` en Hibernate).
- Auditoría automática con `@EntityListeners(AuditingEntityListener.class)`.
- Nunca exponer entidades en la capa Controller.

```java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

### Mappers con MapStruct

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    User toEntity(UserRequest request);
}
```

---

## Manejo de Excepciones

### Jerarquía de excepciones de dominio

```java
// Excepción base de negocio
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}

// 404 - Recurso no encontrado
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " with id " + id + " not found");
    }
}
```

### Handler global con ProblemDetail (RFC 7807)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource Not Found");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setProperty("errors", ex.getBindingResult().getFieldErrors()
            .stream().map(e -> e.getField() + ": " + e.getDefaultMessage()).toList());
        return problem;
    }
}
```

---

## Estándares de Testing

### 1. Tests Unitarios (Spock + Groovy)

**Ubicación:** `src/test/groovy/...`

Reglas:
- Usar bloques `given:`, `when:`, `then:` (o `expect:` para casos simples).
- Nombres descriptivos con strings en español o inglés consistente por proyecto.
- Usar `Mock()` y `Stub()` de Spock. No usar Mockito.
- Cobertura mínima: 80% en Service, 70% en Controller.
- Un `Specification` por clase productiva.

```groovy
class UserServiceSpec extends Specification {

    UserRepository userRepository = Mock()
    UserMapper userMapper = Mock()
    UserService userService = new UserServiceImpl(userRepository, userMapper)

    def "debería retornar el usuario cuando existe el ID"() {
        given:
        def user = new User(id: 1L, name: "Ana")
        def expected = new UserResponse(1L, "Ana", "ana@mail.com", LocalDateTime.now())

        when:
        userRepository.findById(1L) >> Optional.of(user)
        userMapper.toResponse(user) >> expected
        def result = userService.findById(1L)

        then:
        result == expected
    }

    def "debería lanzar ResourceNotFoundException cuando el ID no existe"() {
        given:
        userRepository.findById(99L) >> Optional.empty()

        when:
        userService.findById(99L)

        then:
        thrown(ResourceNotFoundException)
    }

    def "debería guardar múltiples usuarios correctamente"() {
        given:
        def request = new UserRequest(name, email)

        expect:
        userService.create(request) != null

        where:
        name    | email
        "Ana"   | "ana@mail.com"
        "Luis"  | "luis@mail.com"
    }
}
```

### 2. Tests de Aceptación (Cucumber + Gherkin)

**Ubicación features:** `src/test/resources/features/`
**Ubicación steps:** `src/test/java/.../acceptance/steps/`

Reglas:
- Escribir features en lenguaje de negocio (declarativo), no técnico.
- Un archivo `.feature` por funcionalidad de negocio.
- Los Step Definitions solo invocan a un `TestClient` (RestAssured o MockMvc wrapper). Sin lógica en los steps.
- Usar `@SpringBootTest(webEnvironment = RANDOM_PORT)` en el runner.

```gherkin
# features/user_management.feature
Feature: Gestión de Usuarios
  Como administrador del sistema
  Quiero poder crear y consultar usuarios
  Para gestionar el acceso a la plataforma

  Scenario: Crear un usuario con datos válidos
    Given que tengo los datos de un nuevo usuario "Carlos" con email "carlos@mail.com"
    When envío la solicitud de creación
    Then el sistema responde con código 201
    And el usuario creado tiene el nombre "Carlos"

  Scenario: Consultar un usuario que no existe
    Given que el usuario con ID 999 no existe en el sistema
    When consulto el usuario con ID 999
    Then el sistema responde con código 404
    And el mensaje de error indica "Resource Not Found"
```

```java
// CucumberIntegrationIT.java
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.company.project.acceptance")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberIntegrationIT {}
```

### 3. Tests de Rendimiento (JMeter)

**Ubicación:** `src/test/jmeter/`

Reglas:
- Versionar todos los archivos `.jmx` en Git.
- Parametrizar `host`, `port` y `basePath` como propiedades JMeter (no hardcodear).
- Incluir assertions de tiempo de respuesta (p95 < 500ms como baseline).
- Nombrar el plan de prueba con el endpoint objetivo: `user_create_load_test.jmx`.
- Documentar el escenario de carga en un comentario dentro del `.jmx`.

Ejecución desde Maven con el plugin JMeter:
```bash
./mvnw verify -Pjmeter -Djmeter.host=localhost -Djmeter.port=8080
```

---

## Estándares de Logging

Usar SLF4J + Logback. Nunca `System.out.println`.

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

Niveles de uso:
- `DEBUG`: flujo interno, valores de variables durante desarrollo.
- `INFO`: operaciones de negocio exitosas (creación, actualización).
- `WARN`: situaciones recuperables (recurso no encontrado, reintentos).
- `ERROR`: excepciones inesperadas que requieren atención.

---

## Documentación de API (OpenAPI 3)

Anotar todos los endpoints con SpringDoc:

```java
@Tag(name = "Users", description = "Gestión de usuarios del sistema")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Operation(summary = "Obtener usuario por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) { ... }
}
```

La UI estará disponible en `/swagger-ui.html` cuando la app esté corriendo.

---

## Migraciones de Base de Datos (Flyway)

- Nombrar scripts: `V{versión}__{descripción_snake_case}.sql` — ejemplo: `V2__add_email_index_to_users.sql`.
- Nunca modificar un script ya aplicado en producción.
- Un script por cambio de esquema lógico.
- Incluir `-- Flyway migration` como primer comentario en cada script.

```sql
-- Flyway migration
CREATE TABLE users (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
```

---

## Convenciones de Git

- **Branches:** `feature/`, `fix/`, `refactor/`, `chore/` + descripción en kebab-case.
  - Ejemplo: `feature/user-password-reset`
- **Commits:** Conventional Commits en inglés.
  - `feat(user): add password reset endpoint`
  - `fix(auth): handle expired token correctly`
  - `test(user): add spock spec for UserService`
- **PR:** Un PR = una funcionalidad o fix. Sin commits de merge, usar rebase.

---

## Seguridad

- Nunca loguear contraseñas, tokens ni PII.
- Sanitizar inputs en el Controller con `@Valid` antes de que lleguen al Service.
- Usar `@PreAuthorize` para control de acceso a nivel de método en el Service.
- Las credenciales van en variables de entorno, nunca en `application.yaml` commiteado.
- Configurar CORS explícitamente en `SecurityConfig`. No usar `@CrossOrigin` en controllers.

---

## Checklist antes de entregar código

- [ ] Tests Spock pasan (`./mvnw test`).
- [ ] Checkstyle sin violaciones (`./mvnw checkstyle:check`).
- [ ] No hay lógica de negocio en Controller ni en Repository.
- [ ] Excepciones de dominio usan `ProblemDetail`.
- [ ] Nuevos endpoints documentados con OpenAPI.
- [ ] Migración Flyway creada si hubo cambio de esquema.
- [ ] Sin `System.out.println` ni credenciales hardcodeadas.
