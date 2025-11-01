# 🔧 **STOCK SYNC INSTRUCTIONS - CRITICAL FIX**

## **Problem Identified:**
1. Product Service (products.stock_quantity) = 2
2. Inventory Service record might be MISSING or OUT OF SYNC
3. Both orders succeeded because inventory wasn't properly validated
4. Stock not updating after purchase because services weren't synced

## **Root Cause:**
- **Inventory Service** is the SINGLE SOURCE OF TRUTH for stock
- **Product Service** stock is denormalized (can be stale)
- If Inventory Service record doesn't exist or is out of sync → Overselling occurs

## **Solution Applied:**
✅ OrderService now relies ONLY on Inventory Service for stock validation
✅ Product Service is just a quick pre-check
✅ Real validation happens in Inventory Service with pessimistic locking
✅ Stock deduction happens in both services (Inventory = primary, Product = sync)

## **CRITICAL STEPS - Run These NOW:**

### **Step 1: Sync Inventory with Products**
```bash
psql -h localhost -U ujjawalkumar -d postgres -f microservices/sync-inventory-with-products.sql
```

This will:
- Create inventory records for products that don't have inventory
- Update existing inventory to match Product Service stock
- Verify sync status

### **Step 2: Verify Sync Status**
```sql
-- Check if all products have inventory
SELECT 
    COUNT(*) as total_products,
    COUNT(i.id) as products_with_inventory,
    COUNT(*) - COUNT(i.id) as missing_inventory
FROM products p
LEFT JOIN inventory i ON p.id = i.product_id;

-- Check specific product (ID = 1)
SELECT 
    p.id,
    p.name,
    p.stock_quantity as product_stock,
    i.quantity as inventory_quantity,
    i.reserved_quantity,
    (i.quantity - i.reserved_quantity) as available
FROM products p
LEFT JOIN inventory i ON p.id = i.product_id
WHERE p.id = 1;
```

### **Step 3: Restart Services**
```bash
cd microservices
# Restart these services in order:
# 1. inventory-service
# 2. order-service  
# 3. product-service
```

## **How It Works Now:**

### **Order Creation Flow:**
```
Customer orders → OrderService
  ↓
1. Quick check: Product Service stock > 0? (pre-check only)
  ↓
2. REAL VALIDATION: Inventory Service reserveInventory()
  - Uses pessimistic locking (SELECT FOR UPDATE)
  - Checks: availableQuantity = quantity - reservedQuantity
  - If availableQuantity >= requested → Reserve ✅
  - If availableQuantity < requested → Reject ❌
  ↓
3. Order created OR rejected based on Inventory Service response
```

### **Payment Success Flow:**
```
Payment success → Order status: PENDING → CONFIRMED
  ↓
1. Deduct from Inventory Service (PRIMARY - must succeed)
  - quantity decreases
  - reservedQuantity decreases
  ↓
2. Sync Product Service (SECONDARY - can fail)
  - stock_quantity decreases
  - Keeps frontend display accurate
```

## **Testing:**

### **Test Scenario 1: Stock = 2, 2 Customers Order Simultaneously**
```
Initial: Inventory quantity = 2, reservedQuantity = 0

Customer 1 orders 2 items:
  → Lock row → availableQuantity = 2 - 0 = 2
  → 2 >= 2? YES → Reserve 2
  → reservedQuantity = 2 → Commit ✅

Customer 2 orders 1 item:
  → Wait for lock → Lock row
  → availableQuantity = 2 - 2 = 0
  → 0 >= 1? NO → Return false → Order rejected ❌
```

### **Test Scenario 2: Verify Stock Updates**
```
1. Check product stock before order: products.stock_quantity = 2
2. Create order with quantity 2
3. Complete payment
4. Check product stock after: products.stock_quantity = 0 ✅
5. Check inventory: inventory.quantity = 0, reservedQuantity = 0 ✅
```

## **Monitoring:**
- Check logs for "🔒 Reserving stock in Inventory Service"
- Check logs for "✅ Stock reserved successfully"
- Check logs for "❌ Stock reservation failed" (indicates overselling prevention working)
- Monitor inventory table for sync issues

