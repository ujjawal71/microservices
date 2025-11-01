# Seed Data Instructions

This guide explains how to add 10,000 dummy users and 10,000 dummy products to your e-commerce database.

## Prerequisites

1. **PostgreSQL must be running** with the configured database
2. **Tables must exist** (they will be created when you start the services)
3. **Python 3** with required packages

## Option 1: Using Python Script (Recommended)

### Step 1: Install Python Dependencies

```bash
pip3 install psycopg2-binary faker bcrypt
```

Or:
```bash
python3 -m pip install psycopg2-binary faker bcrypt
```

### Step 2: Start Your Services First

The tables need to be created before seeding. Start at least:
- User Service (creates `users` table)
- Product Service (creates `products` table)
- Inventory Service (creates `inventory` table)

### Step 3: Run the Seed Script

```bash
cd microservices
python3 seed_data.py
```

The script will:
- Insert 10,000 users with realistic data
- Insert 10,000 products across 15 categories
- Create inventory records for all products
- Display a summary of inserted data

## Option 2: Using SQL Script

### Step 1: Start Your Services

Same as above - tables must exist.

### Step 2: Run SQL Script

```bash
PGPASSWORD=ujju psql -h localhost -p 5432 -U ujjawalkumar -d postgres -f seed-data.sql
```

## What Gets Created

### Users (10,000 records)
- Username: user1, user2, ..., user10000
- Email: user1@example.com, user2@example.com, etc.
- Password: "password" (hashed with BCrypt)
- Realistic names, addresses, and phone numbers
- 95% regular users, 5% admins

### Products (10,000 records)
- Products across 15 categories:
  - Electronics
  - Clothing
  - Books
  - Home & Garden
  - Sports & Outdoors
  - Toys & Games
  - Health & Beauty
  - Automotive
  - Food & Beverages
  - Pet Supplies
  - Office Supplies
  - Jewelry
  - Musical Instruments
  - Baby Products
  - Fashion Accessories
- Prices: $19.99 to $1000.00
- Stock quantities: 10 to 510 units
- Realistic descriptions
- Image URLs from Picsum

### Inventory
- One inventory record per product
- Reserved quantities: 0 to 9 units

## Verification

After seeding, verify the data:

```bash
psql -h localhost -p 5432 -U ujjawalkumar -d postgres

# Then run:
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM products;
SELECT COUNT(*) FROM inventory;

# View sample data:
SELECT * FROM users LIMIT 5;
SELECT * FROM products LIMIT 5;
```

## Troubleshooting

### Tables Don't Exist
**Error**: `relation "users" does not exist`

**Solution**: Start your microservices first. Tables are created automatically when services start.

```bash
# Start user-service
cd microservices/user-service
mvn spring-boot:run

# Start product-service  
cd microservices/product-service
mvn spring-boot:run
```

### Python Packages Not Found
**Error**: `ModuleNotFoundError: No module named 'psycopg2'`

**Solution**: Install required packages:
```bash
pip3 install psycopg2-binary faker bcrypt
```

### Connection Refused
**Error**: `could not connect to server`

**Solution**: Ensure PostgreSQL is running:
```bash
# Check if PostgreSQL is running
psql -U ujjawalkumar -d postgres

# Or start it:
brew services start postgresql@14  # macOS
```

### Duplicate Key Errors
**Error**: `duplicate key value violates unique constraint`

**Solution**: The script uses `ON CONFLICT DO NOTHING`, so this shouldn't happen. If it does, clear existing data first:
```sql
TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE products CASCADE;
TRUNCATE TABLE inventory CASCADE;
```

## Performance

- The Python script inserts data in batches of 100 records
- 10,000 users: ~30-60 seconds
- 10,000 products: ~60-120 seconds
- Total time: ~2-3 minutes

For faster insertion, you can increase the batch size in the script.

## Resetting Data

To clear all seeded data:

```sql
TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE products CASCADE;
TRUNCATE TABLE inventory CASCADE;
TRUNCATE TABLE orders CASCADE;
TRUNCATE TABLE order_items CASCADE;
TRUNCATE TABLE payments CASCADE;
```

Then re-run the seed script.

