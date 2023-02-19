package com.alleyoop.diary


import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.alleyoop.diary.R
import com.alleyoop.diary.databinding.ActivityMainBinding
import com.alleyoop.diary.databinding.ActivityMainInsideBinding
import java.util.*


class MainActivity : AppCompatActivity() {

    private var isFabOpen = false
    private var onlyRisky = false
    private var onlyRecent = true
    private var numDays = 15
    private val feedEntry = ContactDatabase.ContactDatabase.FeedEntry
    private val dbHelper = FeedReaderDbHelper(this)
    private lateinit var binding: ActivityMainBinding
    private lateinit var elements: ActivityMainInsideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        super.onCreate(savedInstanceState)
        when (preferences.getString("theme", "System")) {
            "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "System" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        elements = binding.activityMainInside
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        val notificationHandler = NotificationHandler()
        notificationHandler.scheduleNotification(this)

        onlyRecent = preferences.getBoolean("logNdays", true)
        numDays = preferences.getString("number_of_days", "15")!!.toInt()
        if (onlyRecent) {
            restrictLastDays(numDays)
        }
        onlyRisky = preferences.getBoolean("closecontactonly", false)
        viewData(onlyRisky)

        registerForContextMenu(findViewById(R.id.diarytable))

        elements.diarytable.setOnItemClickListener { _, _, position, _ ->
            val idx = elements.diarytable.adapter.getItemId(position)
            val entry = elements.diarytable.adapter.getItem(position) as Cursor

            when (entry.getString(
                entry.getColumnIndexOrThrow(
                    ContactDatabase.ContactDatabase.FeedEntry.TYPE_COLUMN
                ))) {
                "Contact" -> {
                    val intent = Intent(this@MainActivity, EditContactActivity::class.java)
                    intent.putExtra("entry", idx.toString())
                    startActivity(intent)
                }
                "Event" -> {
                    val intent = Intent(this@MainActivity, EditEventActivity::class.java)
                    intent.putExtra("entry", idx.toString())
                    startActivity(intent)
                }
                else -> {
                    Toast.makeText(this, "Something very wrong has happened", Toast.LENGTH_LONG).show()
                }
            }
        }

        elements.diarytable.emptyView = findViewById(R.id.emptyList)
        elements.fab.setOnClickListener {
            animateFAB()
        }
    }

    override fun onResume() {
        super.onResume()
        if (onlyRecent) { restrictLastDays(numDays) }
        viewData(onlyRisky)
    }

    override fun onBackPressed() {
        if (isFabOpen) {
            collapseFAB()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.popup_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info: AdapterView.AdapterContextMenuInfo = item.menuInfo as AdapterView.AdapterContextMenuInfo
        return when (item.itemId) {
            R.id.popup_select -> {
                elements.diarytable.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
                val itemList: MutableList<Long> = ArrayList()
                elements.diarytable.setMultiChoiceModeListener(
                    object : AbsListView.MultiChoiceModeListener {
                        override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                            actionMode.menuInflater.inflate(R.menu.context_menu, menu)
                            return true
                        }

                        override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                            return false
                        }

                        override fun onActionItemClicked(
                            actionMode: ActionMode,
                            menuItem: MenuItem
                        ): Boolean {
                            when (menuItem.itemId) {
                                R.id.context_delete -> {
                                    itemList.forEach { deleteEntry(it) }
                                    Toast.makeText(
                                        applicationContext,
                                        getString(
                                            if (itemList.size > 1) R.string.entries_deleted
                                            else R.string.entry_deleted
                                        ),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    itemList.clear()
                                    actionMode.finish()
                                    if (onlyRecent) {
                                        restrictLastDays(numDays)
                                    }
                                    viewData(onlyRisky)
                                    return true
                                }
                                R.id.context_duplicate -> {
                                    itemList.forEach { duplicateEntry(it) }
                                    Toast.makeText(
                                        applicationContext,
                                        getString(
                                            if (itemList.size > 1) R.string.entries_duplicated
                                            else R.string.entry_duplicated
                                        ),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    itemList.clear()
                                    actionMode.finish()
                                    if (onlyRecent) {
                                        restrictLastDays(numDays)
                                    }
                                    viewData(onlyRisky)
                                    return true
                                }
                                else -> {
                                    return false
                                }
                            }
                        }
                        override fun onDestroyActionMode(actionMode: ActionMode) {
                            itemList.clear()
                            elements.diarytable.choiceMode = ListView.CHOICE_MODE_SINGLE
                        }

                        override fun onItemCheckedStateChanged(
                            actionMode: ActionMode,
                            i: Int,
                            position: Long,
                            checked: Boolean
                        ) {
                            if (checked) {
                                itemList.add(position)
                                actionMode.title =
                                    itemList.size.toString() + getString(R.string.entries_selected)
                            } else {
                                itemList.remove(position)
                                actionMode.title =
                                    itemList.size.toString() + getString(R.string.entries_selected)
                            }
                        }
                    })
                elements.diarytable.setItemChecked(info.position, true)
                true
            }
            R.id.popup_duplicate -> {
                duplicateEntry(info.id)
                if (onlyRecent) { restrictLastDays(numDays) }
                viewData(onlyRisky)
                true
            }
            R.id.popup_delete -> {
                deleteEntry(info.id)
                Toast.makeText(this, R.string.entry_deleted, Toast.LENGTH_SHORT).show()
                if (onlyRecent) { restrictLastDays(numDays) }
                viewData(onlyRisky)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun expandFAB() {
        val fabOpen = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        val fabTextOpen = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.fab_open
        )
        val rotateForward = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.rotate_forward
        )

        elements.fab.startAnimation(rotateForward)
        elements.fab1.startAnimation(fabOpen)
        elements.fabText1.startAnimation(fabTextOpen)
        elements.fab2.startAnimation(fabOpen)
        elements.fabText2.startAnimation(fabTextOpen)
        elements.fab1.isClickable = true
        elements.fab2.isClickable = true
        isFabOpen = true
    }

    private fun collapseFAB() {
        val fabClose: Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
        val fabTextClose: Animation = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.fab_close
        )
        val rotateBackward: Animation = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.rotate_backward
        )

        elements.fab.startAnimation(rotateBackward)
        elements.fab1.startAnimation(fabClose)
        elements.fabText1.startAnimation(fabTextClose)
        elements.fab2.startAnimation(fabClose)
        elements.fabText2.startAnimation(fabTextClose)
        elements.fab1.isClickable = false
        elements.fab2.isClickable = false
        isFabOpen = false
    }

