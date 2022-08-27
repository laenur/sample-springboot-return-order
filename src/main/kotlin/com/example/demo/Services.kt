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
        println(items.size)

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
        return returnOrder
    }

    fun getReturnOrder(id: String): ReturnOrder {
        val existedReturnOrder = returnOrderRepository.findById(id)
        return existedReturnOrder.get()
    }

    fun updateItemStatus(returnOrderId: String, returnOrderItemId: String, status: String) {
        val existedReturnOrder = returnOrderRepository.findById(returnOrderId).get()

        val items = existedReturnOrder.returnOrderItems
        var item: ReturnOrderItem? = null

        var everyThingUpdated = true

        for (i in items) {
            if (i.id == returnOrderItemId) {
                item = i
            } else if (i.status == "AWAITING_APPROVAL" && everyThingUpdated) {
                everyThingUpdated = false
            }
        }

        if (item == null) {
            throw Exception("Item not found")
        }

        item.status = status
        if (item.status == "REJECTED") existedReturnOrder.amount -= item.quantity * item.price
        if (everyThingUpdated) existedReturnOrder.status = "COMPLETE"

        returnOrderRepository.save(existedReturnOrder)
        returnOrderItemRepository.save(item)

        return
    }

    fun updateItemQuantity(returnOrderId: String, returnOrderItemId: String, quantity: Int) {
        val existedReturnOrder = returnOrderRepository.findById(returnOrderId).get()

        val items = existedReturnOrder.returnOrderItems
        var item: ReturnOrderItem? = null

        for (i in items) {
            if (i.id == returnOrderItemId) {
                item = i
            }
        }

        if (item == null) {
            throw Exception("Item not found")
        }

        existedReturnOrder.amount -= item.quantity * item.price
        existedReturnOrder.amount += quantity * item.price
        item.quantity = quantity

        returnOrderRepository.save(existedReturnOrder)
        returnOrderItemRepository.save(item)

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
            print(ex)
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
