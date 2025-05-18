package com.example.myapplication

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.Database


object Database_s {
    private var database: Database? = null

    fun getDatabase(context: Context): Database {
        if (database == null) {
            val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context, "order_database.db")
            database = Database(driver)
        }
        return database!!
    }
}