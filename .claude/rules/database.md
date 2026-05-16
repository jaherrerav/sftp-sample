# Migraciones de Base de Datos (Flyway)

- Nombrar scripts: `V{versión}__{descripción_snake_case}.sql`
  - Ejemplo: `V2__add_email_index_to_users.sql`
- **Nunca modificar un script ya aplicado en producción.**
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