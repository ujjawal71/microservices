#!/bin/bash

echo "=========================================="
echo "E-Commerce Microservices Startup Script"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
check_prerequisites() {
    echo "Checking prerequisites..."
    
    # Check Java
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -ge 17 ]; then
            echo -e "${GREEN}✓${NC} Java $JAVA_VERSION is installed"
        else
            echo -e "${RED}✗${NC} Java 17 or higher is required (found: $JAVA_VERSION)"
            return 1
        fi
    else
        echo -e "${RED}✗${NC} Java is not installed"
        echo "   Install with: brew install openjdk@17"
        return 1
    fi
    
    # Check Maven
    if command -v mvn &> /dev/null; then
        echo -e "${GREEN}✓${NC} Maven is installed"
    else
        echo -e "${RED}✗${NC} Maven is not installed"
        echo "   Install with: brew install maven"
        return 1
    fi
    
    # Check Node.js
    if command -v node &> /dev/null; then
        echo -e "${GREEN}✓${NC} Node.js is installed"
    else
        echo -e "${RED}✗${NC} Node.js is not installed"
        echo "   Install with: brew install node"
        return 1
    fi
    
    # Check Docker
    if command -v docker &> /dev/null; then
        if docker ps &> /dev/null; then
            echo -e "${GREEN}✓${NC} Docker is running"
        else
            echo -e "${YELLOW}⚠${NC} Docker is installed but not running"
            echo "   Please start Docker Desktop"
        fi
    else
        echo -e "${YELLOW}⚠${NC} Docker is not installed (needed for Kafka)"
        echo "   Install with: brew install --cask docker"
    fi
    
    echo ""
    return 0
}

# Start Kafka and Zipkin
start_kafka() {
    if command -v docker &> /dev/null && docker ps &> /dev/null; then
        echo "Starting Kafka, Zookeeper, and Zipkin..."
        docker-compose up -d zookeeper kafka zipkin
        echo "Waiting for services to be ready..."
        sleep 15
    else
        echo -e "${YELLOW}⚠${NC} Skipping Kafka and Zipkin (Docker not available)"
        echo "   Note: Order, Payment, Inventory, and Notification services need Kafka"
        echo "   Note: Distributed tracing requires Zipkin"
    fi
}

# Function to start a service
start_service() {
    SERVICE_NAME=$1
    SERVICE_DIR=$2
    PORT=$3
    
    echo "Starting $SERVICE_NAME on port $PORT..."
    cd "$SERVICE_DIR"
    mvn spring-boot:run > "/tmp/$SERVICE_NAME.log" 2>&1 &
    cd - > /dev/null
    sleep 5
}

# Main execution
if ! check_prerequisites; then
    echo ""
    echo -e "${RED}Please install missing prerequisites before continuing.${NC}"
    echo "See START_PROJECT.md for detailed instructions."
    exit 1
fi

echo -e "${GREEN}All prerequisites met!${NC}"
echo ""
read -p "Do you want to start all services? (y/n) " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Aborted."
    exit 0
fi

echo ""
echo "Starting services..."
echo ""

# Start Kafka first
start_kafka

# Start Eureka (must be first)
echo "Starting Eureka Service Registry..."
cd service-registry
mvn spring-boot:run > /tmp/eureka.log 2>&1 &
EUREKA_PID=$!
cd - > /dev/null
echo "Waiting for Eureka to start (30 seconds)..."
sleep 30

# Start all other services
echo "Starting API Gateway..."
start_service "api-gateway" "api-gateway" "8080"

echo "Starting User Service..."
start_service "user-service" "user-service" "8081"

echo "Starting Product Service..."
start_service "product-service" "product-service" "8082"

echo "Starting Order Service..."
start_service "order-service" "order-service" "8083"

echo "Starting Payment Service..."
start_service "payment-service" "payment-service" "8084"

echo "Starting Inventory Service..."
start_service "inventory-service" "inventory-service" "8085"

echo "Starting Notification Service..."
start_service "notification-service" "notification-service" "8086"

echo ""
echo "Waiting for services to initialize (20 seconds)..."
sleep 20

# Start frontend
echo "Starting React Frontend..."
cd ecommerce-ui
if [ ! -d "node_modules" ]; then
    echo "Installing npm dependencies (first time only)..."
    npm install
fi
npm start > /tmp/react-frontend.log 2>&1 &
cd - > /dev/null

echo ""
echo "=========================================="
echo -e "${GREEN}Services are starting!${NC}"
echo "=========================================="
echo ""
echo "Service URLs:"
echo "  - Eureka Dashboard: http://localhost:8761"
echo "  - Zipkin UI: http://localhost:9411"
echo "  - API Gateway: http://localhost:8080"
echo "  - Frontend: http://localhost:3000 (will open automatically)"
echo ""
echo "Logs are available in /tmp/*.log"
echo ""
echo "To stop all services, run: pkill -f 'spring-boot:run' && pkill -f 'react-scripts'"
echo ""

