package com.example.demo

import com.github.guepardoapps.kulid.ULID
import com.opencsv.CSVReader
import java.io.BufferedReader
import java.io.InputStreamReader
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class PendingTokenService(
        val pendingTokenRepository: PendingTokenRepository,
        val recordOrderRepository: RecordOrderRepository
) {
    fun generate(email: String, orderId: String): String {
        val existingToken = pendingTokenRepository.findByEmailAndOrderId(email, orderId)
        if (existingToken != null) {
            return existingToken.token
        }

        val items = recordOrderRepository.findByEmailAndOrderId(email, orderId)
        if (items.size == 0) {
            throw Exception("Not found")
        }

        val newToken = PendingToken(ULID.random(), orderId, email)
        pendingTokenRepository.save(newToken)
        return newToken.token
    }
}

@Service
class ReturnOrderService(
        val pendingTokenRepository: PendingTokenRepository,
        val returnOrderRepository: ReturnOrderRepository,
        val returnOrderItemRepository: ReturnOrderItemRepository,
        val recordOrderRepository: RecordOrderRepository
) {
    fun returnOrder(token: String): ReturnOrder {
        val pendingReturn = pendingTokenRepository.findByToken(token)
        if (pendingReturn == null) {
            throw Exception("Token invalid")
        }

        val items =
                recordOrderRepository.findByEmailAndOrderId(
                        pendingReturn.email,
                        pendingReturn.orderId
                )

        val returnOrder = ReturnOrder(ULID.random(), 0.00, "AWAITING_APPROVAL")
        for (item in items) {
            returnOrder.amount += item.price * item.quantity
            returnOrder.addReturnOrderItem(
                ReturnOrderItem(
                    ULID.random(),
                    returnOrder.id,
                    item.orderId,
                    item.email,
                    false,
                    item.quantity,
                    item.price,
                    item.itemName,
                    item.sku,
                    "AWAITING_APPROVAL"
                )
            )
        }

        returnOrderRepository.save(returnOrder)
        pendingTokenRepository.delete(pendingReturn)
        return returnOrder
    }

    fun getReturnOrder(id: String): ReturnOrder {
        val existedReturnOrder = returnOrderRepository.findById(id)
        return existedReturnOrder.get()
    }

    fun updateItemStatus(returnOrderId: String, returnOrderItemId: String, status: String) {
        if (status != "ACCEPTED" && status != "REJECTED") {
            throw Exception("Invalid status: ACCEPTED or REJECTED only")
        }
        val existedReturnOrder = returnOrderRepository.findById(returnOrderId).get()

        val items = existedReturnOrder.returnOrderItems
        var item: ReturnOrderItem? = null

        var amount: Double = 0.00
        for (i in items) {
            if (i.id == returnOrderItemId) {
                item = i
            } else if (i.status == "ACCEPTED" || i.status == "AWAITING_APPROVAL") {
                amount += i.price * i.quantity
            }
        }

        if (item == null) {
            throw Exception("Item not found")
        }

        if (status == "ACCEPTED") {
            val existedReturnOrderItem = returnOrderItemRepository.findByEmailAndOrderId(item.email, item.orderId)
            var alreadyProcessed = false
            for (i in existedReturnOrderItem) {
                if (i.status != "AWAITING_APPROVAL" && i.id == returnOrderItemId) {
                    alreadyProcessed = true
                    break
                }
            }
            if (alreadyProcessed) {
                throw Exception("Item already returned once")
            }
        }

        item.status = status
        if (item.status == "ACCEPTED") amount += item.quantity * item.price
        existedReturnOrder.amount = amount

        returnOrderRepository.save(existedReturnOrder)
        returnOrderItemRepository.save(item)

        return
    }

    fun updateItemQuantity(returnOrderId: String, returnOrderItemId: String, quantity: Int) {
        if (quantity < 0) {
            throw Exception("Invalid quantity")
        }
        val existedReturnOrder = returnOrderRepository.findById(returnOrderId).get()
        if (existedReturnOrder.status == "COMPLETED") {
            throw Exception("This return status already completed")
        }

        val items = existedReturnOrder.returnOrderItems
        var item: ReturnOrderItem? = null

        for (i in items) {
            if (i.id == returnOrderItemId) {
                item = i
                break
            }
        }

        if (item == null) {
            throw Exception("Item not found")
        }

        val originalOrder = recordOrderRepository.findById(item.orderId+item.email+item.sku).get()
        if (quantity > originalOrder.quantity) {
            throw Exception("Quantity not match")
        }

        if (item.status == "ACCEPTED" || item.status == "AWAITING_APPROVAL") {
            existedReturnOrder.amount -= item.quantity * item.price
            existedReturnOrder.amount += quantity * item.price
        }
        item.quantity = quantity

        returnOrderRepository.save(existedReturnOrder)
        returnOrderItemRepository.save(item)

        return
    }

    fun completeReturnOrder(returnOrderId: String) {
        val existedReturnOrder = returnOrderRepository.findById(returnOrderId).get()
        if (existedReturnOrder.status == "COMPLETED") {
            throw Exception("This return status already completed")
        }

        val items = existedReturnOrder.returnOrderItems
        var everyThingUpdated = true
        for (i in items) {
            if (i.status == "AWAITING_APPROVAL") {
                everyThingUpdated = false
                break
            }
        }

        if (!everyThingUpdated) {
            throw Exception("Some item not yet reviewed")
        }
        existedReturnOrder.status = "COMPLETED"
        returnOrderRepository.save(existedReturnOrder)
        return
    }
}

@Service
class CsvService(val recordOrderRepository: RecordOrderRepository) {

    fun uploadCsvFile(file: MultipartFile) {
        throwIfFileEmpty(file)
        var fileReader: BufferedReader? = null

        try {
            fileReader = BufferedReader(InputStreamReader(file.inputStream))
            val reader = CSVReader(fileReader)

            reader.use { r ->
                var line = r.readNext()
                while (line != null) {
                    recordOrderRepository.save(
                            RecordOrder(
                                    line.get(0) + line.get(1) + line.get(2),
                                    line.get(0),
                                    line.get(1),
                                    line.get(2),
                                    line.get(3).toInt(),
                                    line.get(4).toDouble(),
                                    line.get(5),
                            )
                    )
                    line = r.readNext()
                }
            }
        } catch (ex: Exception) {
            throw Exception("Error during csv import")
        } finally {
            closeFileReader(fileReader)
        }
    }

    private fun throwIfFileEmpty(file: MultipartFile) {
        if (file.isEmpty) throw Exception("Empty file")
    }

    private fun closeFileReader(fileReader: BufferedReader?) {
        try {
            fileReader!!.close()
        } catch (ex: Exception) {
            throw Exception("Error during csv import")
        }
    }
}
