package com.example.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(database.expenseDao)
        
        // Seed database if empty
        viewModelScope.launch {
            repository.allExpenses.collect { list ->
                if (list.isEmpty()) {
                    seedDatabase()
                }
            }
        }
    }

    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filter states
    val selectedMonth = MutableStateFlow<String>("Todos") // Format YYYY-MM or "Todos"
    val selectedCategory = MutableStateFlow<String>("Todas") // "Todas" or specific category string

    // Map of month name keys and YYYY-MM values for filtering select dropdowns
    val availableMonths: StateFlow<List<Pair<String, String>>> = allExpenses
        .combine(selectedMonth) { list, _ ->
            val months = list.map { it.yearAndMonth to "${it.monthName} ${it.yearAndMonth.substring(0, 4)}" }
                .distinctBy { it.first }
                .sortedByDescending { it.first }
            listOf("Todos" to "Todos los meses") + months
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("Todos" to "Todos los meses")
        )

    // Filtered lists
    val filteredExpenses: StateFlow<List<Expense>> = combine(
        allExpenses,
        selectedMonth,
        selectedCategory
    ) { list, month, category ->
        list.filter { expense ->
            val monthMatch = month == "Todos" || expense.yearAndMonth == month
            val categoryMatch = category == "Todas" || expense.category == category
            monthMatch && categoryMatch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Summary calculations for selected filter
    val currentSummary: StateFlow<SummaryData> = filteredExpenses.mapSummary()

    // Grouped by Category totals for donut / pie chart
    val categoryBreakdown: StateFlow<List<CategoryBreakdown>> = filteredExpenses
        .combine(selectedMonth) { list, _ ->
            val expensesOnly = list.filter { !it.isIncome }
            val total = expensesOnly.sumOf { it.amount }
            if (total == 0.0) return@combine emptyList<CategoryBreakdown>()

            expensesOnly.groupBy { it.category }
                .map { (category, items) ->
                    val sum = items.sumOf { it.amount }
                    CategoryBreakdown(
                        category = category,
                        amount = sum,
                        percentage = if (total > 0) (sum / total * 100).toFloat() else 0f
                    )
                }
                .sortedByDescending { it.amount }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Monthly historical trends for the detailed bar chart
    val monthlyTrends: StateFlow<List<MonthlyTrend>> = allExpenses
        .combine(selectedMonth) { list, _ ->
            val grouped = list.groupBy { it.yearAndMonth }
            grouped.map { (yearMonth, items) ->
                val income = items.filter { it.isIncome }.sumOf { it.amount }
                val expense = items.filter { !it.isIncome }.sumOf { it.amount }
                val dateCal = Calendar.getInstance()
                if (items.isNotEmpty()) {
                    dateCal.timeInMillis = items.first().dateMillis
                }
                val label = "${getShortMonthName(dateCal.get(Calendar.MONTH))} ${yearMonth.substring(2, 4)}"
                MonthlyTrend(
                    yearMonth = yearMonth,
                    label = label,
                    income = income,
                    expenses = expense,
                    net = income - expense
                )
            }.sortedBy { it.yearMonth }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // DB Operations
    fun addExpense(
        description: String,
        amount: Double,
        category: String,
        dateMillis: Long,
        isIncome: Boolean
    ) {
        viewModelScope.launch {
            repository.insert(
                Expense(
                    description = description.trim(),
                    amount = amount,
                    category = category,
                    dateMillis = dateMillis,
                    isIncome = isIncome
                )
            )
        }
    }

    fun updateExpense(
        id: Int,
        description: String,
        amount: Double,
        category: String,
        dateMillis: Long,
        isIncome: Boolean
    ) {
        viewModelScope.launch {
            repository.update(
                Expense(
                    id = id,
                    description = description.trim(),
                    amount = amount,
                    category = category,
                    dateMillis = dateMillis,
                    isIncome = isIncome
                )
            )
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.delete(expense)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
            // It will trigger seedDatabase due to empty collected list
        }
    }

    private fun StateFlow<List<Expense>>.mapSummary(): StateFlow<SummaryData> {
        return this.map { list ->
            var income = 0.0
            var expenses = 0.0
            list.forEach {
                if (it.isIncome) {
                    income += it.amount
                } else {
                    expenses += it.amount
                }
            }
            SummaryData(
                totalIncome = income,
                totalExpenses = expenses,
                balance = income - expenses
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SummaryData(0.0, 0.0, 0.0)
        )
    }

    private suspend fun seedDatabase() {
        val cal = Calendar.getInstance()
        val seedList = mutableListOf<Expense>()

        // Helper to get relative millis
        fun getRelativeTimeMillis(monthsAgo: Int, dayOfMonth: Int, hour: Int): Long {
            val c = Calendar.getInstance()
            c.add(Calendar.MONTH, -monthsAgo)
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            c.set(Calendar.HOUR_OF_DAY, hour)
            c.set(Calendar.MINUTE, 0)
            return c.timeInMillis
        }

        // --- SEEDING 2 MONTHS AGO (April 2026) ---
        seedList.add(Expense(description = "Nómina Mensual Abril", amount = 1850.0, category = "Ingresos", dateMillis = getRelativeTimeMillis(2, 1, 9), isIncome = true))
        seedList.add(Expense(description = "Ingreso Extra freelance", amount = 320.0, category = "Ingresos", dateMillis = getRelativeTimeMillis(2, 15, 14), isIncome = true))
        
        seedList.add(Expense(description = "Alquiler Piso", amount = 650.0, category = "Vivienda", dateMillis = getRelativeTimeMillis(2, 1, 10), isIncome = false))
        seedList.add(Expense(description = "Compra Semanal Mercadona", amount = 112.50, category = "Alimentación", dateMillis = getRelativeTimeMillis(2, 3, 12), isIncome = false))
        seedList.add(Expense(description = "Cena Restaurante Italiano", amount = 54.0, category = "Ocio y Entretenimiento", dateMillis = getRelativeTimeMillis(2, 5, 21), isIncome = false))
        seedList.add(Expense(description = "Gasolina Repsol", amount = 65.0, category = "Transporte", dateMillis = getRelativeTimeMillis(2, 10, 15), isIncome = false))
        seedList.add(Expense(description = "Suscripción Netflix", amount = 17.99, category = "Ocio y Entretenimiento", dateMillis = getRelativeTimeMillis(2, 12, 8), isIncome = false))
        seedList.add(Expense(description = "Consulta Dentista", amount = 90.0, category = "Salud", dateMillis = getRelativeTimeMillis(2, 18, 16), isIncome = false))
        seedList.add(Expense(description = "Compra Semanal Carrefour", amount = 125.40, category = "Alimentación", dateMillis = getRelativeTimeMillis(2, 20, 11), isIncome = false))
        seedList.add(Expense(description = "Factura de Luz Endesa", amount = 85.20, category = "Vivienda", dateMillis = getRelativeTimeMillis(2, 25, 14), isIncome = false))

        // --- SEEDING 1 MONTH AGO (May 2026) ---
        seedList.add(Expense(description = "Nómina Mensual Mayo", amount = 1850.0, category = "Ingresos", dateMillis = getRelativeTimeMillis(1, 1, 9), isIncome = true))
        
        seedList.add(Expense(description = "Alquiler Piso", amount = 650.0, category = "Vivienda", dateMillis = getRelativeTimeMillis(1, 1, 10), isIncome = false))
        seedList.add(Expense(description = "Compra Semanal Mercadona", amount = 95.80, category = "Alimentación", dateMillis = getRelativeTimeMillis(1, 4, 11), isIncome = false))
        seedList.add(Expense(description = "Entradas de Cine y Palomitas", amount = 28.50, category = "Ocio y Entretenimiento", dateMillis = getRelativeTimeMillis(1, 8, 18), isIncome = false))
        seedList.add(Expense(description = "Gasolina Repsol", amount = 60.0, category = "Transporte", dateMillis = getRelativeTimeMillis(1, 11, 17), isIncome = false))
        seedList.add(Expense(description = "Suscripción Netflix", amount = 17.99, category = "Ocio y Entretenimiento", dateMillis = getRelativeTimeMillis(1, 12, 8), isIncome = false))
        seedList.add(Expense(description = "Vacunas Veterinaria", amount = 45.0, category = "Salud", dateMillis = getRelativeTimeMillis(1, 15, 12), isIncome = false))
        seedList.add(Expense(description = "Compra Ropa Zara", amount = 89.90, category = "Otros", dateMillis = getRelativeTimeMillis(1, 19, 15), isIncome = false))
        seedList.add(Expense(description = "Compra Semanal Mercadona", amount = 118.0, category = "Alimentación", dateMillis = getRelativeTimeMillis(1, 21, 13), isIncome = false))
        seedList.add(Expense(description = "Factura Internet Fibra", amount = 42.0, category = "Vivienda", dateMillis = getRelativeTimeMillis(1, 24, 10), isIncome = false))
        seedList.add(Expense(description = "Regalo Cumpleaños Sofia", amount = 50.0, category = "Otros", dateMillis = getRelativeTimeMillis(1, 28, 18), isIncome = false))

        // --- SEEDING CURRENT MONTH (June 2026) ---
        seedList.add(Expense(description = "Nómina Mensual Junio", amount = 1850.0, category = "Ingresos", dateMillis = getRelativeTimeMillis(0, 1, 9), isIncome = true))
        seedList.add(Expense(description = "Alquiler Piso", amount = 650.0, category = "Vivienda", dateMillis = getRelativeTimeMillis(0, 1, 10), isIncome = false))
        seedList.add(Expense(description = "Compra Semanal Lidl", amount = 74.20, category = "Alimentación", dateMillis = getRelativeTimeMillis(0, 2, 11), isIncome = false))
        seedList.add(Expense(description = "Cena Hamburguesería", amount = 35.50, category = "Ocio y Entretenimiento", dateMillis = getRelativeTimeMillis(0, 2, 20), isIncome = false))

        repository.insertAll(seedList)
    }

    private fun getShortMonthName(month: Int): String {
        return when (month) {
            Calendar.JANUARY -> "Ene"
            Calendar.FEBRUARY -> "Feb"
            Calendar.MARCH -> "Mar"
            Calendar.APRIL -> "Abr"
            Calendar.MAY -> "May"
            Calendar.JUNE -> "Jun"
            Calendar.JULY -> "Jul"
            Calendar.AUGUST -> "Ago"
            Calendar.SEPTEMBER -> "Sep"
            Calendar.OCTOBER -> "Oct"
            Calendar.NOVEMBER -> "Nov"
            Calendar.DECEMBER -> "Dic"
            else -> "Otro"
        }
    }
}

data class SummaryData(
    val totalIncome: Double,
    val totalExpenses: Double,
    val balance: Double
)

data class CategoryBreakdown(
    val category: String,
    val amount: Double,
    val percentage: Float
)

data class MonthlyTrend(
    val yearMonth: String,
    val label: String,
    val income: Double,
    val expenses: Double,
    val net: Double
)