    private fun animateFAB() {
        if (isFabOpen) {
            collapseFAB()
        } else {
            expandFAB()
        }
    }

    fun addContact(view: View) {
        startActivity(Intent(this@MainActivity, NewContactActivity::class.java))
    }

    fun addEvent(view: View) {
        startActivity(Intent(this@MainActivity, NewEventActivity::class.java))
    }

    fun openSettings(view: View) {
        startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
    }

    private fun viewData(onlyRisky: Boolean) {
        val cursor = dbHelper.viewData(onlyRisky)
        val adapter = DataCursorAdapter(this, cursor)

        elements.diarytable.adapter = adapter
    }

    private fun restrictLastDays(numDays: Int) {
        val db = dbHelper.writableDatabase
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -numDays)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.HOUR, 0)
        val daysAgo = cal.timeInMillis.toString()

        val selection = "DELETE FROM ${ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME} " +
                "WHERE ${ContactDatabase.ContactDatabase.FeedEntry.TIME_BEGIN_COLUMN} <= " + daysAgo
        db.execSQL(selection)
    }

    private fun deleteEntry(id: Long) {
        val db = dbHelper.writableDatabase
        db.delete(ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME, "_id LIKE ?", arrayOf(id.toString()))
    }

    private fun duplicateEntry(id: Long) {
        val db = dbHelper.writableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM ${ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME}" +
                    " WHERE _id=" + id, null
        )
        cursor.moveToFirst()

        val beginTimestamp = cursor.getLong(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.TIME_BEGIN_COLUMN))
        val initCal = Calendar.getInstance()
        val currentDay = initCal.get(Calendar.DAY_OF_YEAR)
        val currentYear = initCal.get(Calendar.YEAR)
        initCal.timeInMillis = beginTimestamp
        initCal.set(Calendar.DAY_OF_YEAR, currentDay)
        initCal.set(Calendar.YEAR, currentYear)

        val endTimestamp = cursor.getLong(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.TIME_END_COLUMN))
        val endCal = Calendar.getInstance()
        endCal.timeInMillis = endTimestamp
        endCal.set(Calendar.DAY_OF_YEAR, currentDay)
        endCal.set(Calendar.YEAR, currentYear)

        val values = ContentValues().apply {
            put(
                ContactDatabase.ContactDatabase.FeedEntry.TYPE_COLUMN,
                cursor.getString(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.TYPE_COLUMN))
            )
            put(
                ContactDatabase.ContactDatabase.FeedEntry.NAME_COLUMN,
                cursor.getString(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.NAME_COLUMN))
            )
            put(
                ContactDatabase.ContactDatabase.FeedEntry.PLACE_COLUMN,
                cursor.getString(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.PLACE_COLUMN))
            )
            put(ContactDatabase.ContactDatabase.FeedEntry.TIME_BEGIN_COLUMN, initCal.timeInMillis)
            put(ContactDatabase.ContactDatabase.FeedEntry.TIME_END_COLUMN, endCal.timeInMillis)
            put(
                ContactDatabase.ContactDatabase.FeedEntry.PHONE_COLUMN,
                cursor.getString(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.PHONE_COLUMN))
            )
            put(
                ContactDatabase.ContactDatabase.FeedEntry.RELATIVE_COLUMN,
                cursor.getInt(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.RELATIVE_COLUMN))
            )
            put(
                ContactDatabase.ContactDatabase.FeedEntry.COMPANIONS_COLUMN,
                cursor.getString(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.COMPANIONS_COLUMN))
            )
            put(
                ContactDatabase.ContactDatabase.FeedEntry.CLOSECONTACT_COLUMN,
                cursor.getInt(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.CLOSECONTACT_COLUMN))
            )
            put(
                ContactDatabase.ContactDatabase.FeedEntry.ENCOUNTER_COLUMN,
                cursor.getInt(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.ENCOUNTER_COLUMN))
            )
            put(
                ContactDatabase.ContactDatabase.FeedEntry.NOTES_COLUMN,
                cursor.getString(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.NOTES_COLUMN))
            )
            put(
                ContactDatabase.ContactDatabase.FeedEntry.MASK_COLUMN,
                cursor.getInt(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.MASK_COLUMN))
            )
            put(
                ContactDatabase.ContactDatabase.FeedEntry.VENTILATION_COLUMN,
                cursor.getInt(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.VENTILATION_COLUMN))
            )
        }

        db?.insert(ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME, null, values)
        cursor.close()
    }
}
