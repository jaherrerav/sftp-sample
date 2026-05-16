# Estándares de Testing

## 1. Tests Unitarios (Spock + Groovy)

**Ubicación:** `src/test/groovy/...`

- Usar bloques `given:`, `when:`, `then:` (o `expect:` para casos simples).
- Nombres descriptivos en español como strings: `def "debería retornar 404 cuando el usuario no existe"()`.
- Usar `Mock()` y `Stub()` de Spock. **No usar Mockito.**
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

## 2. Tests de Aceptación (Cucumber + Gherkin)

**Ubicación features:** `src/test/resources/features/`
**Ubicación steps:** `src/test/java/.../acceptance/steps/`

- Escribir features en lenguaje de negocio (declarativo), no técnico.
- Un archivo `.feature` por funcionalidad de negocio.
- Los Step Definitions solo invocan a un `TestClient` (RestAssured o MockMvc wrapper). Sin lógica en los steps.
- Usar `@SpringBootTest(webEnvironment = RANDOM_PORT)` en el runner.

```gherkin
Feature: Gestión de Usuarios
  Como administrador del sistema
  Quiero poder crear y consultar usuarios
  Para gestionar el acceso a la plataforma

  Scenario: Crear un usuario con datos válidos
    Given que tengo los datos de un nuevo usuario "Carlos" con email "carlos@mail.com"
    When envío la solicitud de creación
    Then el sistema responde con código 201
    And el usuario creado tiene el nombre "Carlos"
```

```java
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.example.project.acceptance")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberIT {}
```

## 3. Tests de Rendimiento (JMeter)

**Ubicación:** `src/test/jmeter/`

- Versionar todos los archivos `.jmx` en Git.
- Parametrizar `host`, `port` y `basePath` como propiedades JMeter (no hardcodear).
- Incluir assertions de tiempo de respuesta (p95 < 500ms como baseline).
- Nombrar el plan con el endpoint objetivo: `sftp_upload_load_test.jmx`.
- Documentar el escenario de carga en un comentario XML dentro del `.jmx`.

```bash
jmeter -n -t src/test/jmeter/sftp_upload_load_test.jmx \
  -Jhost=localhost -Jport=8080 \
  -l build/reports/jmeter/results.jtl -e -o build/reports/jmeter/html/
```