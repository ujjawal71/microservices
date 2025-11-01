# Quick Fix for 403 Error

## The Problem
Your services are running with OLD CODE that doesn't have:
1. CORS configuration in SecurityConfig
2. findByUsernameOrEmail method
3. Updated AuthService

## Solution: Rebuild and Restart

### Option 1: Quick Restart (if Maven is available)

```bash
# 1. Stop all services
pkill -f "spring-boot:run"

# 2. Rebuild user-service
cd microservices/user-service
mvn clean package -DskipTests
mvn spring-boot:run > /tmp/user-service.log 2>&1 &
cd ../../

# 3. Rebuild api-gateway
cd microservices/api-gateway
mvn clean package -DskipTests
mvn spring-boot:run > /tmp/api-gateway.log 2>&1 &
cd ../../

# 4. Wait 30 seconds, then test login
```

### Option 2: Temporary Workaround - Direct Service Call

If you can't rebuild right now, you can temporarily test by:
1. Accessing user-service directly: http://localhost:8081/api/auth/login
2. Or disable CORS in browser (for testing only)

## Verification

After restarting, verify services:
```bash
# Check Eureka
curl http://localhost:8761

# Check user-service directly
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"password"}'

# Check via API Gateway
curl -X POST http://localhost:8080/api/users/auth/login \
  -H "Content-Type: application/json" \
  -H "Origin: http://localhost:3000" \
  -d '{"username":"user1","password":"password"}'
```

## Expected Result After Fix

You should get a JSON response with:
```json
{
  "token": "eyJhbGc...",
  "type": "Bearer",
  "id": 1,
  "username": "user1",
  "email": "user1@example.com"
}
```

