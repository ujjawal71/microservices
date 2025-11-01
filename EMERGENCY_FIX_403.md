# Emergency Fix for 403 Error

## Problem
Your services are running with OLD CODE that doesn't have the CORS and login fixes.

## Immediate Solution: Rebuild Services

### If you have Maven installed:

```bash
# 1. Stop all services
pkill -f "spring-boot:run"

# 2. Rebuild and restart user-service
cd microservices/user-service
mvn clean package -DskipTests
mvn spring-boot:run > /tmp/user-service.log 2>&1 &
cd ../../

# 3. Wait 20 seconds
sleep 20

# 4. Rebuild and restart api-gateway
cd microservices/api-gateway
mvn clean package -DskipTests
mvn spring-boot:run > /tmp/api-gateway.log 2>&1 &
cd ../../

# 5. Wait 20 seconds and test
sleep 20
```

### If you DON'T have Maven:

**Option A: Install Maven**
```bash
brew install maven
```

**Option B: Use an IDE (IntelliJ IDEA / Eclipse)**
1. Open the project in your IDE
2. Right-click on `microservices/user-service` → Maven → Reload
3. Right-click on `UserServiceApplication.java` → Run
4. Repeat for API Gateway

**Option C: Use the pre-built restart script**
```bash
/tmp/restart-services.sh
```

## Why 403 is happening:

1. ❌ User-service has OLD SecurityConfig without CORS
2. ❌ User-service has OLD code without `findByUsernameOrEmail` method
3. ❌ API Gateway might have routing issues

## Current Frontend Status:

I've updated the frontend to bypass the API Gateway temporarily.
It now calls user-service directly at `http://localhost:8081`

But the user-service STILL needs to be rebuilt to work properly.

## After Rebuilding:

1. The frontend will work with the direct service call
2. Or you can revert to use API Gateway after both services are rebuilt

## Verify Fix:

After restarting, test:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"password"}'
```

You should get a JSON response with a token, not "User not found" or 403.

