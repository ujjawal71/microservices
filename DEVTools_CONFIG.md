# 🔄 **Spring Boot DevTools - Auto Restart Configuration**

## **What is Spring Boot DevTools?**

Spring Boot DevTools provides automatic application restart when code changes are detected. This eliminates the need to manually restart services during development.

## **Features:**

1. **Automatic Restart:**
   - Detects changes in compiled `.class` files
   - Triggers restart automatically (~5-10 seconds)
   - Only restarts the changed service (not all services)

2. **Fast Restart:**
   - Uses two-classloader architecture
   - Base classes (dependencies) stay loaded
   - Only restarts application classes (your code)
   - Much faster than full JVM restart

3. **LiveReload Support:**
   - Optional browser refresh integration
   - Can be disabled if not needed

## **How It Works:**

```
1. You edit Java file (e.g., OrderService.java)
   ↓
2. IDE compiles file → Generates new .class file
   ↓
3. DevTools detects .class file change
   ↓
4. DevTools triggers restart (~5-10 seconds)
   ↓
5. Application restarts with new code ✅
```

## **Usage:**

### **Method 1: Using Maven**
```bash
# Navigate to service directory
cd microservices/user-service

# Run with Maven (DevTools will auto-detect)
mvn spring-boot:run

# Now edit any Java file and save
# → Service will automatically restart!
```

### **Method 2: Using IDE (IntelliJ IDEA / Eclipse / VS Code)**
1. Run the service from your IDE
2. Make changes to Java files
3. Save the file (Ctrl+S / Cmd+S)
4. IDE compiles → DevTools detects → Auto restart ✅

### **Method 3: Build and Run JAR**
```bash
# Build JAR (includes DevTools)
mvn clean package

# Run JAR
java -jar target/user-service-1.0.0.jar

# DevTools will still work!
```

## **Configuration (Optional):**

You can customize DevTools behavior by adding to `application.yml`:

```yaml
spring:
  devtools:
    restart:
      enabled: true              # Enable/disable auto restart
      poll-interval: 1s          # How often to check for changes
      quiet-period: 400ms        # Wait time before restart
      trigger-file: .reloadtrigger  # Trigger restart manually (create this file)
    livereload:
      enabled: true              # Enable LiveReload (default: true)
      port: 35729                # LiveReload server port
```

## **What Triggers Restart:**

✅ **Triggers Restart:**
- Changes in Java source files (`.java`)
- Changes in compiled classes (`.class`)
- Changes in `application.yml` or `application.properties`
- Changes in `resources/` folder (templates, static files)

❌ **Does NOT Trigger Restart:**
- Changes in `test/` directory (tests don't affect runtime)
- Changes in `META-INF/maven/` (build info)

## **Performance:**

- **First Restart:** ~5-10 seconds (loads base classes)
- **Subsequent Restarts:** ~2-5 seconds (base classes cached)
- **Full JVM Restart (without DevTools):** ~20-30 seconds

## **Troubleshooting:**

### **Restart Not Happening?**
1. Check if DevTools dependency is present in `pom.xml`
2. Ensure `scope=runtime` and `optional=true`
3. Check IDE auto-compile is enabled
4. Try manually: `mvn compile` then check for restart

### **Too Many Restarts?**
- Increase `poll-interval` in `application.yml`
- Use `.reloadtrigger` file for manual restart control

### **Disable for Specific Service:**
```yaml
spring:
  devtools:
    restart:
      enabled: false
```

## **Best Practices:**

1. **Development Only:**
   - DevTools is automatically excluded from production builds (`optional=true`)
   - Only active when running from IDE or `spring-boot:run`

2. **IDE Settings:**
   - Enable "Build project automatically" in IntelliJ IDEA
   - Enable "Project > Build Automatically" in Eclipse
   - Save actions should compile on save

3. **Multiple Services:**
   - Each service restarts independently
   - Changing `user-service` code → only `user-service` restarts
   - Other services continue running normally

## **Example Workflow:**

```
1. Start all services:
   ./start-all.sh

2. Make code change in OrderService.java:
   - Add new method
   - Save file (Ctrl+S)

3. IDE compiles → .class file updated

4. DevTools detects change:
   → "Restarting due to 1 class path changes"
   → Order Service restarts (~5 seconds)

5. Other services unaffected ✅

6. Test your changes immediately!
```

## **LiveReload (Optional):**

If you want browser auto-refresh (for frontend changes):
1. Install LiveReload browser extension
2. DevTools automatically starts LiveReload server (port 35729)
3. Browser extension connects → Auto-refresh on file changes

**Note:** Only useful if you're modifying templates or static files.

---

## **Summary:**

✅ DevTools added to all 8 services
✅ Automatic restart on code changes
✅ Fast restart (~5 seconds vs 30 seconds)
✅ Development-only (not in production)
✅ No configuration needed - works out of the box!

**Just rebuild and restart services, then code changes will auto-restart! 🚀**


