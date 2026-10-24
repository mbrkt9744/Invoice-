package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class InvoiceItem(
    val id: String = UUID.randomUUID().toString(),
    val description: String = "",
    val qty: Double = 1.0,
    val unitPrice: Double = 0.0,
    val busNo: String? = null,
    val goNo: String? = null
) {
    val totalAmount: Double
        get() = qty * unitPrice
}

object InvoiceItemJsonAdapter {
    fun toJson(items: List<InvoiceItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("description", item.description)
            obj.put("qty", item.qty)
            obj.put("unitPrice", item.unitPrice)
            obj.put("busNo", item.busNo ?: "")
            obj.put("goNo", item.goNo ?: "")
            array.put(obj)
        }
        return array.toString()
    }

    fun fromJson(json: String?): List<InvoiceItem> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<InvoiceItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    InvoiceItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        description = obj.optString("description", ""),
                        qty = obj.optDouble("qty", 1.0),
                        unitPrice = obj.optDouble("unitPrice", 0.0),
                        busNo = obj.optString("busNo").takeIf { it.isNotEmpty() },
                        goNo = obj.optString("goNo").takeIf { it.isNotEmpty() }
                    )
                )
            }
        } catch (_: Exception) {
            // Fallback to empty if parse fails
        }
        return list
    }
}

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val invoiceNo: String,
    val invoiceDate: String,
    val dueDate: String,
    val companyTrn: String = "100364388700003",
    val clientName: String,
    val clientAddress: String = "",
    val clientPoBox: String = "",
    val clientPhone: String = "",
    val clientTrn: String = "",
    val itemsJson: String = "[]",
    val subtotal: Double = 0.0,
    val vatAmount: Double = 0.0,
    val totalDue: Double = 0.0,
    val status: String = "Pending", // Draft, Pending, Paid, Overdue
    val paymentTerms: String = "Payment due within 45 days of invoice date.",
    val notes: String = "Thank you for your business! Please contact us at Contact@avarental.ae if you have any questions.",
    val createdAt: Long = System.currentTimeMillis()
) {
    val items: List<InvoiceItem>
        get() = InvoiceItemJsonAdapter.fromJson(itemsJson)
}

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val busNo: String,
    val plateNo: String,
    val makeModel: String,
    val seatingCapacity: String,
    val fuelType: String = "Diesel",
    val driverName: String,
    val status: String = "Active", // On Contract, Active, Standby, Maintenance
    val mulkiyaExpiry: String = "",
    val defaultDailyRate: Double = 500.0
)

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val code: String,
    val category: String,
    val name: String,
    val description: String,
    val typicalBusCapacity: String,
    val rateBasis: String,
    val standardRate: Double,
    val inclusions: String
)

data class TrackerKPIStats(
    val totalInvoiced: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalPending: Double = 0.0,
    val totalOverdue: Double = 0.0,
    val totalCount: Int = 0,
    val paidCount: Int = 0,
    val pendingCount: Int = 0,
    val overdueCount: Int = 0
)
