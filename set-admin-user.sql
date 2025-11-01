-- SQL script to set a user as ADMIN
-- Usage: Update the username to the desired admin user

-- Example: Set user1 as admin
UPDATE users SET role = 'ADMIN' WHERE username = 'user1';

-- Or set by email
-- UPDATE users SET role = 'ADMIN' WHERE email = 'user1@example.com';

-- Or set by ID
-- UPDATE users SET role = 'ADMIN' WHERE id = 1;

-- To verify admin user was set:
-- SELECT id, username, email, role FROM users WHERE role = 'ADMIN';

