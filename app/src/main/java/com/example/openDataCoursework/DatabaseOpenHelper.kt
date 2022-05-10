package com.example.openDataCoursework

import android.content.Context
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper

// constructor
class DatabaseOpenHelper(context: Context?) :
    SQLiteAssetHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "crimet.db"
        private const val DATABASE_VERSION = 1
    }
}