package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val amount: Double,
    val category: String,
    val dateMillis: Long,
    val isIncome: Boolean = false // false = expense, true = income (allows tracking balance too!)
) {
    // Helper to extract year and month
    val yearAndMonth: String
        get() {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = dateMillis
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-indexed
            // Format as YYYY-MM
            return String.format("%04d-%02d", year, month)
        }

    val monthName: String
        get() {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = dateMillis
            return when (calendar.get(Calendar.MONTH)) {
                Calendar.JANUARY -> "Enero"
                Calendar.FEBRUARY -> "Febrero"
                Calendar.MARCH -> "Marzo"
                Calendar.APRIL -> "Abril"
                Calendar.MAY -> "Mayo"
                Calendar.JUNE -> "Junio"
                Calendar.JULY -> "Julio"
                Calendar.AUGUST -> "Agosto"
                Calendar.SEPTEMBER -> "Septiembre"
                Calendar.OCTOBER -> "Octubre"
                Calendar.NOVEMBER -> "Noviembre"
                Calendar.DECEMBER -> "Diciembre"
                else -> "Otro"
            }
        }
}
