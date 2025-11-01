-- Seed 10,000 users
DO $$
DECLARE
    i INTEGER;
    first_names TEXT[] := ARRAY['John', 'Jane', 'Michael', 'Sarah', 'David', 'Emily', 'James', 'Emma', 'Robert', 'Olivia', 'William', 'Ava', 'Richard', 'Isabella', 'Joseph', 'Sophia', 'Thomas', 'Charlotte', 'Charles', 'Mia'];
    last_names TEXT[] := ARRAY['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez', 'Hernandez', 'Lopez', 'Wilson', 'Anderson', 'Thomas', 'Taylor', 'Moore', 'Jackson', 'Martin', 'Lee'];
    addresses TEXT[] := ARRAY['123 Main St, New York, NY 10001', '456 Oak Ave, Los Angeles, CA 90001', '789 Pine Rd, Chicago, IL 60601', '321 Elm St, Houston, TX 77001', '654 Maple Dr, Phoenix, AZ 85001', '987 Cedar Ln, Philadelphia, PA 19101', '147 Birch Way, San Antonio, TX 78201', '258 Spruce Ct, San Diego, CA 92101', '369 Willow Blvd, Dallas, TX 75201', '741 Cherry St, San Jose, CA 95101'];
    default_password TEXT := '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';
BEGIN
    FOR i IN 1..10000 LOOP
        INSERT INTO users (username, email, password, first_name, last_name, phone, address, role)
        VALUES (
            'user' || i,
            'user' || i || '@example.com',
            default_password,
            first_names[1 + (random() * (array_length(first_names, 1) - 1))::INT],
            last_names[1 + (random() * (array_length(last_names, 1) - 1))::INT],
            '+1-555-' || LPAD((500 + (random() * 499)::INT)::TEXT, 3, '0') || '-' || LPAD((random() * 9999)::INT::TEXT, 4, '0'),
            addresses[1 + (random() * (array_length(addresses, 1) - 1))::INT],
            CASE WHEN random() < 0.05 THEN 'ADMIN' ELSE 'USER' END
        ) ON CONFLICT (username) DO NOTHING;
        
        IF i % 1000 = 0 THEN
            RAISE NOTICE 'Inserted % users', i;
        END IF;
    END LOOP;
    RAISE NOTICE 'Finished inserting 10,000 users';
END $$;

SELECT COUNT(*) as total_users FROM users;

