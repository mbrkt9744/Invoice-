package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firestore.FirestoreRepository
import com.example.data.local.AppDatabase
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceItem
import com.example.data.model.InvoiceItemJsonAdapter
import com.example.data.model.ServiceEntity
import com.example.data.model.TrackerKPIStats
import com.example.data.model.VehicleEntity
import com.example.data.repository.InvoiceRepository
import com.example.util.NumberToWords
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class AppTab {
    GENERATOR,
    TRACKER,
    SERVICES,
    FLEET
}

enum class GeneratorMode {
    FORM_EDITOR,
    EXACT_SHEET_PREVIEW
}

data class InvoiceFormState(
    val id: String = UUID.randomUUID().toString(),
    val invoiceNo: String = "INV-02849",
    val invoiceDate: String = "31 Aug 2026",
    val dueDate: String = "15 Oct 2026",
    val companyTrn: String = "100364388700003",
    val clientName: String = "ALUMINIUM & LIGHT INDUSTRIES CO. (ALICO) LLC",
    val clientAddress: String = "Sharjah - United Arab Emirates",
    val clientPoBox: String = "P.O. Box 6011",
    val clientPhone: String = "+971 6 5582244",
    val clientTrn: String = "100016708800003",
    val status: String = "Pending",
    val items: List<InvoiceItem> = emptyList()
) {
    val subtotal: Double
        get() = items.sumOf { it.totalAmount }

    val vatAmount: Double
        get() = subtotal * 0.05

    val totalDue: Double
        get() = subtotal + vatAmount

    val amountInWords: String
        get() = NumberToWords.convertToUaeCurrency(totalDue)

    fun toEntity(): InvoiceEntity {
        return InvoiceEntity(
            id = id,
            invoiceNo = invoiceNo.trim().ifEmpty { "INV-${System.currentTimeMillis() % 100000}" },
            invoiceDate = invoiceDate.trim(),
            dueDate = dueDate.trim(),
            companyTrn = companyTrn.trim(),
            clientName = clientName.trim().ifEmpty { "Cash Client" },
            clientAddress = clientAddress.trim(),
            clientPoBox = clientPoBox.trim(),
            clientPhone = clientPhone.trim(),
            clientTrn = clientTrn.trim(),
            itemsJson = InvoiceItemJsonAdapter.toJson(items),
            subtotal = subtotal,
            vatAmount = vatAmount,
            totalDue = totalDue,
            status = status
        )
    }

    companion object {
        fun fromEntity(entity: InvoiceEntity): InvoiceFormState {
            return InvoiceFormState(
                id = entity.id,
                invoiceNo = entity.invoiceNo,
                invoiceDate = entity.invoiceDate,
                dueDate = entity.dueDate,
                companyTrn = entity.companyTrn,
                clientName = entity.clientName,
                clientAddress = entity.clientAddress,
                clientPoBox = entity.clientPoBox,
                clientPhone = entity.clientPhone,
                clientTrn = entity.clientTrn,
                status = entity.status,
                items = entity.items
            )
        }

        fun defaultTemplate(): InvoiceFormState {
            return InvoiceFormState(
                id = UUID.randomUUID().toString(),
                invoiceNo = "INV-02849",
                invoiceDate = "31 Aug 2026",
                dueDate = "15 Oct 2026",
                companyTrn = "100364388700003",
                clientName = "ALUMINIUM & LIGHT INDUSTRIES CO. (ALICO) LLC",
                clientAddress = "Sharjah - United Arab Emirates",
                clientPoBox = "P.O. Box 6011",
                clientPhone = "+971 6 5582244",
                clientTrn = "100016708800003",
                status = "Pending",
                items = listOf(
                    InvoiceItem(
                        description = "60 Seated AC Bus with driver and fuel from camp to Site Pick and Drop\nBUS: 14377 | GO NO: 219 | FROM 01/09/2026-12/09/2026",
                        qty = 12.0,
                        unitPrice = 416.67,
                        busNo = "14377",
                        goNo = "219"
                    ),
                    InvoiceItem(
                        description = "From Shj Camp - Rak\nFROM 13/09/2026-30/09/2026",
                        qty = 18.0,
                        unitPrice = 500.00
                    ),
                    InvoiceItem(
                        description = "ADDED TRIP: 01,10-Sep 2026 from Uaq camp",
                        qty = 2.0,
                        unitPrice = 240.39
                    ),
                    InvoiceItem(
                        description = "ADDED TRIP: 18 Sep 2026 From Shj Camp - Rak",
                        qty = 1.0,
                        unitPrice = 576.92
                    ),
                    InvoiceItem(
                        description = "ADDED TRIP: 13,20,27-Sep 2026 Sunday",
                        qty = 3.0,
                        unitPrice = 576.92
                    )
                )
            )
        }

        fun blank(): InvoiceFormState {
            val now = Date()
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
            val dateStr = dateFormat.format(now)
            val dueStr = dateFormat.format(Date(now.time + 45L * 24 * 60 * 60 * 1000))
            val randNum = (1000..9999).random()

            return InvoiceFormState(
                id = UUID.randomUUID().toString(),
                invoiceNo = "INV-0$randNum",
                invoiceDate = dateStr,
                dueDate = dueStr,
                companyTrn = "100364388700003",
                clientName = "",
                clientAddress = "",
                clientPoBox = "",
                clientPhone = "",
                clientTrn = "",
                status = "Draft",
                items = listOf(
                    InvoiceItem(description = "", qty = 1.0, unitPrice = 0.0)
                )
            )
        }
    }
}

class InvoiceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InvoiceRepository
    private val firestoreRepository: FirestoreRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = InvoiceRepository(database)
        firestoreRepository = FirestoreRepository(application)
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(Firebase.auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _currentTab = MutableStateFlow(AppTab.GENERATOR)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _generatorMode = MutableStateFlow(GeneratorMode.FORM_EDITOR)
    val generatorMode: StateFlow<GeneratorMode> = _generatorMode.asStateFlow()

    private val _formState = MutableStateFlow(InvoiceFormState.defaultTemplate())
    val formState: StateFlow<InvoiceFormState> = _formState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("ALL")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val allInvoices: StateFlow<List<InvoiceEntity>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVehicles: StateFlow<List<VehicleEntity>> = repository.allVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allServices: StateFlow<List<ServiceEntity>> = repository.allServices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val kpiStats: StateFlow<TrackerKPIStats> = repository.kpiStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TrackerKPIStats())

    val filteredInvoices: StateFlow<List<InvoiceEntity>> = combine(
        allInvoices,
        _searchQuery,
        _statusFilter
    ) { invoices, query, status ->
        invoices.filter { inv ->
            val matchesQuery = query.isBlank() ||
                    inv.invoiceNo.contains(query, ignoreCase = true) ||
                    inv.clientName.contains(query, ignoreCase = true) ||
                    inv.itemsJson.contains(query, ignoreCase = true)

            val matchesStatus = status == "ALL" || inv.status.equals(status, ignoreCase = true)

            matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        Firebase.auth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
            auth.currentUser?.let { user ->
                syncCloudToLocal(user.uid)
            }
        }
        _currentUser.value?.let { user ->
            syncCloudToLocal(user.uid)
        }
    }

    private fun syncCloudToLocal(userId: String) {
        viewModelScope.launch {
            try {
                firestoreRepository.observeInvoices(userId).collect { cloudInvoices ->
                    for (inv in cloudInvoices) {
                        repository.saveInvoice(inv)
                    }
                }
            } catch (e: Exception) {
                Log.e("InvoiceVM", "Error syncing cloud invoices", e)
            }
        }
        viewModelScope.launch {
            try {
                firestoreRepository.observeVehicles(userId).collect { cloudVehicles ->
                    for (v in cloudVehicles) {
                        repository.saveVehicle(v)
                    }
                }
            } catch (e: Exception) {
                Log.e("InvoiceVM", "Error syncing cloud vehicles", e)
            }
        }
        viewModelScope.launch {
            try {
                firestoreRepository.observeServices(userId).collect { cloudServices ->
                    for (s in cloudServices) {
                        repository.saveService(s)
                    }
                }
            } catch (e: Exception) {
                Log.e("InvoiceVM", "Error syncing cloud services", e)
            }
        }
    }

    fun syncAllToCloud() {
        val user = _currentUser.value ?: run {
            _userMessage.value = "Sign in with Google to sync invoices and fleet to Firebase Cloud!"
            return
        }
        viewModelScope.launch {
            _isCloudSyncing.value = true
            try {
                val invoices = allInvoices.value
                val vehicles = allVehicles.value
                val services = allServices.value

                for (inv in invoices) {
                    firestoreRepository.saveInvoice(inv)
                }
                for (v in vehicles) {
                    firestoreRepository.saveVehicle(v)
                }
                for (s in services) {
                    firestoreRepository.saveService(s)
                }
                _userMessage.value = "Synced ${invoices.size} invoices and ${vehicles.size} vehicles to Firebase!"
            } catch (e: Exception) {
                _userMessage.value = "Cloud sync error: ${e.localizedMessage}"
            } finally {
                _isCloudSyncing.value = false
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setGeneratorMode(mode: GeneratorMode) {
        _generatorMode.value = mode
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // Generator Form Updates
    fun updateInvoiceNumber(number: String) {
        _formState.value = _formState.value.copy(invoiceNo = number)
    }

    fun updateInvoiceDate(date: String) {
        _formState.value = _formState.value.copy(invoiceDate = date)
    }

    fun updateDueDate(dueDate: String) {
        _formState.value = _formState.value.copy(dueDate = dueDate)
    }

    fun updateCompanyTrn(trn: String) {
        _formState.value = _formState.value.copy(companyTrn = trn)
    }

    fun updateClientDetails(
        name: String,
        address: String = _formState.value.clientAddress,
        poBox: String = _formState.value.clientPoBox,
        phone: String = _formState.value.clientPhone,
        trn: String = _formState.value.clientTrn
    ) {
        _formState.value = _formState.value.copy(
            clientName = name,
            clientAddress = address,
            clientPoBox = poBox,
            clientPhone = phone,
            clientTrn = trn
        )
    }

    fun updateInvoiceStatus(status: String) {
        _formState.value = _formState.value.copy(status = status)
    }

    fun addLineItem() {
        val currentItems = _formState.value.items.toMutableList()
        currentItems.add(InvoiceItem(description = "", qty = 1.0, unitPrice = 0.0))
        _formState.value = _formState.value.copy(items = currentItems)
    }

    fun updateLineItem(index: Int, description: String, qty: Double, unitPrice: Double) {
        val currentItems = _formState.value.items.toMutableList()
        if (index in currentItems.indices) {
            val existing = currentItems[index]
            currentItems[index] = existing.copy(
                description = description,
                qty = qty,
                unitPrice = unitPrice
            )
            _formState.value = _formState.value.copy(items = currentItems)
        }
    }

    fun removeLineItem(index: Int) {
        val currentItems = _formState.value.items.toMutableList()
        if (index in currentItems.indices) {
            currentItems.removeAt(index)
            if (currentItems.isEmpty()) {
                currentItems.add(InvoiceItem(description = "", qty = 1.0, unitPrice = 0.0))
            }
            _formState.value = _formState.value.copy(items = currentItems)
        }
    }

    fun insertService(service: ServiceEntity) {
        val currentItems = _formState.value.items.toMutableList()
        if (currentItems.isNotEmpty() && currentItems.last().description.isBlank() && currentItems.last().unitPrice == 0.0) {
            val lastIdx = currentItems.lastIndex
            currentItems[lastIdx] = InvoiceItem(
                description = service.description,
                qty = 1.0,
                unitPrice = service.standardRate
            )
        } else {
            currentItems.add(
                InvoiceItem(
                    description = service.description,
                    qty = 1.0,
                    unitPrice = service.standardRate
                )
            )
        }
        _formState.value = _formState.value.copy(items = currentItems)
        _userMessage.value = "Added service: ${service.name}"
    }

    fun attachVehicleToLastItem(vehicle: VehicleEntity) {
        val currentItems = _formState.value.items.toMutableList()
        if (currentItems.isEmpty()) {
            currentItems.add(
                InvoiceItem(
                    description = "BUS: ${vehicle.busNo} (${vehicle.makeModel})\nDRIVER: ${vehicle.driverName}",
                    qty = 1.0,
                    unitPrice = vehicle.defaultDailyRate,
                    busNo = vehicle.busNo
                )
            )
        } else {
            val lastIdx = currentItems.lastIndex
            val item = currentItems[lastIdx]
            val busTag = "BUS: ${vehicle.busNo} (${vehicle.makeModel}) | DRIVER: ${vehicle.driverName}"
            val newDesc = if (item.description.isBlank()) busTag else "${item.description}\n$busTag"
            currentItems[lastIdx] = item.copy(
                description = newDesc,
                busNo = vehicle.busNo,
                unitPrice = if (item.unitPrice == 0.0) vehicle.defaultDailyRate else item.unitPrice
            )
        }
        _formState.value = _formState.value.copy(items = currentItems)
        _userMessage.value = "Attached Bus ${vehicle.busNo} to invoice item"
    }

    fun resetToTemplate() {
        _formState.value = InvoiceFormState.defaultTemplate()
        _userMessage.value = "Reset to AVA standard template (ALICO LLC)"
    }

    fun startBlankInvoice() {
        _formState.value = InvoiceFormState.blank()
        _userMessage.value = "New blank invoice created"
    }

    fun saveCurrentInvoice() {
        viewModelScope.launch {
            val entity = _formState.value.toEntity()
            repository.saveInvoice(entity)
            _currentUser.value?.let {
                try {
                    firestoreRepository.saveInvoice(entity)
                    _userMessage.value = "Invoice ${entity.invoiceNo} saved & synced to Firebase Cloud!"
                    return@launch
                } catch (e: Exception) {
                    Log.e("InvoiceVM", "Cloud save error", e)
                }
            }
            _userMessage.value = "Invoice ${entity.invoiceNo} saved to Tracker!"
        }
    }

    fun loadInvoiceForEditing(invoice: InvoiceEntity) {
        _formState.value = InvoiceFormState.fromEntity(invoice)
        _currentTab.value = AppTab.GENERATOR
        _generatorMode.value = GeneratorMode.FORM_EDITOR
    }

    fun loadInvoiceForViewing(invoice: InvoiceEntity) {
        _formState.value = InvoiceFormState.fromEntity(invoice)
        _currentTab.value = AppTab.GENERATOR
        _generatorMode.value = GeneratorMode.EXACT_SHEET_PREVIEW
    }

    fun updateInvoiceStatus(invoiceId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateInvoiceStatus(invoiceId, newStatus)
            if (_formState.value.id == invoiceId) {
                _formState.value = _formState.value.copy(status = newStatus)
            }
            _currentUser.value?.let {
                try {
                    firestoreRepository.updateInvoiceStatus(invoiceId, newStatus)
                } catch (e: Exception) {
                    Log.e("InvoiceVM", "Cloud update status error", e)
                }
            }
            _userMessage.value = "Status updated to $newStatus"
        }
    }

    fun duplicateInvoice(invoice: InvoiceEntity) {
        val randNum = (1000..9999).random()
        val duplicated = invoice.copy(
            id = UUID.randomUUID().toString(),
            invoiceNo = "INV-0$randNum",
            status = "Draft",
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.saveInvoice(duplicated)
            _currentUser.value?.let {
                try {
                    firestoreRepository.saveInvoice(duplicated)
                } catch (_: Exception) {}
            }
            _userMessage.value = "Created copy ${duplicated.invoiceNo}"
        }
    }

    fun deleteInvoice(invoiceId: String) {
        viewModelScope.launch {
            repository.deleteInvoice(invoiceId)
            _currentUser.value?.let {
                try {
                    firestoreRepository.deleteInvoice(invoiceId)
                } catch (_: Exception) {}
            }
            _userMessage.value = "Invoice deleted"
        }
    }

    // Vehicle CRUD
    fun saveVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch {
            repository.saveVehicle(vehicle)
            _currentUser.value?.let {
                try {
                    firestoreRepository.saveVehicle(vehicle)
                } catch (_: Exception) {}
            }
            _userMessage.value = "Vehicle ${vehicle.busNo} saved"
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicleId)
            _currentUser.value?.let {
                try {
                    firestoreRepository.deleteVehicle(vehicleId)
                } catch (_: Exception) {}
            }
            _userMessage.value = "Vehicle removed from fleet"
        }
    }

    // Service CRUD
    fun saveService(service: ServiceEntity) {
        viewModelScope.launch {
            repository.saveService(service)
            _currentUser.value?.let {
                try {
                    firestoreRepository.saveService(service)
                } catch (_: Exception) {}
            }
            _userMessage.value = "Service ${service.name} saved"
        }
    }

    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            repository.deleteService(serviceId)
            _currentUser.value?.let {
                try {
                    firestoreRepository.deleteService(serviceId)
                } catch (_: Exception) {}
            }
            _userMessage.value = "Service removed"
        }
    }
}
