package com.example.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    suspend fun insert(expense: Expense): Long {
        return expenseDao.insert(expense)
    }

    suspend fun insertAll(expenses: List<Expense>) {
        expenseDao.insertAll(expenses)
    }

    suspend fun update(expense: Expense) {
        expenseDao.update(expense)
    }

    suspend fun delete(expense: Expense) {
        expenseDao.delete(expense)
    }

    suspend fun clearAll() {
        expenseDao.deleteAll()
    }
}
