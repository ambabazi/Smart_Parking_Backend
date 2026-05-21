#!/bin/bash

BASE_URL="http://localhost:8080"
RESULTS_FILE="/tmp/comprehensive_tests.log"

echo "=== Comprehensive Smart Parking API Testing ===" > $RESULTS_FILE
echo "Started: $(date)" >> $RESULTS_FILE
echo "" >> $RESULTS_FILE

# Register a test HOST user
echo "1. Registering HOST user:" >> $RESULTS_FILE
HOST_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test Host",
    "email": "testhost@smartparking.com",
    "phone": "+250700000002",
    "password": "TestPass123!",
    "role": "HOST"
  }')
echo "$HOST_RESPONSE" >> $RESULTS_FILE
HOST_TOKEN=$(echo "$HOST_RESPONSE" | grep -o '"token":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "Host Token: $HOST_TOKEN" >> $RESULTS_FILE
echo "" >> $RESULTS_FILE

# Register a test DRIVER user
echo "2. Registering DRIVER user:" >> $RESULTS_FILE
DRIVER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test Driver",
    "email": "testdriver@smartparking.com",
    "phone": "+250700000001",
    "password": "TestPass123!",
    "role": "DRIVER"
  }')
echo "$DRIVER_RESPONSE" >> $RESULTS_FILE
DRIVER_TOKEN=$(echo "$DRIVER_RESPONSE" | grep -o '"token":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "Driver Token: $DRIVER_TOKEN" >> $RESULTS_FILE
echo "" >> $RESULTS_FILE

# Create a parking space as HOST
echo "3. Creating parking space as HOST:" >> $RESULTS_FILE
PARKING_RESPONSE=$(curl -s -X POST "$BASE_URL/parking-spaces" \
  -H "Authorization: Bearer $HOST_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Parking Zone",
    "address": "Kigali, Rwanda",
    "latitude": -1.9505,
    "longitude": 29.8739,
    "totalSlots": 50,
    "pricePerHour": 2000,
    "description": "Test parking space"
  }')
echo "$PARKING_RESPONSE" >> $RESULTS_FILE
PARKING_ID=$(echo "$PARKING_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Parking ID: $PARKING_ID" >> $RESULTS_FILE
echo "" >> $RESULTS_FILE

# Get parking spaces
echo "4. Getting all parking spaces:" >> $RESULTS_FILE
curl -s -X GET "$BASE_URL/parking-spaces" \
  -H "Accept: application/json" >> $RESULTS_FILE 2>&1
echo "" >> $RESULTS_FILE

# Create a reservation as DRIVER
echo "5. Creating reservation as DRIVER:" >> $RESULTS_FILE
START_TIME=$(date -d "+1 hour" -Iseconds)
END_TIME=$(date -d "+3 hours" -Iseconds)
RESERVATION_RESPONSE=$(curl -s -X POST "$BASE_URL/reservations" \
  -H "Authorization: Bearer $DRIVER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"parkingSpaceId\": $PARKING_ID,
    \"slotCount\": 1,
    \"startTime\": \"$START_TIME\",
    \"endTime\": \"$END_TIME\"
  }")
echo "$RESERVATION_RESPONSE" >> $RESULTS_FILE
RESERVATION_ID=$(echo "$RESERVATION_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Reservation ID: $RESERVATION_ID" >> $RESULTS_FILE
echo "" >> $RESULTS_FILE

# Get my reservations
echo "6. Getting driver's reservations:" >> $RESULTS_FILE
curl -s -X GET "$BASE_URL/reservations/my?page=0&size=10" \
  -H "Authorization: Bearer $DRIVER_TOKEN" \
  -H "Accept: application/json" >> $RESULTS_FILE 2>&1
echo "" >> $RESULTS_FILE

# Get specific reservation
echo "7. Getting specific reservation:" >> $RESULTS_FILE
curl -s -X GET "$BASE_URL/reservations/$RESERVATION_ID" \
  -H "Authorization: Bearer $DRIVER_TOKEN" \
  -H "Accept: application/json" >> $RESULTS_FILE 2>&1
echo "" >> $RESULTS_FILE

echo "" >> $RESULTS_FILE
echo "Completed: $(date)" >> $RESULTS_FILE
echo "=== Test Summary ===" >> $RESULTS_FILE
echo "Created HOST user with token" >> $RESULTS_FILE
echo "Created DRIVER user with token" >> $RESULTS_FILE
echo "Created parking space (ID: $PARKING_ID)" >> $RESULTS_FILE
echo "Created reservation (ID: $RESERVATION_ID)" >> $RESULTS_FILE
echo "" >> $RESULTS_FILE

cat $RESULTS_FILE
