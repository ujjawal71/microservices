-- SQL Script to Update Product Stock Quantity
-- This script sets stock_quantity to 10 for all products where stock_quantity is NULL or 0
-- Default stock value: 10

-- Update products with NULL stock_quantity to 10
UPDATE products
SET stock_quantity = 10
WHERE stock_quantity IS NULL;

-- Update products with stock_quantity = 0 to 10 (optional - uncomment if you want to restock all out-of-stock items)
-- UPDATE products
-- SET stock_quantity = 10
-- WHERE stock_quantity = 0;

-- Verify the update
SELECT id, name, stock_quantity 
FROM products 
ORDER BY id 
LIMIT 10;

-- Count products by stock status
SELECT 
    CASE 
        WHEN stock_quantity > 0 THEN 'In Stock'
        WHEN stock_quantity = 0 THEN 'Out of Stock'
        ELSE 'No Stock Info'
    END as stock_status,
    COUNT(*) as count
FROM products
GROUP BY stock_status;


