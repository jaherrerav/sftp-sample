# Arquitectura de 3 Capas

```
Client → [DTO entrada] → Controller → Service → Repository → DB
                                   ↘ Mapper ↗        ↘ Entity ↗
         [DTO salida]  ← Controller ← Service
```

## Responsabilidades por capa

**Controller** (`controller/`)
- Solo maneja HTTP: status codes, headers, routing.
- Recibe y devuelve DTOs, nunca entidades JPA.
- Valida entrada con `@Valid` / `@Validated`.
- No contiene lógica de negocio bajo ninguna circunstancia.

**Service** (`service/`)
- Toda la lógica de negocio y orquestación vive aquí.
- Define siempre una interfaz (`UserService`) y su implementación (`UserServiceImpl`).
- Gestiona transacciones con `@Transactional`. Solo anota en el impl, no en la interfaz.
- Lanza excepciones de dominio propias.

**Repository** (`repository/`)
- Extiende `JpaRepository` o `CrudRepository`.
- Consultas complejas con `@Query` (JPQL preferido sobre SQL nativo).
- Nunca retorna entidades mapeadas a lógica; eso es responsabilidad del Service.

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
    │   ├── SftpFlowSpec.groovy
    │   ├── controller/SftpControllerSpec.groovy
    │   └── service/SftpServiceSpec.groovy
    ├── java/com/example/sftp_sample/acceptance/
    │   ├── CucumberIT.java
    │   ├── CucumberSpringConfiguration.java
    │   └── steps/SftpUploadSteps.java
    ├── resources/features/sftp_upload.feature
    └── jmeter/sftp_upload_load_test.jmx
```