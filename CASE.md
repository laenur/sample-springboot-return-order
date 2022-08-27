# Case to Test

## Normal Flow

1. generate token. `POST` to `/returns` with body `{ "orderId": "RK-238", "email": "carly@example.com" }`
2. confirm return. `POST` to `/pending/returns` with body `{ "token": "{{token}}" }`
3. check return status. `GET` to `/returns/{{returnOrderId}}`
4. admin accept all items. `PUT` to `/returns/{{returnOrderId}}/items/{{returnOrderItemId}}/qc/status` with body `{ "status": "ACCEPTED" }`
5. admin mark the return as complete. `GET` to `/returns/{{returnOrderId}}/complete`
6. return status completed. `GET` to `/returns/{{returnOrderId}}`

## Partial Quantity. only 1 from 2 quantity of item.

1. generate token. `POST` to `/returns` with body `{ "orderId": "RK-238", "email": "carly@example.com" }`
2. confirm return. `POST` to `/pending/returns` with body `{ "token": "{{token}}" }`
3. check return status. `GET` to `/returns/{{returnOrderId}}`
5. admin update the item quantity to 1. `PUT` to `/returns/{{returnOrderId}}/items/{{returnOrderItemId}}/qc/quantity` with body `{ "quantity": 1 }`
6. admin mark the item as ACCEPTED. `PUT` to `/returns/{{returnOrderId}}/items/{{returnOrderItemId}}/qc/status` with body `{ "status": "ACCEPTED" }`
7. admin mark return as complete. `GET` to `/returns/{{returnOrderId}}/complete`
8. return status completed. `GET` to `/returns/{{returnOrderId}}`

## Partial Items. only 1 from 2 item orders.

1. generate token. `POST` to `/returns` with body `{ "orderId": "RK-238", "email": "carly@example.com" }`
2. confirm return. `POST` to `/pending/returns` with body `{ "token": "{{token}}" }`
3. check return status. `GET` to `/returns/{{returnOrderId}}`
6. admin mark the return item as ACCEPTED. `PUT` to `/returns/{{returnOrderId}}/items/{{returnOrderItemId}}/qc/status` with body `{ "status": "ACCEPTED" }`
5. admin update the non returned item quantity to 0`PUT` to `/returns/{{returnOrderId}}/items/{{returnOrderItemId}}/qc/quantity` with body `{ "quantity": 0 }`
6. admin mark the non returned item as ACCEPTED. `PUT` to `/returns/{{returnOrderId}}/items/{{returnOrderItemId}}/qc/status` with body `{ "status": "ACCEPTED" }`
7. admin mark return as complete. `GET` to `/returns/{{returnOrderId}}/complete`
8. return status completed. `GET` to `/returns/{{returnOrderId}}`