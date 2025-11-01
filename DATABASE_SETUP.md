# Database Configuration

## PostgreSQL Setup

All microservices are configured to use PostgreSQL with the following credentials:

- **Host**: localhost
- **Port**: 5432
- **Database**: postgres
- **Username**: ujjawalkumar
- **Password**: ujju

## Services and Their Tables

Each microservice will create the following tables in the `postgres` database:

### User Service
- `users` - User accounts and authentication data

### Product Service
- `products` - Product catalog information

### Order Service
- `orders` - Order records
- `order_items` - Individual items in each order

### Payment Service
- `payments` - Payment transactions

### Inventory Service
- `inventory` - Stock and inventory management

## Database Configuration

All services use:
- **Hibernate ddl-auto**: `update` (tables are created/updated automatically)
- **Show SQL**: Enabled (see SQL queries in logs)
- **Dialect**: PostgreSQL

## Prerequisites

1. **PostgreSQL must be installed and running**
   ```bash
   # Check if PostgreSQL is running
   psql -U ujjawalkumar -d postgres
   
   # Or check service status
   # macOS: brew services list | grep postgresql
   ```

2. **Database connection test**
   ```bash
   psql -h localhost -p 5432 -U ujjawalkumar -d postgres
   # Enter password: ujju
   ```

3. **Verify database exists**
   ```sql
   \l  -- List all databases
   \c postgres  -- Connect to postgres database
   \dt  -- List all tables (after services run)
   ```

## Starting Services

After PostgreSQL is running, start your services:

```bash
# 1. Start Eureka
cd microservices/service-registry
mvn spring-boot:run

# 2. Start other services
# They will automatically connect to PostgreSQL
# Tables will be created on first run
```

## Troubleshooting

### Connection Refused
```
Error: Connection to localhost:5432 refused
```
**Solution**: Make sure PostgreSQL is running
```bash
# macOS
brew services start postgresql

# Linux
sudo systemctl start postgresql
```

### Authentication Failed
```
Error: password authentication failed
```
**Solution**: Verify credentials in application.yml match your PostgreSQL setup

### Table Already Exists
```
Error: relation "users" already exists
```
**Solution**: This is normal if you've run services before. Hibernate will update existing tables.

### Permission Denied
```
Error: permission denied for schema public
```
**Solution**: Grant necessary permissions
```sql
GRANT ALL PRIVILEGES ON DATABASE postgres TO ujjawalkumar;
GRANT ALL ON SCHEMA public TO ujjawalkumar;
```

## Migration from H2 to PostgreSQL

If you previously ran services with H2:
1. All data was in-memory and will be lost
2. New tables will be created in PostgreSQL
3. You'll need to re-register users and add products again

## Production Recommendations

For production, consider:
1. **Separate databases per service** (microservices best practice)
2. **Database connection pooling** (already configured via Spring Boot)
3. **Backup strategy** for PostgreSQL
4. **Monitoring** database performance
5. **Use Spring Cloud Config** for externalized configuration

