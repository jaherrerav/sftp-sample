# Seguridad

- **Nunca loguear** contraseñas, tokens ni PII.
- Sanitizar inputs en el Controller con `@Valid` antes de que lleguen al Service.
- Usar `@PreAuthorize` para control de acceso a nivel de método en el Service.
- Las credenciales van en **variables de entorno**, nunca en `application.yaml` commiteado.
- Configurar CORS explícitamente en `SecurityConfig`. No usar `@CrossOrigin` en controllers.
- Versionar las rutas de la API: `/api/v1/...`. Exponer Swagger bajo `/swagger-ui/**` y `/v3/api-docs/**`.