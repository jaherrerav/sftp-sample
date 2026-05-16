# Reglas de Código y Buenas Prácticas

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

## DTOs como Records (Java 17+)

Los DTOs son inmutables. Usar `record` en lugar de clases con getters/setters.

```java
public record UserRequest(
    @NotBlank @Size(max = 100) String name,
    @Email @NotBlank String email
) {}

public record UserResponse(Long id, String name, String email, LocalDateTime createdAt) {}
```

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

## Mappers con MapStruct

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    User toEntity(UserRequest request);
}
```