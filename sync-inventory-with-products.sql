-- SQL Script to Sync Inventory with Products
-- CRITICAL: Inventory Service must have records for all products
-- This script creates/updates inventory records based on Product Service stock

-- Step 1: Create inventory records for products that don't have inventory
-- Uses Product Service stock_quantity as source of truth
INSERT INTO inventory (product_id, quantity, reserved_quantity)
SELECT 
    p.id as product_id,
    COALESCE(p.stock_quantity, 0) as quantity,
    0 as reserved_quantity
FROM products p
WHERE NOT EXISTS (
    SELECT 1 FROM inventory i WHERE i.product_id = p.id
)
ON CONFLICT DO NOTHING;

-- Step 2: Update existing inventory records to match Product Service stock
-- Only update if inventory quantity differs from product stock_quantity
UPDATE inventory i
SET quantity = COALESCE(p.stock_quantity, 0)
FROM products p
WHERE i.product_id = p.id
  AND i.quantity != COALESCE(p.stock_quantity, 0);

-- Step 3: Verify sync (show products and their inventory)
SELECT 
    p.id as product_id,
    p.name as product_name,
    p.stock_quantity as product_stock,
    i.quantity as inventory_quantity,
    i.reserved_quantity as inventory_reserved,
    (i.quantity - i.reserved_quantity) as available_quantity,
    CASE 
        WHEN i.id IS NULL THEN '❌ MISSING INVENTORY'
        WHEN p.stock_quantity != i.quantity THEN '⚠️ OUT OF SYNC'
        ELSE '✅ SYNCED'
    END as sync_status
FROM products p
LEFT JOIN inventory i ON p.id = i.product_id
ORDER BY p.id
LIMIT 20;


