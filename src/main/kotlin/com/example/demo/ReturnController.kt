package com.example.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/")
class ReturnController(
        val pendingTokenService: PendingTokenService,
        val returnOrderService: ReturnOrderService,
        val csvService: CsvService,
) {
    @GetMapping("/test") fun test() = "OK"

    data class PendingTokenRequest(val email: String, val orderId: String)
    @PostMapping("/pending/returns")
    fun generateReturnsToken(@RequestBody pendingTokenRequest: PendingTokenRequest): String {
        return pendingTokenService.generate(pendingTokenRequest.email, pendingTokenRequest.orderId)
    }

    data class ReturnRequest(val token: String)
    @PostMapping("/returns")
    fun returnOrder(@RequestBody returnRequest: ReturnRequest): ReturnOrder {
        return returnOrderService.returnOrder(returnRequest.token)
    }

    @GetMapping("/returns/{id}")
    fun returnOrder(@PathVariable id: String): ReturnOrder {
        return returnOrderService.getReturnOrder(id)
    }

    data class UpdateQCRequest(val status: String)
    @PutMapping("/returns/{returnOrderId}/items/{returnOrderItemId}/qc/status")
    fun updateQCStatus(
            @RequestBody updateQCRequest: UpdateQCRequest,
            @PathVariable returnOrderId: String,
            @PathVariable returnOrderItemId: String
    ) {
        return returnOrderService.updateItemStatus(
                returnOrderId,
                returnOrderItemId,
                updateQCRequest.status
        )
    }

    data class UpdateQuantityRequest(val quantity: Int)
    @PutMapping("/returns/{returnOrderId}/items/{returnOrderItemId}/qc/quantity")
    fun updateQuantity(
            @RequestBody updateQuantityRequest: UpdateQuantityRequest,
            @PathVariable returnOrderId: String,
            @PathVariable returnOrderItemId: String
    ) {
        return returnOrderService.updateItemQuantity(
                returnOrderId,
                returnOrderItemId,
                updateQuantityRequest.quantity
        )
    }

    @GetMapping("/returns/{returnOrderId}/complete")
    fun completeReturnOrder(@PathVariable returnOrderId: String) {
        return returnOrderService.completeReturnOrder(returnOrderId)
    }

    @PostMapping("/upload-order-csv")
    fun uploadCsvFile(@RequestParam("file") file: MultipartFile) {
        csvService.uploadCsvFile(file)
    }
}
