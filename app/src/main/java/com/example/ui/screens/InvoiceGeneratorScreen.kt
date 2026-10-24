package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceEntity
import com.example.data.model.VehicleEntity
import com.example.ui.GeneratorMode
import com.example.ui.InvoiceFormState
import com.example.ui.InvoiceViewModel
import com.example.ui.components.ServicePickerBottomSheet
import com.example.ui.components.VehiclePickerBottomSheet
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusDraftBg
import com.example.ui.theme.StatusDraftText
import com.example.ui.theme.StatusOverdueBg
import com.example.ui.theme.StatusOverdueText
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPaidText
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusPendingText
import com.example.util.InvoicePdfGenerator
import java.util.Locale

@Composable
fun InvoiceGeneratorScreen(
    viewModel: InvoiceViewModel,
    formState: InvoiceFormState,
    generatorMode: GeneratorMode,
    vehicles: List<VehicleEntity>,
    services: List<ServiceEntity>
) {
    val context = LocalContext.current
    var showServicePicker by remember { mutableStateOf(false) }
    var showVehiclePicker by remember { mutableStateOf(false) }

    if (showServicePicker) {
        ServicePickerBottomSheet(
            services = services,
            onSelectService = { service ->
                viewModel.insertService(service)
            },
            onDismiss = { showServicePicker = false }
        )
    }

    if (showVehiclePicker) {
        VehiclePickerBottomSheet(
            vehicles = vehicles,
            onSelectVehicle = { vehicle ->
                viewModel.attachVehicleToLastItem(vehicle)
            },
            onDismiss = { showVehiclePicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Mode Switcher Tab: Form Editor vs A4 Sheet Preview
        TabRow(
            selectedTabIndex = if (generatorMode == GeneratorMode.FORM_EDITOR) 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = NavyPrimary
        ) {
            Tab(
                selected = generatorMode == GeneratorMode.FORM_EDITOR,
                onClick = { viewModel.setGeneratorMode(GeneratorMode.FORM_EDITOR) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text("Form Editor", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                modifier = Modifier.testTag("tab_form_editor")
            )
            Tab(
                selected = generatorMode == GeneratorMode.EXACT_SHEET_PREVIEW,
                onClick = { viewModel.setGeneratorMode(GeneratorMode.EXACT_SHEET_PREVIEW) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text("A4 Sheet Preview", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                modifier = Modifier.testTag("tab_sheet_preview")
            )
        }

        // Quick Action Bar
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.saveCurrentInvoice() },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.testTag("save_to_tracker_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Save to Tracker", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        val entity = formState.toEntity()
                        InvoicePdfGenerator.printInvoice(context, entity)
                    },
                    modifier = Modifier.testTag("print_invoice_button")
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Print / PDF", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        val entity = formState.toEntity()
                        InvoicePdfGenerator.sharePdf(context, entity)
                    },
                    modifier = Modifier.testTag("share_pdf_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Share", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { showServicePicker = true },
                    modifier = Modifier.testTag("add_from_service_button")
                ) {
                    Icon(Icons.Default.MiscellaneousServices, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("+ Service Catalog", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { showVehiclePicker = true },
                    modifier = Modifier.testTag("attach_fleet_button")
                ) {
                    Icon(Icons.Default.DirectionsBus, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("+ Attach Bus", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.resetToTemplate() },
                    modifier = Modifier.testTag("reset_template_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Reset ALICO", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.startBlankInvoice() },
                    modifier = Modifier.testTag("new_blank_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("New Blank", fontSize = 12.sp)
                }
            }
        }

        // Main Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (generatorMode == GeneratorMode.FORM_EDITOR) {
                InvoiceFormEditor(
                    viewModel = viewModel,
                    formState = formState
                )
            } else {
                ExactSheetPreview(
                    formState = formState,
                    onPrint = {
                        InvoicePdfGenerator.printInvoice(context, formState.toEntity())
                    },
                    onShare = {
                        InvoicePdfGenerator.sharePdf(context, formState.toEntity())
                    }
                )
            }
        }
    }
}

@Composable
private fun InvoiceFormEditor(
    viewModel: InvoiceViewModel,
    formState: InvoiceFormState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Meta Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Invoice Metadata",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NavyPrimary
                    )
                    // Status Badge Picker
                    StatusPickerRow(
                        currentStatus = formState.status,
                        onStatusChange = { viewModel.updateInvoiceStatus(it) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = formState.invoiceNo,
                        onValueChange = { viewModel.updateInvoiceNumber(it) },
                        label = { Text("Invoice No.") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_invoice_no"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = formState.companyTrn,
                        onValueChange = { viewModel.updateCompanyTrn(it) },
                        label = { Text("Company TRN") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_company_trn"),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = formState.invoiceDate,
                        onValueChange = { viewModel.updateInvoiceDate(it) },
                        label = { Text("Invoice Date") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_invoice_date"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = formState.dueDate,
                        onValueChange = { viewModel.updateDueDate(it) },
                        label = { Text("Due Date") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_due_date"),
                        singleLine = true
                    )
                }
            }
        }

        // Bill To Client Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "■ BILL TO (Client Information)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NavyPrimary
                    )

                    // Quick load ALICO LLC button
                    TextButton(
                        onClick = {
                            viewModel.updateClientDetails(
                                name = "ALUMINIUM & LIGHT INDUSTRIES CO. (ALICO) LLC",
                                address = "Sharjah - United Arab Emirates",
                                poBox = "P.O. Box 6011",
                                phone = "+971 6 5582244",
                                trn = "100016708800003"
                            )
                        }
                    ) {
                        Text("Prefill ALICO", fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = formState.clientName,
                    onValueChange = { viewModel.updateClientDetails(name = it) },
                    label = { Text("Client Company Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_client_name"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = formState.clientAddress,
                        onValueChange = { viewModel.updateClientDetails(name = formState.clientName, address = it) },
                        label = { Text("Address / City") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = formState.clientPoBox,
                        onValueChange = { viewModel.updateClientDetails(name = formState.clientName, poBox = it) },
                        label = { Text("P.O. Box") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = formState.clientPhone,
                        onValueChange = { viewModel.updateClientDetails(name = formState.clientName, phone = it) },
                        label = { Text("Phone") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = formState.clientTrn,
                        onValueChange = { viewModel.updateClientDetails(name = formState.clientName, trn = it) },
                        label = { Text("Client TRN") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        // Line Items Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Line Items (${formState.items.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NavyPrimary
                    )
                    Button(
                        onClick = { viewModel.addLineItem() },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.testTag("add_line_item_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Add Item", fontSize = 11.sp)
                    }
                }

                formState.items.forEachIndexed { index, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("line_item_card_$index"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Item #${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = NavyPrimary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "AED ${String.format(Locale.US, "%,.2f", item.totalAmount)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = GoldAccent
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeLineItem(index) },
                                        modifier = Modifier.testTag("remove_item_$index")
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove item",
                                            tint = Color.Red.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = item.description,
                                onValueChange = { desc ->
                                    viewModel.updateLineItem(index, desc, item.qty, item.unitPrice)
                                },
                                label = { Text("Trip / Route Description") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = if (item.qty % 1.0 == 0.0) item.qty.toInt().toString() else item.qty.toString(),
                                    onValueChange = { qStr ->
                                        val q = qStr.toDoubleOrNull() ?: 0.0
                                        viewModel.updateLineItem(index, item.description, q, item.unitPrice)
                                    },
                                    label = { Text("Qty (Days/Trips)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = if (item.unitPrice == 0.0) "" else item.unitPrice.toString(),
                                    onValueChange = { pStr ->
                                        val p = pStr.toDoubleOrNull() ?: 0.0
                                        viewModel.updateLineItem(index, item.description, item.qty, p)
                                    },
                                    label = { Text("Unit Price (AED)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }
        }

        // Financial Summary & Amount in Words Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Financial Summary",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NavyPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "AED ${String.format(Locale.US, "%,.2f", formState.subtotal)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("VAT (5%):", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "AED ${String.format(Locale.US, "%,.2f", formState.vatAmount)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outline)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "TOTAL DUE:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = NavyPrimary
                    )
                    Text(
                        "AED ${String.format(Locale.US, "%,.2f", formState.totalDue)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "AMOUNT IN WORDS:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        formState.amountInWords,
                        fontSize = 11.5.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // Bank Details & Payment Terms Reference
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("■ PAYMENT TERMS & BANK DETAILS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                Text(
                    "Payment due within 45 days of invoice date — by ${formState.dueDate}.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Bank: RAKBANK • Account: AVA Passengers Transport by Rental Buses L.L.C",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "IBAN: AE11 0400 0083 7152 7349 901",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun StatusPickerRow(
    currentStatus: String,
    onStatusChange: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("Draft", "Pending", "Paid", "Overdue").forEach { s ->
            val isSelected = currentStatus.equals(s, ignoreCase = true)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (s) {
                    "Paid" -> if (isSelected) Color(0xFF059669) else StatusPaidBg
                    "Pending" -> if (isSelected) Color(0xFFD97706) else StatusPendingBg
                    "Overdue" -> if (isSelected) Color(0xFFDC2626) else StatusOverdueBg
                    else -> if (isSelected) Color(0xFF475569) else StatusDraftBg
                },
                modifier = Modifier.clickable { onStatusChange(s) }
            ) {
                Text(
                    s,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else when (s) {
                        "Paid" -> StatusPaidText
                        "Pending" -> StatusPendingText
                        "Overdue" -> StatusOverdueText
                        else -> StatusDraftText
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun ExactSheetPreview(
    formState: InvoiceFormState,
    onPrint: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // A4 Paper Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "P.O. Box 91977, Dubai – United Arab Emirates",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            "Phone: 00971 4 3439666",
                            fontSize = 10.5.sp,
                            color = Color(0xFF334155)
                        )
                        Text(
                            "Email: Contact@avarental.ae",
                            fontSize = 10.5.sp,
                            color = Color(0xFF2563EB)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "TAX INVOICE",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A),
                            letterSpacing = 1.sp
                        )
                        Text(
                            "فاتورة ضريبية",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Meta Grid: 4 columns with clean borders
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF94A3B8))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MetaCell(title = "INVOICE NO.:", value = formState.invoiceNo, modifier = Modifier.weight(1f))
                        Divider(
                            modifier = Modifier
                                .width(1.dp)
                                .height(44.dp),
                            color = Color(0xFF94A3B8)
                        )
                        MetaCell(title = "INVOICE DATE:", value = formState.invoiceDate, modifier = Modifier.weight(1f))
                        Divider(
                            modifier = Modifier
                                .width(1.dp)
                                .height(44.dp),
                            color = Color(0xFF94A3B8)
                        )
                        MetaCell(title = "TRN:", value = formState.companyTrn, modifier = Modifier.weight(1f))
                        Divider(
                            modifier = Modifier
                                .width(1.dp)
                                .height(44.dp),
                            color = Color(0xFF94A3B8)
                        )
                        MetaCell(title = "DUE DATE:", value = formState.dueDate, modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BILL TO Section
                Text(
                    "■ BILL TO",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp)
                ) {
                    Text(
                        formState.clientName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                    if (formState.clientAddress.isNotBlank()) {
                        Text(
                            formState.clientAddress,
                            fontSize = 11.sp,
                            color = Color(0xFF334155)
                        )
                    }
                    if (formState.clientPoBox.isNotBlank()) {
                        Text(
                            formState.clientPoBox,
                            fontSize = 11.sp,
                            color = Color(0xFF334155)
                        )
                    }
                    Row {
                        if (formState.clientPhone.isNotBlank()) {
                            Text(
                                "Phone: ${formState.clientPhone}   ",
                                fontSize = 11.sp,
                                color = Color(0xFF334155)
                            )
                        }
                        if (formState.clientTrn.isNotBlank()) {
                            Text(
                                "TRN: ${formState.clientTrn}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Line Items Table
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(0xFF0F172A))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9))
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "DESCRIPTION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF334155),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "QTY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF334155),
                                modifier = Modifier.width(45.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "UNIT PRICE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF334155),
                                modifier = Modifier.width(75.dp),
                                textAlign = TextAlign.End
                            )
                            Text(
                                "AMOUNT (AED)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF334155),
                                modifier = Modifier.width(90.dp),
                                textAlign = TextAlign.End
                            )
                        }

                        Divider(color = Color(0xFFCBD5E1), thickness = 1.dp)

                        // Rows
                        formState.items.forEachIndexed { i, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    item.description,
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF0F172A),
                                    lineHeight = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                val qtyStr = if (item.qty % 1.0 == 0.0) item.qty.toInt().toString() else item.qty.toString()
                                Text(
                                    qtyStr,
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.width(45.dp),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    String.format(Locale.US, "%,.2f", item.unitPrice),
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.width(75.dp),
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    String.format(Locale.US, "%,.2f", item.totalAmount),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.width(90.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                            if (i < formState.items.lastIndex) {
                                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount in Words & Totals Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Left: Amount in Words
                    Column(modifier = Modifier.weight(1.3f)) {
                        Text(
                            "AMOUNT IN WORDS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFCBD5E1))
                                .padding(8.dp)
                        ) {
                            Text(
                                formState.amountInWords,
                                fontSize = 10.sp,
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFF334155),
                                lineHeight = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right: Financial Totals
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", fontSize = 10.5.sp, color = Color(0xFF64748B))
                            Text(
                                "AED ${String.format(Locale.US, "%,.2f", formState.subtotal)}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Divider(color = Color(0xFFE2E8F0))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("VAT (5%)", fontSize = 10.5.sp, color = Color(0xFF64748B))
                            Text(
                                "AED ${String.format(Locale.US, "%,.2f", formState.vatAmount)}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Divider(color = Color(0xFF0F172A), thickness = 1.5.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "TOTAL DUE",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                "AED ${String.format(Locale.US, "%,.2f", formState.totalDue)}",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Terms & Bank Details (Two Columns)
                Divider(color = Color(0xFFCBD5E1), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("■ PAYMENT TERMS", fontSize = 10.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            "Payment due within 45 days of invoice date — by ${formState.dueDate}.",
                            fontSize = 9.5.sp,
                            color = Color(0xFF475569)
                        )
                        Text(
                            "Please make checks payable to AVA Passengers Transport by Rental Buses L.L.C.",
                            fontSize = 9.5.sp,
                            color = Color(0xFF475569)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("■ BANK DETAILS", fontSize = 10.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(3.dp))
                        Text("Bank: RAKBANK", fontSize = 9.5.sp, color = Color(0xFF334155))
                        Text("Account: AVA Passengers Transport by Rental Buses L.L.C", fontSize = 9.5.sp, color = Color(0xFF334155))
                        Text(
                            "IBAN: AE11 0400 0083 7152 7349 901",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = Color(0xFFCBD5E1), thickness = 1.dp)
                Spacer(modifier = Modifier.height(6.dp))

                // Notes & Disclaimer
                Text(
                    "■ NOTES: Thank you for your business! Please contact us at Contact@avarental.ae if you have any questions.",
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF64748B),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "This is a computer-generated e-invoice and does not require a signature.",
                    fontSize = 8.5.sp,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Floating Action Row for Print & Share
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPrint,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("Print / Save as PDF")
            }

            OutlinedButton(
                onClick = onShare,
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("Share PDF")
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun MetaCell(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 5.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF64748B),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            value,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center
        )
    }
}
