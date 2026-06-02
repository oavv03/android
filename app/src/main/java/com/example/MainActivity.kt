package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.data.Expense
import com.example.data.ExpenseViewModel
import com.example.data.CategoryBreakdown
import com.example.data.MonthlyTrend
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.MonthlyHistoryTrendChart
import com.example.ui.components.getCategoryColor
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ExpenseTrackerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp(viewModel: ExpenseViewModel = viewModel()) {
    val expenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val availableMonths by viewModel.availableMonths.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val summary by viewModel.currentSummary.collectAsStateWithLifecycle()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsStateWithLifecycle()
    val monthlyTrends by viewModel.monthlyTrends.collectAsStateWithLifecycle()

    var showInputDialog by remember { mutableStateOf(false) }
    var chartTabState by remember { mutableStateOf(0) } // 0 = Donut distribution, 1 = Bar trends

    val categories = listOf(
        "Alimentación",
        "Transporte",
        "Vivienda",
        "Ocio y Entretenimiento",
        "Salud",
        "Otros"
    )

    val config = LocalConfiguration.current
    val isTablet = config.screenWidthDp >= 600

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Sleek Custom Header mimicking the HTML design perfectly
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .padding(top = 24.dp), // Top edge-to-edge spacer alignment
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile avatar matching styled account circle badge
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "O", // For Oscar
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text(
                            text = "Hola, Oscar",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Mis Gastos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Notification demo bell and demo data reset
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.clearAllData() },
                        modifier = Modifier.testTag("reset_data_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restablecer datos demo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showInputDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(8.dp)
                    .testTag("add_expense_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir gasto"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            
            // 1. Balance Overview Banner
            SummaryCards(
                income = summary.totalIncome,
                expenses = summary.totalExpenses,
                balance = summary.balance
            )

            // 2. Scrollable Month Filter Chips of the Sleek style
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = "Filtrar por Mes",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableMonths) { (monthKey, monthLabel) ->
                        val isSelected = selectedMonth == monthKey
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    viewModel.selectedMonth.value = monthKey
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("filter_month_${monthKey}")
                        ) {
                            Text(
                                text = monthLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Adaptive layout depending on Screen Width (Large tablets sidebar vs Standard phone stacked)
            if (isTablet) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column: Detailed Charts Card
                    Card(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        ChartsPanel(
                            chartTabState = chartTabState,
                            onTabChange = { chartTabState = it },
                            categoryBreakdown = categoryBreakdown,
                            monthlyTrends = monthlyTrends
                        )
                    }

                    // Right Column: List of Transactions
                    Card(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        TransactionsPanel(
                            expenses = expenses,
                            selectedCategory = selectedCategory,
                            categories = categories,
                            onCategorySelect = { viewModel.selectedCategory.value = it },
                            onDelete = { viewModel.deleteExpense(it) }
                        )
                    }
                }
            } else {
                // Stacked mobile layout (Vertical Layout scrollable layout container)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            ChartsPanel(
                                chartTabState = chartTabState,
                                onTabChange = { chartTabState = it },
                                categoryBreakdown = categoryBreakdown,
                                monthlyTrends = monthlyTrends
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Historial de Transacciones",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }

                    item {
                        // Horizontal Category filters inside the vertical container
                        CategoryFilters(
                            selectedCategory = selectedCategory,
                            categories = categories,
                            onSelect = { viewModel.selectedCategory.value = it }
                        )
                    }

                    if (expenses.isEmpty()) {
                        item {
                            EmptyStatePlaceholder()
                        }
                    } else {
                        items(expenses) { expense ->
                            TransactionRow(
                                expense = expense,
                                onDelete = { viewModel.deleteExpense(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialogue to enter expenses/incomes
    if (showInputDialog) {
        AddExpenseDialog(
            categories = categories,
            onDismiss = { showInputDialog = false },
            onSubmit = { desc, amount, cat, dateMillis, isIncome ->
                viewModel.addExpense(desc, amount, cat, dateMillis, isIncome)
                showInputDialog = false
            }
        )
    }
}

@Composable
fun SummaryCards(
    income: Double,
    expenses: Double,
    balance: Double
) {
    // Elegant Solid Premium Blue or Navy Card with semi-transparent overlay accents
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) Color(0xFF161B22) else Color(0xFF005DB2)
    val cardBorder = if (isDark) Color(0xFF212833) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = if (isDark) BorderStroke(1.dp, cardBorder) else null
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "BALANCE GENERAL",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.75f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%,.2f €", balance),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.testTag("main_net_balance"),
                        fontSize = 32.sp
                    )
                }
                
                // Rounded backdrop-blur-like accent box
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (balance >= 0) Icons.Default.Add else Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Income / Expense columns side-by-side with high-contrast subtle layout rounded containers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Monthly Income block
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(IncomeGreen.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = IncomeGreen,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Ingresos",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "+%,.0f €", income),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }
                }

                // Monthly Expense block
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(ExpenseRed.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "-",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExpenseRed
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Gastos",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "-%,.0f €", expenses),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryFilters(
    selectedCategory: String,
    categories: List<String>,
    onSelect: (String) -> Unit
) {
    val itemsList = listOf("Todas") + categories
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(itemsList) { category ->
            val isSelected = selectedCategory == category
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelect(category) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("filter_cat_${category}")
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ChartsPanel(
    chartTabState: Int,
    onTabChange: (Int) -> Unit,
    categoryBreakdown: List<CategoryBreakdown>,
    monthlyTrends: List<MonthlyTrend>
) {
    Column {
        // Simple internal tab bar configuration
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (chartTabState == 0) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onTabChange(0) }
                    .padding(vertical = 10.dp)
                    .testTag("chart_tab_donut"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Categorías",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (chartTabState == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (chartTabState == 1) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onTabChange(1) }
                    .padding(vertical = 10.dp)
                    .testTag("chart_tab_trend"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Mensual",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (chartTabState == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display targeted chart depending on state
        AnimatedVisibility(
            visible = chartTabState == 0,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CategoryDonutChart(breakdown = categoryBreakdown)
        }

        AnimatedVisibility(
            visible = chartTabState == 1,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            MonthlyHistoryTrendChart(trends = monthlyTrends)
        }
    }
}

@Composable
fun TransactionsPanel(
    expenses: List<Expense>,
    selectedCategory: String,
    categories: List<String>,
    onCategorySelect: (String) -> Unit,
    onDelete: (Expense) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Transacciones",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
        )

        CategoryFilters(
            selectedCategory = selectedCategory,
            categories = categories,
            onSelect = onCategorySelect
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (expenses.isEmpty()) {
            EmptyStatePlaceholder()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(expenses) { expense ->
                    TransactionRow(
                        expense = expense,
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionRow(
    expense: Expense,
    onDelete: (Expense) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("transaction_row_${expense.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle with localized category icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = getCategoryColor(expense.category).copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIconVector(expense.category, expense.isIncome),
                    contentDescription = null,
                    tint = getCategoryColor(expense.category),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Body text description and relative date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${expense.category} • ${formatDate(expense.dateMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Numeric value
            Text(
                text = if (expense.isIncome) {
                    String.format("+%.2f €", expense.amount)
                } else {
                    String.format("-%.2f €", expense.amount)
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (expense.isIncome) IncomeGreen else ExpenseRed
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Quick delete action
            IconButton(
                onClick = { onDelete(expense) },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("delete_transaction_${expense.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar transacción",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStatePlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Sin transacciones",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "No se encontraron registros de gastos que coincidan con la selección activa.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// Helpers
fun getCategoryIconVector(category: String, isIncome: Boolean): ImageVector {
    if (isIncome) return Icons.Default.Add
    return when (category) {
        "Alimentación" -> Icons.Default.ShoppingCart
        "Transporte" -> Icons.Default.LocationOn
        "Vivienda" -> Icons.Default.Home
        "Ocio y Entretenimiento" -> Icons.Default.Star
        "Salud" -> Icons.Default.Favorite
        else -> Icons.Default.Info
    }
}

fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

// Dialog entry form
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onSubmit: (description: String, amount: Double, category: String, dateMillis: Long, isIncome: Boolean) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var isIncome by remember { mutableStateOf(false) } // Default to expense
    
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .testTag("add_expense_form")
            ) {
                Text(
                    text = "Añadir Transacción",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Toggle Selector for Income or Expense structured in beautiful rounded options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (!isIncome) ExpenseRed.copy(alpha = 0.15f) else Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isIncome = false }
                            .border(
                                width = 1.dp,
                                color = if (!isIncome) ExpenseRed else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Text(
                            text = "Gasto",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = if (!isIncome) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isIncome) IncomeGreen.copy(alpha = 0.15f) else Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isIncome = true }
                            .border(
                                width = 1.dp,
                                color = if (isIncome) IncomeGreen else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Text(
                            text = "Ingreso",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = if (isIncome) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Description Input block
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("input_description")
                )

                // Amount numeric field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Cantidad (€)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("input_amount")
                )

                // Category dropdown list (Only display if transaction is Expense)
                if (!isIncome) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Categoría") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            selectedCategory = cat
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Display Error text if validation fails
                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = ExpenseRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                // Submissions buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(text = "Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull()
                            if (description.isBlank()) {
                                errorMsg = "La descripción no puede estar vacía"
							} else if (amount == null || amount <= 0.0) {
                                errorMsg = "Introduce una cantidad válida superior a 0"
                            } else {
                                onSubmit(
                                    description,
                                    amount,
                                    if (isIncome) "Ingresos" else selectedCategory,
                                    System.currentTimeMillis(),
                                    isIncome
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("submit_expense")
                    ) {
                        Text(text = "Añadir", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private val RectangleShapeWorkaround = RoundedCornerShape(0.dp)
