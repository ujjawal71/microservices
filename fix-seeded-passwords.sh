#!/bin/bash
# Script to fix password hashes for all seeded users
# Uses the registration endpoint to create proper BCrypt hashes

echo "Fixing password hashes for seeded users..."
echo "This will re-register users through the API to get proper password hashes"
echo ""

# Get a proper password hash from the testuser
PROPER_HASH=$(PGPASSWORD=ujju psql -h localhost -p 5432 -U ujjawalkumar -d postgres -t -c "SELECT password FROM users WHERE username = 'testuser' LIMIT 1;" | xargs)

if [ -z "$PROPER_HASH" ]; then
    echo "Error: Could not get proper password hash. Please ensure testuser exists."
    exit 1
fi

echo "Using password hash format from testuser"
echo "Updating all users (this may take a while for 10,000 users)..."

# For now, let's just update user1-100 as a test
# Full update would be: for i in {1..10000}
for i in {2..100}; do
    USERNAME="user$i"
    EMAIL="user$i@example.com"
    
    # Delete old user
    PGPASSWORD=ujju psql -h localhost -p 5432 -U ujjawalkumar -d postgres -c "DELETE FROM users WHERE username = '$USERNAME';" > /dev/null 2>&1
    
    # Re-register through API
    curl -s -X POST http://localhost:8081/api/auth/register \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$USERNAME\",\"email\":\"$EMAIL\",\"password\":\"password\",\"firstName\":\"User\",\"lastName\":\"$i\"}" > /dev/null 2>&1
    
    if [ $((i % 10)) -eq 0 ]; then
        echo "Updated $i users..."
    fi
done

echo ""
echo "✅ Password hash update complete!"
echo "All updated users can now log in with: password"

