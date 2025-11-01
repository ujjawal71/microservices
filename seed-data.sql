-- Seed script for E-Commerce Database
-- Creates 10,000 dummy users and 10,000 dummy products
-- Run this AFTER starting your services so tables exist

-- Function to generate random string
CREATE OR REPLACE FUNCTION random_string(length INTEGER) RETURNS TEXT AS $$
DECLARE
    chars TEXT := 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    result TEXT := '';
    i INTEGER;
BEGIN
    FOR i IN 1..length LOOP
        result := result || substr(chars, floor(random() * length(chars) + 1)::INTEGER, 1);
    END LOOP;
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Insert 10,000 dummy users
DO $$
DECLARE
    i INTEGER;
    username TEXT;
    email TEXT;
    first_names TEXT[] := ARRAY['John', 'Jane', 'Michael', 'Sarah', 'David', 'Emily', 'James', 'Emma', 'Robert', 'Olivia', 'William', 'Ava', 'Richard', 'Isabella', 'Joseph', 'Sophia', 'Thomas', 'Charlotte', 'Charles', 'Mia'];
    last_names TEXT[] := ARRAY['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez', 'Hernandez', 'Lopez', 'Wilson', 'Anderson', 'Thomas', 'Taylor', 'Moore', 'Jackson', 'Martin', 'Lee'];
    addresses TEXT[] := ARRAY['123 Main St, New York, NY 10001', '456 Oak Ave, Los Angeles, CA 90001', '789 Pine Rd, Chicago, IL 60601', '321 Elm St, Houston, TX 77001', '654 Maple Dr, Phoenix, AZ 85001', '987 Cedar Ln, Philadelphia, PA 19101', '147 Birch Way, San Antonio, TX 78201', '258 Spruce Ct, San Diego, CA 92101', '369 Willow Blvd, Dallas, TX 75201', '741 Cherry St, San Jose, CA 95101'];
    phone_prefix TEXT;
    phone TEXT;
    role TEXT;
    default_password TEXT := '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'; -- password: "password"
BEGIN
    FOR i IN 1..10000 LOOP
        username := 'user' || i;
        email := 'user' || i || '@example.com';
        phone_prefix := LPAD((500 + (random() * 499)::INT)::TEXT, 3, '0');
        phone := '+1-555-' || phone_prefix || '-' || LPAD((random() * 9999)::INT::TEXT, 4, '0');
        role := CASE WHEN random() < 0.05 THEN 'ADMIN' ELSE 'USER' END;
        
        INSERT INTO users (username, email, password, first_name, last_name, phone, address, role)
        VALUES (
            username,
            email,
            default_password,
            first_names[1 + (random() * (array_length(first_names, 1) - 1))::INT],
            last_names[1 + (random() * (array_length(last_names, 1) - 1))::INT],
            phone,
            addresses[1 + (random() * (array_length(addresses, 1) - 1))::INT],
            role
        ) ON CONFLICT DO NOTHING;
        
        IF i % 1000 = 0 THEN
            RAISE NOTICE 'Inserted % users', i;
        END IF;
    END LOOP;
    RAISE NOTICE 'Finished inserting users';
END $$;

-- Insert 10,000 dummy products
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
    image_url TEXT;
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
        image_url := 'https://picsum.photos/seed/' || i || '/400/400';
        
        INSERT INTO products (name, description, price, category, image_url, stock_quantity)
        VALUES (full_name, description, price, category, image_url, stock_qty)
        ON CONFLICT DO NOTHING;
        
        IF i % 1000 = 0 THEN
            RAISE NOTICE 'Inserted % products', i;
        END IF;
    END LOOP;
    RAISE NOTICE 'Finished inserting products';
END $$;

-- Insert inventory records for all products
INSERT INTO inventory (product_id, quantity, reserved_quantity)
SELECT 
    p.id as product_id,
    p.stock_quantity as quantity,
    (random() * 10)::INTEGER as reserved_quantity
FROM products p
WHERE NOT EXISTS (
    SELECT 1 FROM inventory i WHERE i.product_id = p.id
);

-- Display summary
SELECT 
    (SELECT COUNT(*) FROM users) as total_users,
    (SELECT COUNT(*) FROM products) as total_products,
    (SELECT COUNT(*) FROM inventory) as total_inventory_records;

-- Clean up function
DROP FUNCTION IF EXISTS random_string(INTEGER);
