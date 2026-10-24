package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AirportShuttle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceEntity
import com.example.data.model.VehicleEntity
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary
import java.util.Locale

@Composable
fun ServicePickerBottomSheet(
    services: List<ServiceEntity>,
    onSelectService: (ServiceEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MiscellaneousServices,
                    contentDescription = null,
                    tint = NavyPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Select Service Catalog",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(services, key = { it.id }) { service ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectService(service)
                                onDismiss()
                            }
                            .testTag("service_item_${service.code}"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    service.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "AED ${String.format(Locale.US, "%,.2f", service.standardRate)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = GoldAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                service.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        service.rateBasis,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        service.typicalBusCapacity,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun VehiclePickerBottomSheet(
    vehicles: List<VehicleEntity>,
    onSelectVehicle: (VehicleEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DirectionsBus,
                    contentDescription = null,
                    tint = NavyPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Attach Vehicle Fleet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vehicles, key = { it.id }) { vehicle ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectVehicle(vehicle)
                                onDismiss()
                            }
                            .testTag("vehicle_item_${vehicle.busNo}"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Bus ${vehicle.busNo}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "(${vehicle.plateNo})",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (vehicle.status == "On Contract") Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
                                ) {
                                    Text(
                                        vehicle.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = if (vehicle.status == "On Contract") Color(0xFF92400E) else Color(0xFF065F46)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${vehicle.makeModel} • ${vehicle.seatingCapacity}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Driver: ${vehicle.driverName}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "AED ${String.format(Locale.US, "%,.2f", vehicle.defaultDailyRate)}/day",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddEditServiceDialog(
    initialService: ServiceEntity? = null,
    onSave: (ServiceEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var code by remember { mutableStateOf(initialService?.code ?: "SRV-") }
    var category by remember { mutableStateOf(initialService?.category ?: "Labor Transportation") }
    var name by remember { mutableStateOf(initialService?.name ?: "") }
    var description by remember { mutableStateOf(initialService?.description ?: "") }
    var busCapacity by remember { mutableStateOf(initialService?.typicalBusCapacity ?: "60-Seater AC Bus") }
    var rateBasis by remember { mutableStateOf(initialService?.rateBasis ?: "Daily") }
    var rateText by remember { mutableStateOf(initialService?.standardRate?.toString() ?: "500.00") }
    var inclusions by remember { mutableStateOf(initialService?.inclusions ?: "With Driver & Fuel") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialService == null) "Add Transport Service" else "Edit Transport Service")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Service Code (e.g. SRV-CAMP-SITE)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Service Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detailed Description (for Invoice)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        label = { Text("Standard Rate (AED)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = rateBasis,
                        onValueChange = { rateBasis = it },
                        label = { Text("Rate Basis") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = busCapacity,
                    onValueChange = { busCapacity = it },
                    label = { Text("Bus Capacity (e.g. 60-Seater AC)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = inclusions,
                    onValueChange = { inclusions = it },
                    label = { Text("Inclusions (e.g. Driver, Fuel, Salik)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = rateText.toDoubleOrNull() ?: 0.0
                    val entity = ServiceEntity(
                        id = initialService?.id ?: java.util.UUID.randomUUID().toString(),
                        code = code.trim(),
                        category = category.trim(),
                        name = name.trim().ifEmpty { "Transport Service" },
                        description = description.trim(),
                        typicalBusCapacity = busCapacity.trim(),
                        rateBasis = rateBasis.trim(),
                        standardRate = rate,
                        inclusions = inclusions.trim()
                    )
                    onSave(entity)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_service_button")
            ) {
                Text("Save Service")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddEditVehicleDialog(
    initialVehicle: VehicleEntity? = null,
    onSave: (VehicleEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var busNo by remember { mutableStateOf(initialVehicle?.busNo ?: "") }
    var plateNo by remember { mutableStateOf(initialVehicle?.plateNo ?: "") }
    var makeModel by remember { mutableStateOf(initialVehicle?.makeModel ?: "Ashok Leyland Falcon") }
    var capacity by remember { mutableStateOf(initialVehicle?.seatingCapacity ?: "60-Seater AC Bus") }
    var fuelType by remember { mutableStateOf(initialVehicle?.fuelType ?: "Diesel") }
    var driverName by remember { mutableStateOf(initialVehicle?.driverName ?: "") }
    var status by remember { mutableStateOf(initialVehicle?.status ?: "Active") }
    var mulkiyaExpiry by remember { mutableStateOf(initialVehicle?.mulkiyaExpiry ?: "15/03/2027") }
    var rateText by remember { mutableStateOf(initialVehicle?.defaultDailyRate?.toString() ?: "500.00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialVehicle == null) "Add Fleet Vehicle" else "Edit Fleet Vehicle")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = busNo,
                        onValueChange = { busNo = it },
                        label = { Text("Bus No (e.g. 14377)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = plateNo,
                        onValueChange = { plateNo = it },
                        label = { Text("Plate (e.g. 14377 Dubai)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = makeModel,
                    onValueChange = { makeModel = it },
                    label = { Text("Make & Model (e.g. Ashok Leyland Falcon)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text("Capacity (e.g. 60-Seater AC Bus)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = driverName,
                    onValueChange = { driverName = it },
                    label = { Text("Assigned Driver") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        label = { Text("Daily Rate (AED)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mulkiyaExpiry,
                        onValueChange = { mulkiyaExpiry = it },
                        label = { Text("Mulkiya Expiry") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Text("Fleet Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Active", "On Contract", "Standby", "Maintenance").forEach { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(s, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = rateText.toDoubleOrNull() ?: 0.0
                    val entity = VehicleEntity(
                        id = initialVehicle?.id ?: java.util.UUID.randomUUID().toString(),
                        busNo = busNo.trim().ifEmpty { "BUS-${(1000..9999).random()}" },
                        plateNo = plateNo.trim().ifEmpty { busNo },
                        makeModel = makeModel.trim(),
                        seatingCapacity = capacity.trim(),
                        fuelType = fuelType.trim(),
                        driverName = driverName.trim().ifEmpty { "Unassigned" },
                        status = status,
                        mulkiyaExpiry = mulkiyaExpiry.trim(),
                        defaultDailyRate = rate
                    )
                    onSave(entity)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_vehicle_button")
            ) {
                Text("Save Vehicle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
