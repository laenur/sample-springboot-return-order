package com.example.demo

import com.github.guepardoapps.kulid.ULID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.repository.findByIdOrNull

@DataJpaTest
class RepositoriesTests
@Autowired
constructor(
                val entityManager: TestEntityManager,
                val pendingTokenRepository: PendingTokenRepository,
                val returnOrderItemRepository: ReturnOrderItemRepository,
                val recordOrderRepository: RecordOrderRepository,
) {

        @Test
        fun `When PendingTokenRepository findByIdOrNull then return PendingToken`() {
                val pendingToken = PendingToken("123", "Order1", "user@email.com")
                entityManager.persist(pendingToken)
                entityManager.flush()
                val found = pendingTokenRepository.findByIdOrNull(pendingToken.token)
                assertThat(found).isEqualTo(pendingToken)
        }

        @Test
        fun `When PendingTokenRepository findByEmailAndOrderId then return PendingToken`() {
                val pendingToken = PendingToken("123", "Order1", "user@email.com")
                entityManager.persist(pendingToken)
                entityManager.flush()
                val found =
                                pendingTokenRepository.findByEmailAndOrderId(
                                                pendingToken.email,
                                                pendingToken.orderId
                                )
                assertThat(found).isEqualTo(pendingToken)
        }

        @Test
        fun `When ReturnOrderItemRepository findByReturnOrderId then return List ReturnOrderItem`() {
                val returnOrder = ReturnOrder(ULID.random(), 90.00, "AWAITING_APPROVAL")
                returnOrder.addReturnOrderItem(
                                ReturnOrderItem(
                                                ULID.random(),
                                                returnOrder.id,
                                                "Order1",
                                                "user@email.com",
                                                false,
                                                1,
                                                10.00,
                                                "blue shirt",
                                                "SHR1",
                                                "AWAITING_APPROVAL"
                                )
                )
                returnOrder.addReturnOrderItem(
                                ReturnOrderItem(
                                                ULID.random(),
                                                returnOrder.id,
                                                "Order1",
                                                "user@email.com",
                                                false,
                                                1,
                                                10.00,
                                                "red shirt",
                                                "SHR2",
                                                "AWAITING_APPROVAL"
                                )
                )
                entityManager.persist(returnOrder)
                entityManager.flush()
                val found = returnOrderItemRepository.findByReturnOrderId(returnOrder.id)
                assertThat(found.size).isEqualTo(returnOrder.returnOrderItems.size)
        }

        @Test
        fun `When ReturnOrderItemRepository findByEmailAndOrderId then return List ReturnOrderItem`() {
                val returnOrder = ReturnOrder(ULID.random(), 90.00, "AWAITING_APPROVAL")
                returnOrder.addReturnOrderItem(
                                ReturnOrderItem(
                                                ULID.random(),
                                                returnOrder.id,
                                                "Order1",
                                                "user@email.com",
                                                false,
                                                1,
                                                10.00,
                                                "blue shirt",
                                                "SHR1",
                                                "AWAITING_APPROVAL"
                                )
                )
                returnOrder.addReturnOrderItem(
                                ReturnOrderItem(
                                                ULID.random(),
                                                returnOrder.id,
                                                "Order1",
                                                "user@email.com",
                                                false,
                                                1,
                                                10.00,
                                                "red shirt",
                                                "SHR2",
                                                "AWAITING_APPROVAL"
                                )
                )
                entityManager.persist(returnOrder)
                entityManager.flush()
                val found =
                                returnOrderItemRepository.findByEmailAndOrderId(
                                                returnOrder.returnOrderItems.elementAt(0).email,
                                                returnOrder.returnOrderItems.elementAt(0).orderId
                                )
                assertThat(found.size).isEqualTo(returnOrder.returnOrderItems.size)
        }

        @Test
        fun `When OrderRepository findByEmailAndOrderId then return List Order`() {
                val orders: List<RecordOrder> =
                                listOf(
                                                RecordOrder(
                                                                ULID.random(),
                                                                "Order1",
                                                                "user@email.com",
                                                                "SHR1",
                                                                1,
                                                                10.00,
                                                                "blue shirt"
                                                ),
                                                RecordOrder(
                                                                ULID.random(),
                                                                "Order1",
                                                                "user@email.com",
                                                                "SHR2",
                                                                1,
                                                                10.00,
                                                                "red shirt"
                                                ),
                                )
                for (i in orders) entityManager.persist(i)
                entityManager.flush()
                val found =
                                recordOrderRepository.findByEmailAndOrderId(
                                                orders.elementAt(0).email,
                                                orders.elementAt(0).orderId
                                )
                assertThat(found.size).isEqualTo(orders.size)
        }
}
