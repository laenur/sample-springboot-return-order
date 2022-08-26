# Overview

Sample project of spring boot using kotlin creating REST server for order item returns management.

# Getting Started
1. `./gradlew bootRun`

# Curl Collection

- upload csv
`curl -X POST -F 'file=@orders.csv' http://localhost:8080/upload-order-csv`
- generate returns token
`curl -X POST --location "http://localhost:8080/pending/returns" -H "Content-Type: application/json" -d "{ \"orderId\": \"RK-478\", \"email\": \"john@example.com\" }"`

- return order
`curl -X POST --location "http://localhost:8080/returns" -H "Content-Type: application/json" -d "{ \"token\": \"01GBDDSBDRYJGB1S43YEK11B04%\" }"`

- check status
`curl -X GET --location "http://localhost:8080/returns/01GBDE74WJMBD803JH9F1W1J80" -H "Content-Type: application/json" `
