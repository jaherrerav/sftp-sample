# Documentación de API (OpenAPI 3 / SpringDoc)

Anotar todos los endpoints con SpringDoc. La UI estará disponible en `/swagger-ui.html`.

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

Configurar metadata global en `OpenApiConfig`:

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API Title")
                .description("Descripción del servicio")
                .version("1.0.0"));
    }
}
```