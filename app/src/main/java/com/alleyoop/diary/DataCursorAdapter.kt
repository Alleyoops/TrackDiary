package com.alleyoop.diary

import android.content.Context
import android.content.res.Configuration
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.cursoradapter.widget.CursorAdapter
import com.alleyoop.diary.R
import java.text.DateFormat
import java.util.*

class DataCursorAdapter(context: Context?, c: Cursor?) : CursorAdapter(context, c, 0) {
    private var mDateColumnIndex = cursor.getColumnIndex(ContactDatabase.ContactDatabase.FeedEntry.TIME_BEGIN_COLUMN)
    private val formatter: DateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
    private val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return inflater.inflate(R.layout.list_layout, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        var contact = ""
        if (cursor != null) {
            contact = cursor.getString(
                cursor.getColumnIndexOrThrow(
                    ContactDatabase.ContactDatabase.FeedEntry.NAME_COLUMN
                )
            )
        }

        val listItem = view?.findViewById(R.id.list_item) as TextView
        listItem.text = contact
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = inflater.inflate(
                R.layout.list_layout, parent, false
            )
        }

        cursor.moveToPosition(position)
        val listItemHeader = convertView?.findViewById(R.id.list_item_header) as TextView
        val listItem = convertView.findViewById(R.id.list_item) as TextView
        val listDivider = convertView.findViewById(R.id.list_divider) as View
        val headerDivider = convertView.findViewById(R.id.header_divider) as View
        val entryType = cursor.getString(
            cursor.getColumnIndexOrThrow(
                ContactDatabase.ContactDatabase.FeedEntry.TYPE_COLUMN
            )
        )
        var entryEmoji = if(entryType == "Event") "\uD83D\uDCC5" else "\uD83D\uDC68"
        entryEmoji += "   " + cursor.getString(
            cursor.getColumnIndexOrThrow(
                ContactDatabase.ContactDatabase.FeedEntry.NAME_COLUMN
            )
        )
        listItem.text = entryEmoji

        if (position - 1 >= 0) {
            val currentDate = formatter.format(Date(cursor.getLong(mDateColumnIndex)))
            cursor.moveToPosition(position - 1)
            val previousDate = formatter.format(Date(cursor.getLong(mDateColumnIndex)))
            if (currentDate.equals(previousDate, ignoreCase = true)) {
                listItemHeader.visibility = View.GONE
                if (isNightModeActive(convertView)) {
                    listDivider.visibility = View.GONE
                    listDivider.layoutParams.height = 3
                } else { listDivider.visibility = View.VISIBLE }
            } else {
                listItemHeader.visibility = View.VISIBLE
                listItemHeader.text = currentDate
                if (isNightModeActive(convertView)) {
                    listDivider.visibility = View.VISIBLE
                    headerDivider.visibility = View.VISIBLE
                    listDivider.layoutParams.height = 3
                    headerDivider.layoutParams.height = 3
                } else { listDivider.visibility = View.GONE }
            }
        } else {
            listItemHeader.visibility = View.VISIBLE
            listItemHeader.text = formatter.format(Date(cursor.getLong(mDateColumnIndex)))
            if (isNightModeActive(convertView)) {
                listDivider.visibility = View.VISIBLE
                listDivider.layoutParams.height = 3
                headerDivider.visibility = View.VISIBLE
            } else {
                listDivider.visibility = View.GONE
                headerDivider.visibility = View.GONE
            }
        }
        return convertView
    }

    private fun isNightModeActive(context: View?): Boolean {
        val defaultNightMode = AppCompatDelegate.getDefaultNightMode()
        if (defaultNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            return true
        }
        if (defaultNightMode == AppCompatDelegate.MODE_NIGHT_NO) {
            return false
        }
        val currentNightMode = (context!!.resources.configuration.uiMode
                and Configuration.UI_MODE_NIGHT_MASK)
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> return false
            Configuration.UI_MODE_NIGHT_YES -> return true
            Configuration.UI_MODE_NIGHT_UNDEFINED -> return false
        }
        return false
    }
}