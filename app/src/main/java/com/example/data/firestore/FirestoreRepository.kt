package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceItemJsonAdapter
import com.example.data.model.ServiceEntity
import com.example.data.model.VehicleEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreRepository(
    private val firestore: FirebaseFirestore,
    private val auth: com.google.firebase.auth.FirebaseAuth = com.google.firebase.Firebase.auth
) {
    // Secondary constructor resolving custom database id as mandated
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        com.google.firebase.Firebase.auth
    )

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google to perform cloud operations.")
    }

    val currentUserId: String?
        get() = auth.currentUser?.uid

    // --- INVOICES ---
    fun observeInvoices(userId: String): Flow<List<InvoiceEntity>> {
        val invoicesRef = firestore.collection("users").document(userId).collection("invoices")
        return invoicesRef.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    try {
                        InvoiceEntity(
                            id = doc.id,
                            invoiceNo = data["invoiceNo"] as? String ?: doc.id,
                            invoiceDate = data["invoiceDate"] as? String ?: "",
                            dueDate = data["dueDate"] as? String ?: "",
                            companyTrn = data["companyTrn"] as? String ?: "100364388700003",
                            clientName = data["clientName"] as? String ?: "",
                            clientAddress = data["clientAddress"] as? String ?: "",
                            clientPoBox = data["clientPoBox"] as? String ?: "",
                            clientPhone = data["clientPhone"] as? String ?: "",
                            clientTrn = data["clientTrn"] as? String ?: "",
                            itemsJson = data["itemsJson"] as? String ?: "[]",
                            subtotal = (data["subtotal"] as? Number)?.toDouble() ?: 0.0,
                            vatAmount = (data["vatAmount"] as? Number)?.toDouble() ?: 0.0,
                            totalDue = (data["totalDue"] as? Number)?.toDouble() ?: 0.0,
                            status = data["status"] as? String ?: "Draft",
                            paymentTerms = data["paymentTerms"] as? String ?: "",
                            notes = data["notes"] as? String ?: "",
                            createdAt = System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        Log.e("FirestoreRepo", "Error mapping invoice: ${e.message}", e)
                        null
                    }
                }
            }
            .catch { e ->
                if (e is Exception) {
                    handleFirestoreError(e, OperationType.LIST, invoicesRef.path)
                }
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun saveInvoice(invoice: InvoiceEntity) = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = firestore.collection("users").document(uid).collection("invoices").document(invoice.id)
        try {
            val payload = hashMapOf<String, Any>(
                "id" to invoice.id,
                "userId" to uid,
                "invoiceNo" to invoice.invoiceNo,
                "invoiceDate" to invoice.invoiceDate,
                "dueDate" to invoice.dueDate,
                "companyTrn" to invoice.companyTrn,
                "clientName" to invoice.clientName,
                "clientAddress" to invoice.clientAddress,
                "clientPoBox" to invoice.clientPoBox,
                "clientPhone" to invoice.clientPhone,
                "clientTrn" to invoice.clientTrn,
                "itemsJson" to invoice.itemsJson,
                "subtotal" to invoice.subtotal,
                "vatAmount" to invoice.vatAmount,
                "totalDue" to invoice.totalDue,
                "status" to invoice.status,
                "paymentTerms" to invoice.paymentTerms,
                "notes" to invoice.notes,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteInvoice(invoiceId: String) = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = firestore.collection("users").document(uid).collection("invoices").document(invoiceId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun updateInvoiceStatus(invoiceId: String, status: String) = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = firestore.collection("users").document(uid).collection("invoices").document(invoiceId)
        try {
            docRef.update(
                mapOf(
                    "status" to status,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- VEHICLES ---
    fun observeVehicles(userId: String): Flow<List<VehicleEntity>> {
        val vehiclesRef = firestore.collection("users").document(userId).collection("vehicles")
        return vehiclesRef.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    try {
                        VehicleEntity(
                            id = doc.id,
                            busNo = data["busNo"] as? String ?: "",
                            plateNo = data["plateNo"] as? String ?: "",
                            makeModel = data["makeModel"] as? String ?: "",
                            seatingCapacity = data["seatingCapacity"] as? String ?: "60-Seater AC Bus",
                            fuelType = data["fuelType"] as? String ?: "Diesel",
                            driverName = data["driverName"] as? String ?: "",
                            status = data["status"] as? String ?: "Active",
                            mulkiyaExpiry = data["mulkiyaExpiry"] as? String ?: "",
                            defaultDailyRate = (data["defaultDailyRate"] as? Number)?.toDouble() ?: 500.0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            .catch { e ->
                if (e is Exception) {
                    handleFirestoreError(e, OperationType.LIST, vehiclesRef.path)
                }
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun saveVehicle(vehicle: VehicleEntity) = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = firestore.collection("users").document(uid).collection("vehicles").document(vehicle.id)
        try {
            val payload = hashMapOf<String, Any>(
                "id" to vehicle.id,
                "userId" to uid,
                "busNo" to vehicle.busNo,
                "plateNo" to vehicle.plateNo,
                "makeModel" to vehicle.makeModel,
                "seatingCapacity" to vehicle.seatingCapacity,
                "fuelType" to vehicle.fuelType,
                "driverName" to vehicle.driverName,
                "status" to vehicle.status,
                "mulkiyaExpiry" to vehicle.mulkiyaExpiry,
                "defaultDailyRate" to vehicle.defaultDailyRate,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteVehicle(vehicleId: String) = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = firestore.collection("users").document(uid).collection("vehicles").document(vehicleId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- SERVICES ---
    fun observeServices(userId: String): Flow<List<ServiceEntity>> {
        val servicesRef = firestore.collection("users").document(userId).collection("services")
        return servicesRef.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    try {
                        ServiceEntity(
                            id = doc.id,
                            code = data["code"] as? String ?: "",
                            category = data["category"] as? String ?: "",
                            name = data["name"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            typicalBusCapacity = data["typicalBusCapacity"] as? String ?: "",
                            rateBasis = data["rateBasis"] as? String ?: "Daily",
                            standardRate = (data["standardRate"] as? Number)?.toDouble() ?: 0.0,
                            inclusions = data["inclusions"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            .catch { e ->
                if (e is Exception) {
                    handleFirestoreError(e, OperationType.LIST, servicesRef.path)
                }
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun saveService(service: ServiceEntity) = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = firestore.collection("users").document(uid).collection("services").document(service.id)
        try {
            val payload = hashMapOf<String, Any>(
                "id" to service.id,
                "userId" to uid,
                "code" to service.code,
                "category" to service.category,
                "name" to service.name,
                "description" to service.description,
                "typicalBusCapacity" to service.typicalBusCapacity,
                "rateBasis" to service.rateBasis,
                "standardRate" to service.standardRate,
                "inclusions" to service.inclusions,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteService(serviceId: String) = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = firestore.collection("users").document(uid).collection("services").document(serviceId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}
