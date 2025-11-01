# 🛑 **How to Stop All Services**

## **Why Services Keep Running After Closing IDE?**

When you run services using:
- `mvn spring-boot:run`
- `./start-all.sh`
- Running from IDE

The Java processes run in the **background** and are **independent** of your IDE or terminal window. Closing the IDE/terminal doesn't automatically stop them.

## **Solution: Use Stop Script**

### **Stop All Services:**
```bash
cd microservices
./stop-all.sh
```

This script will:
1. ✅ Stop all Spring Boot services
2. ✅ Stop React UI
3. ✅ Kill Maven processes
4. ✅ Free up ports (8080-8086, 8761, 3000)

## **Manual Methods:**

### **Method 1: Using Stop Script (Recommended)**
```bash
./stop-all.sh
```

### **Method 2: Kill by Port**
```bash
# Stop specific service by port
lsof -ti:8080 | xargs kill -9  # API Gateway
lsof -ti:8081 | xargs kill -9  # User Service
lsof -ti:8082 | xargs kill -9  # Product Service
lsof -ti:8083 | xargs kill -9  # Order Service
lsof -ti:8084 | xargs kill -9  # Inventory Service
lsof -ti:8085 | xargs kill -9  # Payment Service
lsof -ti:8086 | xargs kill -9  # Notification Service
lsof -ti:8761 | xargs kill -9  # Eureka
lsof -ti:3000 | xargs kill -9  # React UI
```

### **Method 3: Kill by Process Name**
```bash
# Kill all Java processes
pkill -f "ecommerce"
pkill -f "spring-boot"

# Kill React UI
pkill -f "react-scripts"
```

### **Method 4: Find and Kill (Mac/Linux)**
```bash
# Find running Java processes
jps -l | grep ecommerce

# Kill specific PID
kill -9 <PID>

# Or kill all at once
jps -l | grep ecommerce | awk '{print $1}' | xargs kill -9
```

## **Verify Services Are Stopped:**

### **Check Running Processes:**
```bash
# Check Java processes
ps aux | grep java | grep -i ecommerce

# Check ports
lsof -ti:8080,8081,8082,8083,8084,8085,8086,8761,3000

# Should return nothing if all stopped
```

### **Check Service URLs:**
```bash
# Try to access services (should fail)
curl http://localhost:8080  # API Gateway
curl http://localhost:8761  # Eureka
curl http://localhost:3000  # React UI

# Should return "Connection refused" if stopped
```

## **Prevent This in Future:**

### **Option 1: Use Stop Script Always**
Before closing IDE, run:
```bash
./stop-all.sh
```

### **Option 2: Run in Foreground**
Instead of background processes, run in separate terminal windows:
```bash
# Terminal 1
cd service-registry && mvn spring-boot:run

# Terminal 2
cd user-service && mvn spring-boot:run

# ... etc

# Close terminals to stop (Ctrl+C)
```

### **Option 3: Use Docker Compose (Recommended)**
If using Docker:
```bash
# Start
docker-compose up

# Stop
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

### **Option 4: Use Process Manager**
Use tools like:
- `pm2` (Node.js process manager)
- `screen` or `tmux` (terminal multiplexers)
- `systemd` (Linux service manager)

## **Troubleshooting:**

### **If Services Won't Stop:**

1. **Force Kill:**
   ```bash
   pkill -9 -f "ecommerce"
   pkill -9 -f "spring-boot"
   ```

2. **Check Zombie Processes:**
   ```bash
   ps aux | grep defunct
   ```

3. **Restart System (Last Resort):**
   - Sometimes processes get stuck
   - Restart your computer

### **If Ports Are Still In Use:**

```bash
# Find process using port
lsof -i :8080

# Kill it
kill -9 <PID>

# Or on Linux:
fuser -k 8080/tcp
```

## **Quick Reference:**

| Service | Port | Stop Command |
|---------|------|--------------|
| Service Registry | 8761 | `lsof -ti:8761 \| xargs kill -9` |
| API Gateway | 8080 | `lsof -ti:8080 \| xargs kill -9` |
| User Service | 8081 | `lsof -ti:8081 \| xargs kill -9` |
| Product Service | 8082 | `lsof -ti:8082 \| xargs kill -9` |
| Order Service | 8083 | `lsof -ti:8083 \| xargs kill -9` |
| Inventory Service | 8084 | `lsof -ti:8084 \| xargs kill -9` |
| Payment Service | 8085 | `lsof -ti:8085 \| xargs kill -9` |
| Notification Service | 8086 | `lsof -ti:8086 \| xargs kill -9` |
| React UI | 3000 | `lsof -ti:3000 \| xargs kill -9` |

## **Summary:**

✅ **Created:** `stop-all.sh` script  
✅ **Usage:** `./stop-all.sh`  
✅ **Stops:** All services, React UI, Maven processes  
✅ **Frees:** All ports (8080-8086, 8761, 3000)

**Always run `./stop-all.sh` before closing your IDE or terminal!** 🚀

