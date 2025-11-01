#!/bin/bash

# Script to set the currently logged-in user as ADMIN
# Usage: ./fix-current-user-admin.sh <username>

if [ -z "$1" ]; then
    echo "❌ Error: Please provide your username"
    echo ""
    echo "Usage: ./fix-current-user-admin.sh <username>"
    echo ""
    echo "To find your username:"
    echo "1. Open browser console (F12)"
    echo "2. Type: JSON.parse(localStorage.getItem('user')).username"
    echo "3. Or check the navbar - it shows your username"
    echo ""
    echo "Example: ./fix-current-user-admin.sh user1"
    exit 1
fi

USERNAME=$1

echo "🔧 Setting user '$USERNAME' as ADMIN..."
echo ""

PGPASSWORD=ujju psql -h localhost -p 5432 -U ujjawalkumar -d postgres << EOF
UPDATE users SET role = 'ADMIN' WHERE username = '$USERNAME';

SELECT id, username, email, role 
FROM users 
WHERE username = '$USERNAME';
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ User '$USERNAME' has been set as ADMIN!"
    echo ""
    echo "🔄 Next steps:"
    echo "1. Logout from the website"
    echo "2. Login again with the same credentials"
    echo "3. You should now see the Admin button in navbar"
    echo "4. Access http://localhost:3000/admin"
else
    echo ""
    echo "❌ Error: Failed to update user role"
    echo "Make sure PostgreSQL is running and the user exists"
fi

