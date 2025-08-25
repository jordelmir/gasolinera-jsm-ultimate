# Gasolinera JSM PostgreSQL MCP Server

Servidor MCP personalizado para acceder a la base de datos PostgreSQL de la plataforma de gamificación Gasolinera JSM.

## Características

- **Consultas SQL**: Ejecuta consultas SELECT de forma segura
- **Comandos de base de datos**: Ejecuta INSERT, UPDATE, DELETE
- **Exploración de esquema**: Lista tablas y describe su estructura
- **Métricas de negocio**: Obtiene KPIs específicos de la plataforma de gamificación

## Herramientas Disponibles

### `query_database`

Ejecuta consultas SELECT en la base de datos.

**Parámetros:**

- `query` (string): Consulta SQL SELECT
- `params` (array, opcional): Parámetros para la consulta

**Ejemplo:**

```sql
SELECT * FROM users WHERE active = true LIMIT 10
```

### `execute_command`

Ejecuta comandos INSERT, UPDATE, DELETE.

**Parámetros:**

- `query` (string): Comando SQL
- `params` (array, opcional): Parámetros para el comando

### `list_tables`

Lista todas las tablas en la base de datos.

### `describe_table`

Obtiene información del esquema de una tabla específica.

**Parámetros:**

- `table_name` (string): Nombre de la tabla

### `get_business_metrics`

Obtiene métricas clave del negocio para la plataforma de gamificación.

**Parámetros:**

- `date_from` (string, opcional): Fecha de inicio (YYYY-MM-DD)
- `date_to` (string, opcional): Fecha de fin (YYYY-MM-DD)

**Métricas incluidas:**

- Total de usuarios
- Estaciones activas
- Cupones generados
- Canjes realizados
- Participantes en rifas semanales
- Participantes en rifas anuales

## Configuración

El servidor se conecta a PostgreSQL usando las siguientes variables de entorno:

- `DB_HOST`: Host de la base de datos (default: localhost)
- `DB_PORT`: Puerto de la base de datos (default: 5432)
- `DB_NAME`: Nombre de la base de datos (default: puntog)
- `DB_USER`: Usuario de la base de datos (default: puntog)
- `DB_PASSWORD`: Contraseña de la base de datos (default: changeme)

## Instalación

1. Ejecuta el script de setup:

```bash
cd ops/mcp-servers
./setup.sh
```

2. El servidor se configurará automáticamente en tu archivo `.kiro/settings/mcp.json`

## Uso

Una vez configurado, puedes usar las herramientas desde Kiro:

```
Listar todas las tablas en la base de datos
Obtener métricas de negocio del último mes
Consultar usuarios activos
```

## Seguridad

- El servidor usa conexiones asyncpg con pool de conexiones
- Los parámetros de consulta se pasan de forma segura para prevenir inyección SQL
- Solo se permiten operaciones en el esquema 'public'

## Troubleshooting

Si el servidor no se conecta:

1. Verifica que PostgreSQL esté ejecutándose
2. Confirma las credenciales de la base de datos
3. Asegúrate de que el usuario tenga permisos adecuados
4. Revisa los logs de MCP en Kiro
