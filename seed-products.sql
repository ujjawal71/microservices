-- Seed 10,000 products (run after product-service creates the products table)
DO $$
DECLARE
    i INTEGER;
    categories TEXT[] := ARRAY['Electronics', 'Clothing', 'Books', 'Home & Garden', 'Sports & Outdoors', 'Toys & Games', 'Health & Beauty', 'Automotive', 'Food & Beverages', 'Pet Supplies', 'Office Supplies', 'Jewelry', 'Musical Instruments', 'Baby Products', 'Fashion Accessories'];
    product_names TEXT[];
    category TEXT;
    product_name TEXT;
    full_name TEXT;
    description TEXT;
    price NUMERIC;
    stock_qty INTEGER;
BEGIN
    FOR i IN 1..10000 LOOP
        category := categories[1 + (random() * (array_length(categories, 1) - 1))::INT];
        
        -- Select product name based on category
        CASE category
            WHEN 'Electronics' THEN product_names := ARRAY['Smartphone', 'Laptop', 'Headphones', 'Smart Watch', 'Tablet', 'Camera', 'Speaker', 'Keyboard', 'Mouse', 'Monitor'];
            WHEN 'Clothing' THEN product_names := ARRAY['T-Shirt', 'Jeans', 'Jacket', 'Shoes', 'Hat', 'Sweater', 'Dress', 'Shorts', 'Pants', 'Coat'];
            WHEN 'Books' THEN product_names := ARRAY['Novel', 'Textbook', 'Cookbook', 'Biography', 'Mystery', 'Science Fiction', 'Fantasy', 'Romance', 'Thriller', 'History'];
            WHEN 'Home & Garden' THEN product_names := ARRAY['Garden Tool', 'Plant Pot', 'Outdoor Furniture', 'Grill', 'Lantern', 'Vase', 'Candle', 'Planter', 'Shovel', 'Lawn Mower'];
            WHEN 'Sports & Outdoors' THEN product_names := ARRAY['Basketball', 'Tennis Racket', 'Yoga Mat', 'Dumbbells', 'Bicycle', 'Running Shoes', 'Water Bottle', 'Tent', 'Backpack', 'Helmet'];
            WHEN 'Toys & Games' THEN product_names := ARRAY['Board Game', 'Puzzle', 'Action Figure', 'Doll', 'Remote Control Car', 'LEGO Set', 'Building Blocks', 'Card Game', 'Chess Set', 'Puzzle'];
            WHEN 'Health & Beauty' THEN product_names := ARRAY['Shampoo', 'Moisturizer', 'Perfume', 'Sunscreen', 'Vitamins', 'Face Mask', 'Lipstick', 'Toothbrush', 'Soap', 'Lotion'];
            WHEN 'Automotive' THEN product_names := ARRAY['Car Charger', 'Phone Mount', 'Dash Cam', 'Car Mat', 'Steering Wheel Cover', 'Tire Gauge', 'Air Freshener', 'Car Wax', 'Tire Inflator', 'Jump Starter'];
            WHEN 'Food & Beverages' THEN product_names := ARRAY['Coffee Beans', 'Tea', 'Snacks', 'Energy Drink', 'Protein Bar', 'Chocolate', 'Wine', 'Beer', 'Juice', 'Cereal'];
            WHEN 'Pet Supplies' THEN product_names := ARRAY['Dog Food', 'Cat Litter', 'Pet Toy', 'Leash', 'Pet Bed', 'Pet Bowl', 'Pet Collar', 'Pet Grooming Brush', 'Cat Scratching Post', 'Dog Treats'];
            WHEN 'Office Supplies' THEN product_names := ARRAY['Notebook', 'Pen Set', 'Desk Organizer', 'Stapler', 'Paper Clip', 'Highlighter', 'Calculator', 'File Folder', 'Binder', 'Erasers'];
            WHEN 'Jewelry' THEN product_names := ARRAY['Necklace', 'Ring', 'Earrings', 'Bracelet', 'Watch', 'Brooch', 'Anklet', 'Pendant', 'Cufflinks', 'Charm'];
            WHEN 'Musical Instruments' THEN product_names := ARRAY['Guitar', 'Piano', 'Drums', 'Violin', 'Microphone', 'Keyboard', 'Ukulele', 'Flute', 'Saxophone', 'Trumpet'];
            WHEN 'Baby Products' THEN product_names := ARRAY['Baby Stroller', 'Diaper Bag', 'Baby Bottle', 'Pacifier', 'Baby Clothes', 'Baby Monitor', 'Baby Blanket', 'High Chair', 'Baby Carrier', 'Baby Toys'];
            ELSE product_names := ARRAY['Sunglasses', 'Wallet', 'Belt', 'Backpack', 'Handbag', 'Scarf', 'Gloves', 'Socks', 'Tie', 'Cap'];
        END CASE;
        
        product_name := product_names[1 + (random() * (array_length(product_names, 1) - 1))::INT];
        full_name := product_name || ' ' || i;
        description := 'This is a high-quality ' || LOWER(product_name) || ' perfect for everyday use. Features premium materials and excellent craftsmanship. Designed for durability and style.';
        price := (19.99 + (random() * 980.01))::NUMERIC(10,2);
        stock_qty := (10 + (random() * 500))::INT;
        
        INSERT INTO products (name, description, price, category, image_url, stock_quantity)
        VALUES (full_name, description, price, category, 'https://picsum.photos/seed/' || i || '/400/400', stock_qty)
        ON CONFLICT DO NOTHING;
        
        IF i % 1000 = 0 THEN
            RAISE NOTICE 'Inserted % products', i;
        END IF;
    END LOOP;
    RAISE NOTICE 'Finished inserting 10,000 products';
END $$;

-- Create inventory records
INSERT INTO inventory (product_id, quantity, reserved_quantity)
SELECT 
    p.id as product_id,
    p.stock_quantity as quantity,
    (random() * 10)::INTEGER as reserved_quantity
FROM products p
WHERE NOT EXISTS (
    SELECT 1 FROM inventory i WHERE i.product_id = p.id
);

SELECT 
    (SELECT COUNT(*) FROM products) as total_products,
    (SELECT COUNT(*) FROM inventory) as total_inventory_records;

