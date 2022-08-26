package com.example.demo
import org.springframework.data.repository.CrudRepository

interface PendingTokenRepository : CrudRepository<PendingToken, String> {
	fun findByEmailAndOrderId(email: String, orderId: String): PendingToken?
    fun findByToken(token: String): PendingToken?
}

interface ReturnOrderRepository : CrudRepository<ReturnOrder, String> {
}

interface ReturnOrderItemRepository : CrudRepository<ReturnOrderItem, String> {
    fun findByReturnOrderId(returnOrderId: String): List<ReturnOrderItem>
    fun findByEmailAndOrderId(email: String, orderId: String): List<ReturnOrderItem>
}

interface OrderRepository : CrudRepository<RecordOrder, String> {
    fun findByEmailAndOrderId(email: String, orderId: String): List<RecordOrder>
}