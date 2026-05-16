# Reglas de Código y Buenas Prácticas

> Fuente adicional: [carm-es/guias — Buenas Prácticas de Codificación](https://github.com/carm-es/guias/blob/master/java/Buenas-Practicas-de-codificacion.md)

---

## Organización del código

### Estructura de un archivo fuente

El orden dentro de una clase debe ser:

1. Comentario Javadoc de la clase
2. Sentencia `package`
3. Sentencias `import` (sin wildcards `*`)
4. Variables `static` (pública → protegida → paquete → privada)
5. Variables de instancia (mismo orden de visibilidad)
6. Bloques estáticos (solo para inicialización de `static`)
7. Constructores
8. Métodos (agrupados por funcionalidad, no por visibilidad)

Evitar archivos de más de **2000 líneas**. Una clase pública = un archivo.

### Imports

No usar `*` para importar paquetes completos. Cada import debe ser explícito.

```java
// Correcto
import java.util.List;
import java.util.Map;

// Incorrecto
import java.util.*;
```

No dejar imports redundantes (clases del mismo paquete, `java.lang`, duplicados).

---

## Estructura del código

### Llaves — siempre obligatorias

Usar llaves incluso en bloques de una sola sentencia. Elimina bugs al añadir o eliminar líneas.

```java
// Correcto
if (condition) {
    doSomething();
}

// Incorrecto
if (condition)
    doSomething();
```

La llave de apertura va **al final de la primera línea**. Si la expresión necesita salto de línea, la llave va en línea propia.

Esta regla aplica a: `if-else`, bucles `for`/`while`/`do-while`, `try-catch-finally`, bloques `synchronized`.

### Indentación

- 4 espacios por nivel. Nunca mezclar tabs y espacios.
- Para evitar indentación profunda en métodos con muchos parámetros, usar 8 espacios en líneas de continuación.

### Longitud de línea

- Máximo **80 caracteres** por línea.
- Saltos de línea: terminar la primera línea con algo que indique continuación (operador, coma, paréntesis abierto).

```java
// Salto en aritmética: operador al inicio de la línea de continuación
longName1 = longName2 * (longName3 + longName4 - longName5)
    + 4 * longname6;

// Salto en if: 8 espacios para separar condición del cuerpo
if ((condition1 && condition2)
        || (condition3 && condition4)) {
    doSomething();
}

// Ternario largo
alpha = (aLongBooleanExpression)
    ? beta
    : gamma;
```

### Una sentencia por línea

```java
// Correcto
int i = 0;
int j = 1;

// Incorrecto
int i = 0; int j = 1;
```

---

## Inyección de Dependencias

Usar siempre inyección por constructor. Nunca `@Autowired` en campos.

```java
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

---

## DTOs como Records (Java 17+)

Los DTOs son inmutables. Usar `record` en lugar de clases con getters/setters.

```java
public record UserRequest(
    @NotBlank @Size(max = 100) String name,
    @Email @NotBlank String email
) {}

public record UserResponse(Long id, String name, String email, LocalDateTime createdAt) {}
```

---

## Entidades JPA

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

---

## Mappers con MapStruct

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    User toEntity(UserRequest request);
}
```

---

## Visibilidad (Alcance)

- El alcance debe ser **lo más pequeño posible**.
- Todos los campos de clase deben ser `private`. Exponer acceso solo con getters/setters.
- Dentro de la misma clase, acceder a los campos propios a través de `this.getX()` (no directamente), para facilitar mantenimiento.
- Usar `final` en todo lo que no deba cambiar: parámetros, variables locales, campos inmutables.

```java
// Correcto
final boolean isValid = user.isActive() && user.hasRole(ADMIN);
if (isValid) { ... }

// Incorrecto — magic condition sin nombre
if (user.isActive() && user.hasRole(ADMIN)) { ... }
```

---

## Granularidad del Método

- Máximo **35 líneas** de código real por método (sin contar líneas en blanco ni comentarios).
- Máximo **5 parámetros** por método. Si se necesitan más, agruparlos en un objeto o Record.
- Un método debe hacer **una sola cosa** — cohesión. El nombre debe reflejarlo.
- Si el nombre resulta en algo como `initPanelAndLoadData`, dividirlo en `initPanel` + `loadData`.

---

## Variables

### Declaración

- Una variable por línea.
- Inicializar en la misma declaración cuando sea posible.
- Usar una variable **para un único propósito**; nunca reutilizarla para cosas distintas.

```java
// Correcto
private int width = 150;
private int height = 50;

// Incorrecto
private int width = 150, height = 50;
```

### Asignaciones

- No asignar el mismo valor a varias variables en una sola sentencia.
- No usar asignación embebida dentro de expresiones.

```java
// Incorrecto
a = b = c = 0;
d = (a = b + c) + r;

// Correcto
a = b + c;
d = a + r;
```

---

## Constantes

No hardcodear literales numéricos directamente en el código. Definirlos como constantes con nombre.
Excepción: `-1`, `0`, `1`, `2` como contadores en bucles.

```java
// Correcto
private static final int MAX_RETRIES = 3;
if (attempts > MAX_RETRIES) { ... }

// Incorrecto
if (attempts > 3) { ... }
```

Acceder a variables y métodos estáticos siempre con el nombre de la clase, nunca a través de una instancia.

```java
MyClass.staticMethod();   // Correcto
myObject.staticMethod();  // Incorrecto
```

---

## Condicionales

Extraer condiciones complejas en variables booleanas con nombre descriptivo:

```java
// Confuso
if (element < 0 || MAX_ELEMENTS < element || element == lastElement) { ... }

// Claro
final boolean outOfRange = (element < 0 || MAX_ELEMENTS < element);
final boolean repeated   = (element == lastElement);
if (outOfRange || repeated) { ... }
```

- Usar paréntesis cuando haya duda sobre precedencia de operadores.
- Comparar booleanos implícitamente (nunca `== true` / `== false`).
- Máximo **3 branches** distintos por bloque de código.
- En cadenas `if-else`, colocar primero los casos más comunes.

```java
// Correcto
if (valid) { ... }
if (!valid) { ... }

// Incorrecto
if (valid == true) { ... }
if (valid == false) { ... }
```

---

## Bucles

- Preferir `for` cuando sea posible: reúne el control en un solo lugar.
- **Nunca modificar** la variable de control dentro del cuerpo del `for`. Si se necesita, usar `while`.
- Cuerpo del bucle: máximo **15 líneas**. Si es mayor, extraer a un método.
- Máximo **3 niveles de anidamiento**.

```java
// Incorrecto: modifica la variable de control dentro del for
for (int i = 0; i < list.size(); ++i) {
    if (list.get(i).shouldRemove()) {
        list.remove(i);
        --i; // ← prohibido
    }
}

// Correcto: usar while
int i = 0;
while (i < list.size()) {
    if (list.get(i).shouldRemove()) {
        list.remove(i);
    } else {
        ++i;
    }
}
```

---

## Switches

- Nunca omitir `break` entre cases. Si hay lógica común, extraerla a un método.
- Incluir siempre un `default` para proteger contra cambios futuros en enumeraciones.

```java
switch (status) {
    case ACTIVE:
        handleActive();
        break;
    case INACTIVE:
        handleInactive();
        break;
    default:
        throw new IllegalArgumentException("Unknown status: " + status);
}
```

---

## Constructores

Tener un único constructor "principal" con todos los parámetros. Los constructores de conveniencia deben delegar en él con `this(...)`, nunca duplicar lógica.

```java
// Constructor principal
public Transfer(String filename, Path destination, int retries) {
    this.filename    = filename;
    this.destination = destination;
    this.retries     = retries;
}

// Constructor de conveniencia — delega, no duplica
public Transfer(String filename, Path destination) {
    this(filename, destination, 3);
}
```

---

## Valores de Retorno

- Un método debe tener **un único `return`** al final cuando sea posible.
- Usar variables locales temporales para acumular el resultado y retornarlas al final.
- Simplificar expresiones booleanas directas:

```java
// Incorrecto
if (condition) {
    return true;
} else {
    return false;
}

// Correcto
return condition;
```

---

## Comentarios

Los comentarios deben explicar el **por qué**, no el qué. El código bien nombrado se explica solo.

- Evitar comentarios redundantes que repitan lo que el código ya dice.
- Un comentario desactualizado es peor que ninguno.
- Usar Javadoc en todas las clases públicas y sus métodos públicos/protegidos.
- Los comentarios de implementación (`//`) se usan para aclarar decisiones no obvias o workarounds.

```java
// Incorrecto: redundante
// Suma a más b
int result = a + b;

// Correcto: explica el porqué
// JSch requiere reconexión explícita tras timeout pasivo de 30s (ver issue #142)
session.reconnect();
```