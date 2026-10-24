package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppTab
import com.example.ui.GeneratorMode
import com.example.ui.InvoiceViewModel
import com.example.ui.screens.InvoiceGeneratorScreen
import com.example.ui.screens.InvoiceTrackerScreen
import com.example.ui.screens.ServiceCatalogScreen
import com.example.ui.screens.VehicleFleetScreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.util.GoogleSignInHelper
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: InvoiceViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val generatorMode by viewModel.generatorMode.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val allInvoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val filteredInvoices by viewModel.filteredInvoices.collectAsStateWithLifecycle()
    val kpiStats by viewModel.kpiStats.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val allVehicles by viewModel.allVehicles.collectAsStateWithLifecycle()
    val allServices by viewModel.allServices.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Handle back navigation
    BackHandler(enabled = currentTab != AppTab.GENERATOR || generatorMode == GeneratorMode.EXACT_SHEET_PREVIEW) {
        if (currentTab == AppTab.GENERATOR && generatorMode == GeneratorMode.EXACT_SHEET_PREVIEW) {
            viewModel.setGeneratorMode(GeneratorMode.FORM_EDITOR)
        } else {
            viewModel.selectTab(AppTab.GENERATOR)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBarHeader(
                currentUser = currentUser,
                isCloudSyncing = isCloudSyncing,
                onSyncAll = { viewModel.syncAllToCloud() },
                onAuthError = { errorMsg ->
                    viewModel.updateInvoiceNumber(viewModel.formState.value.invoiceNo) // No-op, just trigger
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = NavyDark,
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.GENERATOR,
                    onClick = { viewModel.selectTab(AppTab.GENERATOR) },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Invoice") },
                    label = { Text("Invoice", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = GoldAccent,
                        indicatorColor = NavyPrimary,
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray
                    ),
                    modifier = Modifier.testTag("nav_generator")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.TRACKER,
                    onClick = { viewModel.selectTab(AppTab.TRACKER) },
                    icon = {
                        BadgedBox(badge = {
                            if (allInvoices.isNotEmpty()) {
                                Badge(containerColor = GoldAccent, contentColor = Color.Black) {
                                    Text(allInvoices.size.toString(), fontWeight = FontWeight.Bold)
                                }
                            }
                        }) {
                            Icon(Icons.Default.Analytics, contentDescription = "Tracker")
                        }
                    },
                    label = { Text("Tracker", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = GoldAccent,
                        indicatorColor = NavyPrimary,
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray
                    ),
                    modifier = Modifier.testTag("nav_tracker")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.SERVICES,
                    onClick = { viewModel.selectTab(AppTab.SERVICES) },
                    icon = {
                        BadgedBox(badge = {
                            if (allServices.isNotEmpty()) {
                                Badge(containerColor = NavyPrimary, contentColor = Color.White) {
                                    Text(allServices.size.toString())
                                }
                            }
                        }) {
                            Icon(Icons.Default.MiscellaneousServices, contentDescription = "Services")
                        }
                    },
                    label = { Text("Services", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = GoldAccent,
                        indicatorColor = NavyPrimary,
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray
                    ),
                    modifier = Modifier.testTag("nav_services")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.FLEET,
                    onClick = { viewModel.selectTab(AppTab.FLEET) },
                    icon = {
                        BadgedBox(badge = {
                            if (allVehicles.isNotEmpty()) {
                                Badge(containerColor = NavyPrimary, contentColor = Color.White) {
                                    Text(allVehicles.size.toString())
                                }
                            }
                        }) {
                            Icon(Icons.Default.DirectionsBus, contentDescription = "Fleet")
                        }
                    },
                    label = { Text("Fleet", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = GoldAccent,
                        indicatorColor = NavyPrimary,
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray
                    ),
                    modifier = Modifier.testTag("nav_fleet")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.GENERATOR -> {
                    InvoiceGeneratorScreen(
                        viewModel = viewModel,
                        formState = formState,
                        generatorMode = generatorMode,
                        vehicles = allVehicles,
                        services = allServices
                    )
                }
                AppTab.TRACKER -> {
                    InvoiceTrackerScreen(
                        viewModel = viewModel,
                        invoices = filteredInvoices,
                        stats = kpiStats,
                        searchQuery = searchQuery,
                        statusFilter = statusFilter
                    )
                }
                AppTab.SERVICES -> {
                    ServiceCatalogScreen(
                        viewModel = viewModel,
                        services = allServices
                    )
                }
                AppTab.FLEET -> {
                    VehicleFleetScreen(
                        viewModel = viewModel,
                        vehicles = allVehicles
                    )
                }
            }
        }
    }
}

@Composable
fun TopAppBarHeader(
    currentUser: FirebaseUser?,
    isCloudSyncing: Boolean,
    onSyncAll: () -> Unit,
    onAuthError: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var showUserMenu by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars),
        color = NavyDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldAccent,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "AVA",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "AVA Passengers Transport",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp
                    )
                    Text(
                        "Tax Invoice & Fleet Tracker • TRN 100364388700003",
                        color = Color(0xFF94A3B8),
                        fontSize = 9.sp
                    )
                }
            }

            // Cloud Status & User Auth Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentUser != null) {
                    IconButton(
                        onClick = onSyncAll,
                        enabled = !isCloudSyncing,
                        modifier = Modifier.testTag("cloud_sync_button")
                    ) {
                        if (isCloudSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = GoldAccent,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.CloudDone,
                                contentDescription = "Sync to Firebase Cloud",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Box {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = NavyPrimary,
                            modifier = Modifier
                                .clickable { showUserMenu = true }
                                .testTag("user_profile_chip")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "User",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.CloudSync, contentDescription = null) },
                                text = { Text("Sync All to Cloud") },
                                onClick = {
                                    showUserMenu = false
                                    onSyncAll()
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red) },
                                text = { Text("Sign Out", color = Color.Red) },
                                onClick = {
                                    showUserMenu = false
                                    GoogleSignInHelper.signOut(context, credentialManager, coroutineScope) {}
                                }
                            )
                        }
                    }
                } else {
                    // Sign in with Google Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GoldAccent,
                        modifier = Modifier
                            .clickable(enabled = !isSigningIn) {
                                isSigningIn = true
                                GoogleSignInHelper.signInWithGoogle(
                                    context = context,
                                    credentialManager = credentialManager,
                                    scope = coroutineScope,
                                    onSuccess = { isSigningIn = false },
                                    onError = {
                                        isSigningIn = false
                                        onAuthError(it)
                                    },
                                    onCancelled = { isSigningIn = false }
                                )
                            }
                            .testTag("google_sign_in_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.AutoMirrored.Filled.Login,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Google Sync",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
