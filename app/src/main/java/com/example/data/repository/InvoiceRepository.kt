package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.populateInitialData
import com.example.data.model.InvoiceEntity
import com.example.data.model.ServiceEntity
import com.example.data.model.TrackerKPIStats
import com.example.data.model.VehicleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class InvoiceRepository(private val database: AppDatabase) {

    val allInvoices: Flow<List<InvoiceEntity>> = database.invoiceDao().getAllInvoices()
    val allVehicles: Flow<List<VehicleEntity>> = database.vehicleDao().getAllVehicles()
    val allServices: Flow<List<ServiceEntity>> = database.serviceDao().getAllServices()

    val kpiStats: Flow<TrackerKPIStats> = allInvoices.map { invoices ->
        var total = 0.0
        var paid = 0.0
        var pending = 0.0
        var overdue = 0.0
        var paidCount = 0
        var pendingCount = 0
        var overdueCount = 0

        for (inv in invoices) {
            total += inv.totalDue
            when (inv.status) {
                "Paid" -> {
                    paid += inv.totalDue
                    paidCount++
                }
                "Pending" -> {
                    pending += inv.totalDue
                    pendingCount++
                }
                "Overdue" -> {
                    overdue += inv.totalDue
                    overdueCount++
                }
            }
        }

        TrackerKPIStats(
            totalInvoiced = total,
            totalPaid = paid,
            totalPending = pending,
            totalOverdue = overdue,
            totalCount = invoices.size,
            paidCount = paidCount,
            pendingCount = pendingCount,
            overdueCount = overdueCount
        )
    }

    suspend fun getInvoiceById(id: String): InvoiceEntity? = withContext(Dispatchers.IO) {
        database.invoiceDao().getInvoiceById(id)
    }

    suspend fun saveInvoice(invoice: InvoiceEntity) = withContext(Dispatchers.IO) {
        database.invoiceDao().insertOrUpdate(invoice)
    }

    suspend fun deleteInvoice(id: String) = withContext(Dispatchers.IO) {
        database.invoiceDao().deleteById(id)
    }

    suspend fun updateInvoiceStatus(id: String, status: String) = withContext(Dispatchers.IO) {
        database.invoiceDao().updateStatus(id, status)
    }

    suspend fun saveVehicle(vehicle: VehicleEntity) = withContext(Dispatchers.IO) {
        database.vehicleDao().insertOrUpdate(vehicle)
    }

    suspend fun deleteVehicle(id: String) = withContext(Dispatchers.IO) {
        database.vehicleDao().deleteById(id)
    }

    suspend fun saveService(service: ServiceEntity) = withContext(Dispatchers.IO) {
        database.serviceDao().insertOrUpdate(service)
    }

    suspend fun deleteService(id: String) = withContext(Dispatchers.IO) {
        database.serviceDao().deleteById(id)
    }

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        populateInitialData(database)
    }
}
