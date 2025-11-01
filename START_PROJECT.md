# How to Run the E-Commerce Microservices Project

## Prerequisites Installation

### 0. Install and Start PostgreSQL
```bash
# On macOS with Homebrew:
brew install postgresql@14
brew services start postgresql@14

# Verify PostgreSQL is running:
psql -U ujjawalkumar -d postgres

# Database credentials (already configured):
# Host: localhost
# Port: 5432
# Database: postgres
# Username: ujjawalkumar
# Password: ujju
```

### 1. Install Java 17
```bash
# On macOS with Homebrew:
brew install openjdk@17

# Or download from: https://adoptium.net/
```

### 2. Install Maven
```bash
# On macOS with Homebrew:
brew install maven

# Or download from: https://maven.apache.org/download.cgi
```

### 3. Install Node.js and npm
```bash
# On macOS with Homebrew:
brew install node

# Or download from: https://nodejs.org/
```

### 4. Install Docker (for Kafka/Zookeeper)
```bash
# On macOS:
brew install --cask docker

# Then start Docker Desktop application
```

## Starting the Project

### Step 1: Start Kafka, Zookeeper, and Zipkin (using Docker)
```bash
cd microservices
docker-compose up -d zookeeper kafka zipkin

# Wait about 30 seconds for services to be ready
# Zipkin UI will be available at http://localhost:9411
```

### Step 2: Start Service Registry (Eureka)
Open Terminal 1:
```bash
cd microservices/service-registry
mvn spring-boot:run
```
Wait until you see "Started ServiceRegistryApplication" (usually takes 30-60 seconds)

### Step 3: Start API Gateway
Open Terminal 2:
```bash
cd microservices/api-gateway
mvn spring-boot:run
```

### Step 4: Start User Service
Open Terminal 3:
```bash
cd microservices/user-service
mvn spring-boot:run
```

### Step 5: Start Product Service
Open Terminal 4:
```bash
cd microservices/product-service
mvn spring-boot:run
```

### Step 6: Start Order Service
Open Terminal 5:
```bash
cd microservices/order-service
mvn spring-boot:run
```

### Step 7: Start Payment Service
Open Terminal 6:
```bash
cd microservices/payment-service
mvn spring-boot:run
```

### Step 8: Start Inventory Service
Open Terminal 7:
```bash
cd microservices/inventory-service
mvn spring-boot:run
```

### Step 9: Start Notification Service
Open Terminal 8:
```bash
cd microservices/notification-service
mvn spring-boot:run
```

### Step 10: Start React Frontend
Open Terminal 9:
```bash
cd microservices/ecommerce-ui
npm install  # Only needed first time
npm start
```

## Verify Services are Running

1. **Eureka Dashboard**: http://localhost:8761
   - Should show all services registered

2. **Zipkin UI**: http://localhost:9411
   - Distributed tracing dashboard
   - View traces across all microservices
   - Search by service, trace ID, or time range

3. **API Gateway**: http://localhost:8080
   - Test: http://localhost:8080/api/products

4. **Frontend**: http://localhost:3000
   - Should open automatically in browser

## Using Zipkin for Distributed Tracing

1. Open Zipkin UI at http://localhost:9411
2. Make some API calls through the frontend or directly
3. Click "Run Query" to see recent traces
4. Click on a trace to see the detailed timeline
5. Each service call will show:
   - Service name
   - Duration
   - HTTP method and path
   - Trace ID and Span ID
   - Parent-child relationships

### Example: Tracing an Order Request
When you place an order:
- Frontend → API Gateway → Order Service → Product Service
- Order Service → Payment Service
- Order Service → Inventory Service
- Order Service → Kafka (publish event)

All these calls will appear as a single trace in Zipkin!

## Quick Start Script (Alternative)

You can also create a script to start all services. Create `start-all.sh`:

```bash
#!/bin/bash

# Start Kafka
cd microservices
docker-compose up -d zookeeper kafka
sleep 10

# Start Eureka
cd service-registry
mvn spring-boot:run &
sleep 30

# Start all other services
cd ../api-gateway && mvn spring-boot:run &
cd ../user-service && mvn spring-boot:run &
cd ../product-service && mvn spring-boot:run &
cd ../order-service && mvn spring-boot:run &
cd ../payment-service && mvn spring-boot:run &
cd ../inventory-service && mvn spring-boot:run &
cd ../notification-service && mvn spring-boot:run &

# Start frontend
cd ecommerce-ui
npm start
```

Make it executable: `chmod +x start-all.sh`
Run it: `./start-all.sh`

## Troubleshooting

### Port Already in Use
If you get "port already in use" error:
```bash
# Find process using port (e.g., 8761)
lsof -ti:8761 | xargs kill -9
```

### Maven Build Fails
```bash
# Clean and rebuild
cd microservices/[service-name]
mvn clean install
mvn spring-boot:run
```

### Kafka Connection Issues
```bash
# Check if Kafka is running
docker ps | grep kafka

# Restart Kafka
docker-compose restart kafka
```

### Zipkin Not Showing Traces
```bash
# Check if Zipkin is running
docker ps | grep zipkin

# Restart Zipkin
docker-compose restart zipkin

# Verify services are configured to send traces
# Check application.yml has management.zipkin.tracing.endpoint
```

### Services Not Registering with Eureka
- Make sure Eureka is running first
- Check service logs for connection errors
- Verify `application.yml` has correct Eureka URL

## Testing the Application

1. Open http://localhost:3000 in browser
2. Register a new user
3. Browse products
4. Add products to cart
5. Place an order
6. Check orders page

## Stopping All Services

Press `Ctrl+C` in each terminal, or:

```bash
# Stop all Spring Boot processes
pkill -f "spring-boot:run"

# Stop Docker containers
cd microservices
docker-compose down
```

