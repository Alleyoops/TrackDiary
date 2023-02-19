package com.alleyoop.diary


import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import java.util.*

class MigrationTools {
    private val cal: Calendar = Calendar.getInstance()
    private val feedEntry = ContactDatabase.ContactDatabase.FeedEntry

    fun migrateTo3(dataBase: SQLiteDatabase) {
        val query = "Select * from " + ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME
        val cursor = dataBase.rawQuery(query, null)

        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
                val time = cursor.getLong(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.TIMESTAMP_COLUMN))
                cal.timeInMillis = time
                cal.set(Calendar.HOUR, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)

                val values = ContentValues().apply {
                    put(ContactDatabase.ContactDatabase.FeedEntry.TIMESTAMP_COLUMN, cal.timeInMillis)
                    put(ContactDatabase.ContactDatabase.FeedEntry.DURATION_COLUMN, 60)
                }
                val selection = "_id LIKE ?"
                val selectionArgs = arrayOf(id.toString())
                dataBase.update(ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME, values, selection, selectionArgs)
            }
        }
    }

    fun migrateTo4(dataBase: SQLiteDatabase) {
        val query = "Select * from tmp_table"
        val cursor = dataBase.rawQuery(query, null)

        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
                val time = cursor.getLong(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.TIMESTAMP_COLUMN))
                val duration = cursor.getLong(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.DURATION_COLUMN))
                val hours = duration / 60
                val minutes = duration % 60
                cal.timeInMillis = time
                cal.add(Calendar.HOUR_OF_DAY, hours.toInt())
                cal.add(Calendar.MINUTE, minutes.toInt())

                val values = ContentValues().apply {
                    put(ContactDatabase.ContactDatabase.FeedEntry.TIME_END_COLUMN, cal.timeInMillis)
                }
                val selection = "_id LIKE ?"
                val selectionArgs = arrayOf(id.toString())
                dataBase.update(ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME, values, selection, selectionArgs)
            }
        }
    }

    fun migrateTo5(dataBase: SQLiteDatabase) {
        val query = "Select * from " + ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME
        val cursor = dataBase.rawQuery(query, null)

        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
                val closeContact = cursor.getInt(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.CLOSECONTACT_COLUMN))
                val values = ContentValues().apply {
                    put(
                        ContactDatabase.ContactDatabase.FeedEntry.CLOSECONTACT_COLUMN,
                        when (closeContact) {
                            1 -> 0
                            3 -> 1
                            else -> -1
                        }
                    )
                }
                //更新数据和库
                val selection = "_id LIKE ?"
                val selectionArgs = arrayOf(id.toString())
                dataBase.update(ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME, values, selection, selectionArgs)
            }
        }
    }
}