package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.FeeRecord
import com.example.data.model.Student
import com.example.ui.AppTab
import com.example.ui.SchoolMainViewModel
import com.example.ui.attendance.AttendanceScreen
import com.example.ui.components.SchoolTopBar
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.exams.ExamsResultsScreen
import com.example.ui.fees.CollectFeeDialog
import com.example.ui.fees.CreateInvoiceDialog
import com.example.ui.fees.FeesBillingScreen
import com.example.ui.fees.PrintBillDialog
import com.example.ui.staff.StaffManagementScreen
import com.example.ui.students.AddEditStudentDialog
import com.example.ui.students.StudentsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyPrimary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SchoolAppRoot()
            }
        }
    }
}

@Composable
fun SchoolAppRoot(viewModel: SchoolMainViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    val students by viewModel.students.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog & Sheet States
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<Student?>(null) }

    var invoiceToCollect by remember { mutableStateOf<FeeRecord?>(null) }
    var showCreateInvoiceDialog by remember { mutableStateOf(false) }
    var studentForNewInvoice by remember { mutableStateOf<Student?>(null) }

    var printBillTarget by remember { mutableStateOf<Pair<Student, FeeRecord>?>(null) }

    // Listen for toast/snackbar messages
    LaunchedEffect(Unit) {
        viewModel.toastMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Android Hardware/Gesture Back Navigation support
    BackHandler(enabled = currentTab != AppTab.DASHBOARD) {
        viewModel.selectTab(AppTab.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SchoolTopBar(
                title = currentTab.title,
                subtitle = "Oakridge Academy \u2022 Session 2025-2026"
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.DASHBOARD,
                    onClick = { viewModel.selectTab(AppTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Overview", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_dashboard")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.STUDENTS,
                    onClick = { viewModel.selectTab(AppTab.STUDENTS) },
                    icon = { Icon(Icons.Default.People, contentDescription = "Students") },
                    label = { Text("Students", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_students")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.ATTENDANCE,
                    onClick = { viewModel.selectTab(AppTab.ATTENDANCE) },
                    icon = { Icon(Icons.Default.EventAvailable, contentDescription = "Attendance") },
                    label = { Text("Attendance", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_attendance")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.BILLING,
                    onClick = { viewModel.selectTab(AppTab.BILLING) },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Billing") },
                    label = { Text("Fees & Bills", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_billing")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.EXAMS,
                    onClick = { viewModel.selectTab(AppTab.EXAMS) },
                    icon = { Icon(Icons.Default.Grade, contentDescription = "Exams") },
                    label = { Text("Exams", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_exams")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.STAFF,
                    onClick = { viewModel.selectTab(AppTab.STAFF) },
                    icon = { Icon(Icons.Default.Badge, contentDescription = "Staff") },
                    label = { Text("Staff & HR", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_staff")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateTab = { viewModel.selectTab(it) },
                    onOpenCollectFee = { invoice ->
                        if (invoice != null) {
                            invoiceToCollect = invoice
                        } else {
                            viewModel.selectTab(AppTab.BILLING)
                        }
                    },
                    onOpenAddStudent = { showAddStudentDialog = true },
                    onViewInvoice = { invoice ->
                        val student = students.firstOrNull { it.id == invoice.studentId }
                        if (student != null) {
                            printBillTarget = Pair(student, invoice)
                        }
                    }
                )

                AppTab.STUDENTS -> StudentsScreen(
                    viewModel = viewModel,
                    onOpenAddStudent = { showAddStudentDialog = true },
                    onOpenEditStudent = { student -> studentToEdit = student },
                    onCreateFeeBillForStudent = { student ->
                        studentForNewInvoice = student
                        showCreateInvoiceDialog = true
                    },
                    onCollectPaymentForInvoice = { invoice ->
                        invoiceToCollect = invoice
                    },
                    onPrintBill = { student, invoice ->
                        printBillTarget = Pair(student, invoice)
                    }
                )

                AppTab.ATTENDANCE -> AttendanceScreen(
                    viewModel = viewModel
                )

                AppTab.BILLING -> FeesBillingScreen(
                    viewModel = viewModel,
                    onOpenCreateInvoice = {
                        studentForNewInvoice = null
                        showCreateInvoiceDialog = true
                    },
                    onOpenCollectFee = { invoice ->
                        invoiceToCollect = invoice
                    },
                    onPrintBill = { student, invoice ->
                        printBillTarget = Pair(student, invoice)
                    }
                )

                AppTab.EXAMS -> ExamsResultsScreen(
                    viewModel = viewModel
                )

                AppTab.STAFF -> StaffManagementScreen(
                    viewModel = viewModel
                )
            }
        }
    }

    // --- Dialogs & Overlays ---

    // 1. Add / Edit Student Dialog
    if (showAddStudentDialog || studentToEdit != null) {
        AddEditStudentDialog(
            initialStudent = studentToEdit,
            onDismiss = {
                showAddStudentDialog = false
                studentToEdit = null
            },
            onConfirm = { student ->
                viewModel.addOrUpdateStudent(student)
                showAddStudentDialog = false
                studentToEdit = null
            }
        )
    }

    // 2. Create Invoice Dialog
    if (showCreateInvoiceDialog) {
        CreateInvoiceDialog(
            students = students,
            preSelectedStudent = studentForNewInvoice,
            onDismiss = {
                showCreateInvoiceDialog = false
                studentForNewInvoice = null
            },
            onConfirm = { invoice ->
                viewModel.createFeeInvoice(invoice)
                showCreateInvoiceDialog = false
                studentForNewInvoice = null
            }
        )
    }

    // 3. Record Fee Payment Dialog
    invoiceToCollect?.let { invoice ->
        val student = students.firstOrNull { it.id == invoice.studentId }
        CollectFeeDialog(
            invoice = invoice,
            student = student,
            onDismiss = { invoiceToCollect = null },
            onConfirmPayment = { amount, method, remarks ->
                viewModel.recordFeePayment(invoice.id, amount, method, remarks)
                invoiceToCollect = null
            }
        )
    }

    // 4. Print / Share Fee Bill Dialog
    printBillTarget?.let { (student, invoice) ->
        PrintBillDialog(
            student = student,
            invoice = invoice,
            onDismiss = { printBillTarget = null }
        )
    }
}
