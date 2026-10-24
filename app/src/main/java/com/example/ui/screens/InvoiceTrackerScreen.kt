package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvoiceEntity
import com.example.data.model.TrackerKPIStats
import com.example.ui.InvoiceViewModel
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
fun InvoiceTrackerScreen(
    viewModel: InvoiceViewModel,
    invoices: List<InvoiceEntity>,
    stats: TrackerKPIStats,
    searchQuery: String,
    statusFilter: String
) {
    val context = LocalContext.current
    var invoiceToDelete by remember { mutableStateOf<InvoiceEntity?>(null) }

    invoiceToDelete?.let { inv ->
        AlertDialog(
            onDismissRequest = { invoiceToDelete = null },
            title = { Text("Delete Invoice") },
            text = { Text("Are you sure you want to delete ${inv.invoiceNo} for ${inv.clientName}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteInvoice(inv.id)
                        invoiceToDelete = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(14.dp)
    ) {
        // KPI Analytics Row
        KpiCardsSection(stats = stats)

        Spacer(modifier = Modifier.height(14.dp))

        // Search & Status Filters
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search Invoice #, Client, Bus...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tracker_search_field"),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "Pending", "Paid", "Overdue", "Draft").forEach { status ->
                FilterChip(
                    selected = statusFilter.equals(status, ignoreCase = true),
                    onClick = { viewModel.setStatusFilter(status) },
                    label = { Text(status, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("filter_chip_$status")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Invoices Count Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Invoices (${invoices.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = NavyPrimary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Invoices List
        if (invoices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No invoices found",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(invoices, key = { it.id }) { invoice ->
                    InvoiceTrackerCard(
                        invoice = invoice,
                        onEdit = { viewModel.loadInvoiceForEditing(invoice) },
                        onViewSheet = { viewModel.loadInvoiceForViewing(invoice) },
                        onPrint = { InvoicePdfGenerator.printInvoice(context, invoice) },
                        onShare = { InvoicePdfGenerator.sharePdf(context, invoice) },
                        onDuplicate = { viewModel.duplicateInvoice(invoice) },
                        onDelete = { invoiceToDelete = invoice },
                        onStatusChange = { newStatus ->
                            viewModel.updateInvoiceStatus(invoice.id, newStatus)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiCardsSection(stats: TrackerKPIStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        KpiCard(
            title = "TOTAL INVOICED",
            amount = stats.totalInvoiced,
            countText = "${stats.totalCount} Invoices",
            color = NavyPrimary,
            bg = Color(0xFFF1F5F9),
            icon = Icons.Default.Description
        )
        KpiCard(
            title = "COLLECTED (PAID)",
            amount = stats.totalPaid,
            countText = "${stats.paidCount} Paid",
            color = Color(0xFF059669),
            bg = StatusPaidBg,
            icon = Icons.Default.CheckCircle
        )
        KpiCard(
            title = "PENDING",
            amount = stats.totalPending,
            countText = "${stats.pendingCount} Pending",
            color = Color(0xFFD97706),
            bg = StatusPendingBg,
            icon = Icons.Default.HourglassTop
        )
        KpiCard(
            title = "OVERDUE",
            amount = stats.totalOverdue,
            countText = "${stats.overdueCount} Overdue",
            color = Color(0xFFDC2626),
            bg = StatusOverdueBg,
            icon = Icons.Default.Error
        )
    }
}

@Composable
private fun KpiCard(
    title: String,
    amount: Double,
    countText: String,
    color: Color,
    bg: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.width(170.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = color,
                    letterSpacing = 0.5.sp
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = bg,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "AED ${String.format(Locale.US, "%,.2f", amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                countText,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InvoiceTrackerCard(
    invoice: InvoiceEntity,
    onEdit: () -> Unit,
    onViewSheet: () -> Unit,
    onPrint: () -> Unit,
    onShare: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tracker_card_${invoice.invoiceNo}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Invoice No & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        invoice.invoiceNo,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Clickable Status Badge
                    Box {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (invoice.status) {
                                "Paid" -> StatusPaidBg
                                "Pending" -> StatusPendingBg
                                "Overdue" -> StatusOverdueBg
                                else -> StatusDraftBg
                            },
                            modifier = Modifier.clickable { showStatusMenu = true }
                        ) {
                            Text(
                                "${invoice.status} ▾",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (invoice.status) {
                                    "Paid" -> StatusPaidText
                                    "Pending" -> StatusPendingText
                                    "Overdue" -> StatusOverdueText
                                    else -> StatusDraftText
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false }
                        ) {
                            listOf("Pending", "Paid", "Overdue", "Draft").forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        onStatusChange(s)
                                        showStatusMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Overflow Menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More actions")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            text = { Text("Edit in Generator") },
                            onClick = {
                                onEdit()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                            text = { Text("View A4 Sheet") },
                            onClick = {
                                onViewSheet()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Print, contentDescription = null) },
                            text = { Text("Print / Save as PDF") },
                            onClick = {
                                onPrint()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            text = { Text("Share PDF") },
                            onClick = {
                                onShare()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            text = { Text("Duplicate Invoice") },
                            onClick = {
                                onDuplicate()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                            text = { Text("Delete", color = Color.Red) },
                            onClick = {
                                onDelete()
                                showMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Client Name
            Text(
                invoice.clientName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Date row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Date: ${invoice.invoiceDate} • Due: ${invoice.dueDate}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom row: Amount & Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "AED ${String.format(Locale.US, "%,.2f", invoice.totalDue)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldAccent
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onViewSheet) {
                        Text("View A4", fontSize = 11.sp)
                    }
                    TextButton(onClick = onEdit) {
                        Text("Edit", fontSize = 11.sp)
                    }
                    IconButton(onClick = onPrint) {
                        Icon(Icons.Default.Print, contentDescription = "Print", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
