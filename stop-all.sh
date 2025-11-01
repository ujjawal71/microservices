#!/bin/bash

echo "🛑 Stopping all Microservices..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to kill process by name pattern
kill_service() {
    local service_name=$1
    local pids=$(ps aux | grep -i "$service_name" | grep -v grep | awk '{print $2}')
    
    if [ -z "$pids" ]; then
        echo -e "${YELLOW}⚠️  $service_name: Not running${NC}"
        return 0
    fi
    
    for pid in $pids; do
        echo -e "${RED}🛑 Killing $service_name (PID: $pid)...${NC}"
        kill -TERM $pid 2>/dev/null || kill -9 $pid 2>/dev/null
    done
    
    # Wait a bit and verify
    sleep 1
    remaining=$(ps aux | grep -i "$service_name" | grep -v grep | awk '{print $2}')
    if [ ! -z "$remaining" ]; then
        for pid in $remaining; do
            echo -e "${RED}🔪 Force killing $service_name (PID: $pid)...${NC}"
            kill -9 $pid 2>/dev/null
        done
    fi
    
    echo -e "${GREEN}✅ $service_name stopped${NC}"
}

# Function to kill processes by port
kill_by_port() {
    local port=$1
    local pids=$(lsof -ti:$port 2>/dev/null)
    
    if [ -z "$pids" ]; then
        return 0
    fi
    
    for pid in $pids; do
        echo -e "${RED}🛑 Killing process on port $port (PID: $pid)...${NC}"
        kill -TERM $pid 2>/dev/null || kill -9 $pid 2>/dev/null
    done
}

# Stop services by application name
echo ""
echo "=========================================="
echo "Stopping Spring Boot Services..."
echo "=========================================="

kill_service "ServiceRegistryApplication"
kill_service "UserServiceApplication"
kill_service "ProductServiceApplication"
kill_service "OrderServiceApplication"
kill_service "InventoryServiceApplication"
kill_service "PaymentServiceApplication"
kill_service "NotificationServiceApplication"
kill_service "ApiGatewayApplication"

# Stop React UI
echo ""
echo "=========================================="
echo "Stopping React UI..."
echo "=========================================="
kill_service "react-scripts"

# Also kill by ports (in case processes are still running)
echo ""
echo "=========================================="
echo "Cleaning up ports..."
echo "=========================================="

kill_by_port 8761  # Eureka
kill_by_port 8080  # API Gateway
kill_by_port 8081  # User Service
kill_by_port 8082  # Product Service
kill_by_port 8083  # Order Service
kill_by_port 8084  # Inventory Service
kill_by_port 8085  # Payment Service
kill_by_port 8086  # Notification Service
kill_by_port 3000  # React UI

# Kill Maven processes (mvn spring-boot:run)
echo ""
echo "=========================================="
echo "Stopping Maven processes..."
echo "=========================================="
pids=$(ps aux | grep "maven\|mvn.*spring-boot:run" | grep -v grep | awk '{print $2}')
if [ ! -z "$pids" ]; then
    for pid in $pids; do
        echo -e "${RED}🛑 Killing Maven process (PID: $pid)...${NC}"
        kill -TERM $pid 2>/dev/null || kill -9 $pid 2>/dev/null
    done
fi

# Final check
echo ""
echo "=========================================="
echo "Final Check..."
echo "=========================================="
sleep 2

remaining_java=$(ps aux | grep -i "ecommerce\|spring" | grep java | grep -v grep)
if [ -z "$remaining_java" ]; then
    echo -e "${GREEN}✅ All services stopped successfully!${NC}"
else
    echo -e "${YELLOW}⚠️  Some processes may still be running:${NC}"
    echo "$remaining_java"
    echo ""
    echo "To force kill remaining processes, run:"
    echo "  pkill -f 'ecommerce|spring'"
fi

echo ""
echo "=========================================="
echo "Done!"
echo "=========================================="

