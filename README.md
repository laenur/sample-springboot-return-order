# Overview

Sample project of spring boot using kotlin creating REST server for order item returns management.

# Getting Started
1. `./gradlew bootRun

# Curl Collection

## upload csv

`curl -X POST -F 'file=@orders.csv' http://localhost:8080/upload-order-csv`

## generate returns token

`curl -X POST --location "http://localhost:8080/pending/returns" -H "Content-Type: application/json" -d "{ \"orderId\": \"RK-478\", \"email\": \"john@example.com\" }"`

## return order

param:
- token: from `generate returns token`

`curl -X POST --location "http://localhost:8080/returns" -H "Content-Type: application/json" -d "{ \"token\": \"{{token}}\" }" | jq`

## check status

param:
- returnOrderId: from `return order`

`curl -X GET --location "http://localhost:8080/returns/{{returnOrderId}}" -H "Content-Type: application/json" | jq`

## update qc status

param:
- returnOrderId: from `check status`
- returnOrderItemId: from `check status`
- status: `REJECTED` or `ACCEPTED`

`curl -X PUT --location "http://localhost:8080/returns/{{returnOrderId}}/items/{{returnOrderItemId}}/qc/status" -H "Content-Type: application/json" -d "{ \"status\": \"{{status}}\" }"`

## update qc quantity

param:
- returnOrderId: from `check status`
- returnOrderItemId: from `check status`
- quantity: int >= 0
  
`curl -X PUT --location "http://localhost:8080/returns/{{returnOrderId}}/items/{{returnOrderItemId}}/qc/quantity" -H "Content-Type: application/json" -d "{ \"quantity\": {{quantity}} }"`