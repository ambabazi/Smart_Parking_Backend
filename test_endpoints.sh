#!/bin/bash

# Test script for Smart Parking API

BASE_URL="http://localhost:8080"
RESULTS_FILE="/tmp/test_results.log"

echo "=== Smart Parking API Testing ===" > $RESULTS_FILE
echo "Started: $(date)" >> $RESULTS_FILE
echo "" >> $RESULTS_FILE

# Test 1: Health check
echo "1. Testing Health Check:" >> $RESULTS_FILE
curl -s -X GET "$BASE_URL/api/health" -H "Accept: application/json" >> $RESULTS_FILE 2>&1
echo "" >> $RESULTS_FILE

# Test 2: Register a test user
echo "2. Testing User Registration:" >> $RESULTS_FILE
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test Driver",
    "email": "testdriver@smartparking.com",
    "phone": "+250700000001",
    "password": "TestPass123!",
    "role": "DRIVER"
  }')
echo "$REGISTER_RESPONSE" >> $RESULTS_FILE
echo "" >> $RESULTS_FILE

# Extract token from registration response (basic JSON parsing)
ACCESS_TOKEN=$(echo "$REGISTER_RESPONSE" | grep -o '"token":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "Extracted Token: $ACCESS_TOKEN" >> $RESULTS_FILE
echo "" >> $RESULTS_FILE

# Test 3: Test auth/me endpoint
echo "3. Testing GET /api/auth/me:" >> $RESULTS_FILE
curl -s -X GET "$BASE_URL/api/auth/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Accept: application/json" >> $RESULTS_FILE 2>&1
echo "" >> $RESULTS_FILE

# Test 4: Get all parking spaces
echo "4. Testing GET /parking-spaces:" >> $RESULTS_FILE
curl -s -X GET "$BASE_URL/parking-spaces" \
  -H "Accept: application/json" >> $RESULTS_FILE 2>&1
echo "" >> $RESULTS_FILE

# Test 5: Get parking spaces nearby
echo "5. Testing GET /parking-spaces/nearby:" >> $RESULTS_FILE
curl -s -X GET "$BASE_URL/parking-spaces/nearby?latitude=-1.9505&longitude=29.8739" \
  -H "Accept: application/json" >> $RESULTS_FILE 2>&1
echo "" >> $RESULTS_FILE

# Test 6: Admin dashboard
echo "6. Testing GET /api/admin/dashboard:" >> $RESULTS_FILE
curl -s -X GET "$BASE_URL/api/admin/dashboard" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Accept: application/json" >> $RESULTS_FILE 2>&1
echo "" >> $RESULTS_FILE

echo "" >> $RESULTS_FILE
echo "Completed: $(date)" >> $RESULTS_FILE

cat $RESULTS_FILE
