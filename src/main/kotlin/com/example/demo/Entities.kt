package com.example.demo

import javax.persistence.*

@Entity
class PendingToken(
                @Id var token: String,
                var orderId: String,
                var email: String,
)

@Entity
class ReturnOrder(@Id var id: String, var amount: Double, var status: String) {
        @OneToMany(mappedBy = "returnOrder")
        private var _returnOrderItems = mutableListOf<ReturnOrderItem>()

        val returnOrderItems
                get() = _returnOrderItems.toList()

        fun addReturnOrderItem(newItem: ReturnOrderItem) {
                _returnOrderItems += newItem
        }
}

@Entity
class ReturnOrderItem(
                @Id var id: String,
                @ManyToOne var returnOrder: ReturnOrder,
                var orderId: String,
                var email: String,
                var isQualityControlled: Boolean,
                var quantity: Int,
                var price: Double,
                var itemName: String,
                var sku: String,
                var status: String,
)

@Entity
class RecordOrder(
                @Id var id: String,
                var orderId: String,
                var email: String,
                var sku: String,
                var quantity: Int,
                var price: Double,
                var itemName: String,
)
