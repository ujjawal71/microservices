#!/usr/bin/env python3
"""
Seed script to insert 10,000 dummy users and 10,000 dummy products
into PostgreSQL database for E-Commerce application
"""

import psycopg2
import random
import bcrypt
from faker import Faker

# Database configuration
DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'database': 'postgres',
    'user': 'ujjawalkumar',
    'password': 'ujju'
}

fake = Faker()

def get_db_connection():
    """Create and return database connection"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        return conn
    except Exception as e:
        print(f"Error connecting to database: {e}")
        raise

def hash_password(password):
    """Hash password using bcrypt"""
    return bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')

def insert_users(conn, count=10000):
    """Insert dummy users"""
    print(f"Inserting {count} users...")
    cursor = conn.cursor()
    
    # Default password hash for 'password'
    default_password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
    
    categories = ['Electronics', 'Clothing', 'Books', 'Home & Garden', 'Sports & Outdoors',
                  'Toys & Games', 'Health & Beauty', 'Automotive', 'Food & Beverages', 'Pet Supplies',
                  'Office Supplies', 'Jewelry', 'Musical Instruments', 'Baby Products', 'Fashion Accessories']
    
    product_names = ['Smartphone', 'Laptop', 'Headphones', 'Smart Watch', 'Tablet', 'Camera', 
                    'Speaker', 'Keyboard', 'Mouse', 'Monitor', 'T-Shirt', 'Jeans', 'Jacket', 
                    'Shoes', 'Hat', 'Novel', 'Textbook', 'Cookbook', 'Biography', 'Mystery']
    
    batch_size = 100
    total_inserted = 0
    
    for i in range(0, count, batch_size):
        batch_count = min(batch_size, count - i)
        users_data = []
        
        for j in range(batch_count):
            user_num = i + j + 1
            username = f'user{user_num}'
            email = f'user{user_num}@example.com'
            first_name = fake.first_name()
            last_name = fake.last_name()
            phone = fake.phone_number()
            address = fake.address().replace('\n', ', ')
            role = 'ADMIN' if random.random() < 0.05 else 'USER'
            
            users_data.append((
                username, email, default_password, first_name, 
                last_name, phone, address, role
            ))
        
        try:
            cursor.executemany("""
                INSERT INTO users (username, email, password, first_name, last_name, phone, address, role)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                ON CONFLICT DO NOTHING
            """, users_data)
            total_inserted += cursor.rowcount
            conn.commit()
            if (i + batch_count) % 1000 == 0:
                print(f"  Inserted {i + batch_count} users...")
        except Exception as e:
            print(f"Error inserting users batch {i}: {e}")
            conn.rollback()
    
    print(f"✅ Successfully inserted {total_inserted} users")
    cursor.close()
    return total_inserted

def insert_products(conn, count=10000):
    """Insert dummy products"""
    print(f"Inserting {count} products...")
    cursor = conn.cursor()
    
    categories = ['Electronics', 'Clothing', 'Books', 'Home & Garden', 'Sports & Outdoors',
                  'Toys & Games', 'Health & Beauty', 'Automotive', 'Food & Beverages', 'Pet Supplies',
                  'Office Supplies', 'Jewelry', 'Musical Instruments', 'Baby Products', 'Fashion Accessories']
    
    product_templates = {
        'Electronics': ['Smartphone', 'Laptop', 'Headphones', 'Smart Watch', 'Tablet', 'Camera', 'Speaker'],
        'Clothing': ['T-Shirt', 'Jeans', 'Jacket', 'Shoes', 'Hat', 'Sweater', 'Dress'],
        'Books': ['Novel', 'Textbook', 'Cookbook', 'Biography', 'Mystery', 'Science Fiction', 'Fantasy'],
        'Home & Garden': ['Garden Tool', 'Plant Pot', 'Outdoor Furniture', 'Grill', 'Lantern', 'Vase', 'Candle'],
        'Sports & Outdoors': ['Basketball', 'Tennis Racket', 'Yoga Mat', 'Dumbbells', 'Bicycle', 'Running Shoes', 'Water Bottle'],
        'Toys & Games': ['Board Game', 'Puzzle', 'Action Figure', 'Doll', 'Remote Control Car', 'LEGO Set', 'Puzzle'],
        'Health & Beauty': ['Shampoo', 'Moisturizer', 'Perfume', 'Sunscreen', 'Vitamins', 'Face Mask', 'Lipstick'],
        'Automotive': ['Car Charger', 'Phone Mount', 'Dash Cam', 'Car Mat', 'Steering Wheel Cover', 'Tire Gauge', 'Air Freshener'],
        'Food & Beverages': ['Coffee Beans', 'Tea', 'Snacks', 'Energy Drink', 'Protein Bar', 'Chocolate', 'Wine'],
        'Pet Supplies': ['Dog Food', 'Cat Litter', 'Pet Toy', 'Leash', 'Pet Bed', 'Pet Bowl', 'Pet Collar'],
        'Office Supplies': ['Notebook', 'Pen Set', 'Desk Organizer', 'Stapler', 'Paper Clip', 'Highlighter', 'Calculator'],
        'Jewelry': ['Necklace', 'Ring', 'Earrings', 'Bracelet', 'Watch', 'Brooch', 'Anklet'],
        'Musical Instruments': ['Guitar', 'Piano', 'Drums', 'Violin', 'Microphone', 'Keyboard', 'Ukulele'],
        'Baby Products': ['Baby Stroller', 'Diaper Bag', 'Baby Bottle', 'Pacifier', 'Baby Clothes', 'Baby Monitor', 'Baby Blanket'],
        'Fashion Accessories': ['Sunglasses', 'Wallet', 'Belt', 'Backpack', 'Handbag', 'Scarf', 'Gloves']
    }
    
    batch_size = 100
    total_inserted = 0
    
    for i in range(0, count, batch_size):
        batch_count = min(batch_size, count - i)
        products_data = []
        
        for j in range(batch_count):
            category = random.choice(categories)
            product_name = random.choice(product_templates[category])
            name = f"{product_name} {i + j + 1}"
            description = f"This is a high-quality {product_name.lower()} perfect for everyday use. Features premium materials and excellent craftsmanship. {fake.text(max_nb_chars=100)}"
            price = round(random.uniform(19.99, 1000.00), 2)
            image_url = f'https://picsum.photos/seed/{i + j + 1}/400/400'
            stock_quantity = random.randint(10, 510)
            
            products_data.append((
                name, description, price, category, image_url, stock_quantity
            ))
        
        try:
            cursor.executemany("""
                INSERT INTO products (name, description, price, category, image_url, stock_quantity)
                VALUES (%s, %s, %s, %s, %s, %s)
                ON CONFLICT DO NOTHING
            """, products_data)
            total_inserted += cursor.rowcount
            conn.commit()
            if (i + batch_count) % 1000 == 0:
                print(f"  Inserted {i + batch_count} products...")
        except Exception as e:
            print(f"Error inserting products batch {i}: {e}")
            conn.rollback()
    
    print(f"✅ Successfully inserted {total_inserted} products")
    cursor.close()
    return total_inserted

def insert_inventory(conn):
    """Insert inventory records for all products"""
    print("Creating inventory records for products...")
    cursor = conn.cursor()
    
    try:
        cursor.execute("""
            INSERT INTO inventory (product_id, quantity, reserved_quantity)
            SELECT 
                p.id as product_id,
                p.stock_quantity as quantity,
                FLOOR(RANDOM() * 10)::integer as reserved_quantity
            FROM products p
            WHERE NOT EXISTS (
                SELECT 1 FROM inventory i WHERE i.product_id = p.id
            )
        """)
        conn.commit()
        print(f"✅ Created {cursor.rowcount} inventory records")
        cursor.close()
        return cursor.rowcount
    except Exception as e:
        print(f"Error inserting inventory: {e}")
        conn.rollback()
        cursor.close()
        return 0

def get_summary(conn):
    """Get summary of inserted data"""
    cursor = conn.cursor()
    try:
        cursor.execute("""
            SELECT 
                (SELECT COUNT(*) FROM users) as total_users,
                (SELECT COUNT(*) FROM products) as total_products,
                (SELECT COUNT(*) FROM inventory) as total_inventory
        """)
        result = cursor.fetchone()
        cursor.close()
        return result
    except Exception as e:
        print(f"Error getting summary: {e}")
        cursor.close()
        return None

def main():
    """Main function"""
    print("=" * 60)
    print("E-Commerce Database Seeder")
    print("=" * 60)
    print()
    
    # Check if required packages are installed
    try:
        import psycopg2
    except ImportError:
        print("❌ Error: psycopg2 not installed. Install with: pip install psycopg2-binary")
        return
    
    try:
        import faker
    except ImportError:
        print("❌ Error: faker not installed. Install with: pip install faker")
        return
    
    try:
        conn = get_db_connection()
        print("✅ Connected to database")
        print()
        
        # Check if tables exist
        cursor = conn.cursor()
        cursor.execute("""
            SELECT table_name FROM information_schema.tables 
            WHERE table_schema = 'public' 
            AND table_name IN ('users', 'products', 'inventory')
        """)
        existing_tables = [row[0] for row in cursor.fetchall()]
        cursor.close()
        
        if 'users' not in existing_tables:
            print("⚠️  Warning: 'users' table does not exist.")
            print("   Please start the user-service first to create the table.")
            conn.close()
            return
        
        if 'products' not in existing_tables:
            print("⚠️  Warning: 'products' table does not exist.")
            print("   Please start the product-service first to create the table.")
            conn.close()
            return
        
        # Insert data
        users_count = insert_users(conn, 10000)
        print()
        products_count = insert_products(conn, 10000)
        print()
        inventory_count = insert_inventory(conn)
        print()
        
        # Display summary
        summary = get_summary(conn)
        if summary:
            print("=" * 60)
            print("Summary:")
            print(f"  Total Users: {summary[0]}")
            print(f"  Total Products: {summary[1]}")
            print(f"  Total Inventory Records: {summary[2]}")
            print("=" * 60)
        
        conn.close()
        print()
        print("✅ Seeding completed successfully!")
        
    except Exception as e:
        print(f"❌ Error: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()

