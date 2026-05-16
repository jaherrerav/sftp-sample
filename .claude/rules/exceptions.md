# Manejo de Excepciones

## Jerarquía de excepciones de dominio

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

## Handler global con ProblemDetail (RFC 7807)

Toda excepción no controlada debe pasar por `GlobalExceptionHandler`. Nunca lanzar `ResponseEntity` con errores desde el Controller directamente.

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