# Admin Login Guide

## Default Admin Credentials

### For Seeded Users:
- **Password for ALL seeded users**: `password`
- **Username**: Depends on which user is set as ADMIN

## How to Find or Create Admin User

### Option 1: Find Existing Admin Users

Connect to PostgreSQL and check:

```sql
-- Find all ADMIN users
SELECT id, username, email, role FROM users WHERE role = 'ADMIN';
```

Then login with:
- **Username**: (any username from the query above)
- **Password**: `password`

### Option 2: Set a Specific User as Admin

```bash
# Connect to PostgreSQL
psql -U ujjawalkumar -d postgres
```

Then run:

```sql
-- Set user1 as admin (recommended)
UPDATE users SET role = 'ADMIN' WHERE username = 'user1';

-- Verify it worked
SELECT id, username, email, role FROM users WHERE username = 'user1';
```

Then login with:
- **Username**: `user1`
- **Password**: `password`

### Option 3: Create New Admin User via Registration

1. Go to http://localhost:3000/register
2. Register a new user
3. Connect to PostgreSQL and set role:

```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'your_new_username';
```

4. Logout and login again

## Quick Admin Setup Commands

### One-liner to set user1 as admin:

```bash
PGPASSWORD=ujju psql -h localhost -p 5432 -U ujjawalkumar -d postgres -c "UPDATE users SET role = 'ADMIN' WHERE username = 'user1';"
```

### Check if admin was set:

```bash
PGPASSWORD=ujju psql -h localhost -p 5432 -U ujjawalkumar -d postgres -c "SELECT id, username, email, role FROM users WHERE username = 'user1';"
```

## Admin Login Steps

1. **Set a user as ADMIN** (if not already):
   ```sql
   UPDATE users SET role = 'ADMIN' WHERE username = 'user1';
   ```

2. **Go to Admin Panel**:
   - Click "Admin Login" in navbar, OR
   - Visit: http://localhost:3000/admin

3. **Click "Admin Login" button** on the admin panel page

4. **Enter credentials**:
   - Username: `user1` (or any admin username)
   - Password: `password`

5. **After login**: You'll be redirected to admin panel if you have ADMIN role

## Troubleshooting

### Issue: "Access Denied. You need ADMIN role"
**Solution**: Set the user's role to ADMIN:
```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';
```
Then logout and login again.

### Issue: "Invalid credentials"
**Solution**: For seeded users, use password: `password`

### Issue: "User not found"
**Solution**: Make sure the user exists:
```sql
SELECT username FROM users WHERE username = 'your_username';
```

## Summary

✅ **Default Password**: `password` (for all seeded users)  
✅ **Admin Setup**: `UPDATE users SET role = 'ADMIN' WHERE username = 'user1';`  
✅ **Login**: Username: `user1`, Password: `password`

