#!/bin/bash

echo "🛑 Stopping user-service and api-gateway..."
pkill -f "user-service.*spring-boot:run"
pkill -f "api-gateway.*spring-boot:run"
sleep 3

echo "🔨 Rebuilding user-service..."
cd /Users/ujjawalkumar/Documents/GitHub/MyProjects/microservices/user-service
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
    echo "✅ User service built successfully"
    mvn spring-boot:run > /tmp/user-service-new.log 2>&1 &
    echo "🚀 User service starting (PID: $!)"
else
    echo "❌ User service build failed"
    exit 1
fi

echo "⏳ Waiting 20 seconds for user-service to start..."
sleep 20

echo "🔨 Rebuilding api-gateway..."
cd /Users/ujjawalkumar/Documents/GitHub/MyProjects/microservices/api-gateway
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
    echo "✅ API Gateway built successfully"
    mvn spring-boot:run > /tmp/api-gateway-new.log 2>&1 &
    echo "🚀 API Gateway starting (PID: $!)"
else
    echo "❌ API Gateway build failed"
    exit 1
fi

echo "⏳ Waiting 30 seconds for services to fully start..."
sleep 30

echo ""
echo "✅ Services restarted!"
echo ""
echo "🧪 Testing login endpoint..."
curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"password"}' | head -3

echo ""
echo ""
echo "📋 Next steps:"
echo "1. Refresh your browser"
echo "2. Try logging in with: user1 / password"
echo "3. Check logs: tail -f /tmp/user-service-new.log"
