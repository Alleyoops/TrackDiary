package com.alleyoop.diary


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.alleyoop.diary.R
import com.alleyoop.diary.databinding.ActivityAddcontactInsideBinding
import com.alleyoop.diary.databinding.ActivityEditcontactBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class EditContactActivity : AppCompatActivity() {

    private val dbHelper = FeedReaderDbHelper(this)
    private val feedEntry = ContactDatabase.ContactDatabase.FeedEntry
    private lateinit var binding: ActivityEditcontactBinding
    private lateinit var elements: ActivityAddcontactInsideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditcontactBinding.inflate(layoutInflater)
        elements = binding.activityEditcontactInside
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        setupUI(findViewById(R.id.editcontactlayout))

        elements.contactTopDuplicateBtn.visibility = View.VISIBLE
        elements.contactTopDeleteBtn.visibility = View.VISIBLE

        val db = dbHelper.writableDatabase
        val info = intent.extras?.getString("entry")

        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM ${ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME}" +
                    " WHERE _id=" + info, null
        )
        cursor.moveToFirst()

        elements.nameInput.setText(cursor.getString(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.NAME_COLUMN)))
        elements.placeInput.setText(cursor.getString(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.PLACE_COLUMN)))

        val timeFormat = SimpleDateFormat("H:mm")
        val initCal = Calendar.getInstance()
        initCal.timeInMillis = cursor.getLong(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.TIME_BEGIN_COLUMN))

        val endCal = Calendar.getInstance()
        endCal.timeInMillis = cursor.getLong(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.TIME_END_COLUMN))

        elements.dateInput.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(initCal.time))

        if (!((initCal.get(Calendar.HOUR) == 0)
                    and (initCal.get(Calendar.MINUTE) == 0)
                    and (initCal.get(Calendar.SECOND) == 0)
                    and (initCal.get(Calendar.MILLISECOND) == 0))) {
            elements.inittimeInput.setText(timeFormat.format(initCal.time))
        }

        elements.enddateInput.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(endCal.time))

        if (!((endCal.get(Calendar.HOUR) == 0)
                    and (endCal.get(Calendar.MINUTE) == 0)
                    and (endCal.get(Calendar.SECOND) == 0)
                    and (endCal.get(Calendar.MILLISECOND) == 0))) {
            elements.endtimeInput.setText(timeFormat.format(endCal.time))
        }

        if (cursor.getString(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.PHONE_COLUMN)) != ""){
            elements.phoneInput.setText(cursor.getString(cursor.getColumnIndexOrThrow(
                ContactDatabase.ContactDatabase.FeedEntry.PHONE_COLUMN
            )))
        }

        val relative = cursor.getInt(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.RELATIVE_COLUMN))
        if (relative > 0) {
            val relativeBtn = elements.knownGroup.getChildAt(relative) as RadioButton
            relativeBtn.isChecked = true
        }

        val encounter = cursor.getInt(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.ENCOUNTER_COLUMN))
        if (encounter > 0) {
            val encounterBtn = elements.contactIndoorOutdoor.getChildAt(encounter) as RadioButton
            encounterBtn.isChecked = true
        }

        val preventionMeasures = ArrayList<String>()
        val closeContact = cursor.getInt(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.CLOSECONTACT_COLUMN))
        val mask = cursor.getInt(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.MASK_COLUMN))
        val ventilation = cursor.getInt(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.VENTILATION_COLUMN))
        if (closeContact == 0) {
            preventionMeasures.add(getString(R.string.mitigation_distance_value))
        }
        when (mask) {
            1 -> preventionMeasures.add(getString(R.string.mitigation_mask_other_value))
            2 -> preventionMeasures.add(getString(R.string.mitigation_mask_me_value))
            3 -> {
                preventionMeasures.add(getString(R.string.mitigation_mask_me_value))
                preventionMeasures.add(getString(R.string.mitigation_mask_other_value))
            }
        }
        if (ventilation == 1) {
            preventionMeasures.add(getString(R.string.mitigation_ventilation_value))
        }

        if (closeContact + mask + ventilation == -3) {
            elements.mitigation.text = getString(R.string.click_to_select)
        } else {
            if (preventionMeasures.isNotEmpty()) {
                elements.mitigation.text = preventionMeasures.sorted().joinToString(", ")
            }
        }

        elements.notesInput.setText(cursor.getString(cursor.getColumnIndexOrThrow(ContactDatabase.ContactDatabase.FeedEntry.NOTES_COLUMN)))

        cursor.close()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            initCal.set(Calendar.YEAR, year)
            initCal.set(Calendar.MONTH, monthOfYear)
            initCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            elements.dateInput.setText(DateFormat.getDateInstance().format(initCal.time))
            elements.enddateInput.setText(DateFormat.getDateInstance().format(initCal.time))
        }

        elements.dateInput.setOnClickListener {
            DatePickerDialog(
                this@EditContactActivity, dateSetListener,
                initCal.get(Calendar.YEAR),
                initCal.get(Calendar.MONTH),
                initCal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val endDateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            endCal.set(Calendar.YEAR, year)
            endCal.set(Calendar.MONTH, monthOfYear)
            endCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            elements.enddateInput.setText(DateFormat.getDateInstance().format(endCal.time))
        }

        elements.enddateInput.setOnClickListener {
            val pickDialog = DatePickerDialog(
                this@EditContactActivity, endDateSetListener,
                initCal.get(Calendar.YEAR),
                initCal.get(Calendar.MONTH),
                initCal.get(Calendar.DAY_OF_MONTH)
            )
            pickDialog.datePicker.minDate = initCal.time.time
            pickDialog.show()
        }

        val initTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            initCal.set(Calendar.HOUR_OF_DAY, hour)
            initCal.set(Calendar.MINUTE, minute)
            initCal.set(Calendar.MILLISECOND, 1)

            elements.inittimeInput.setText(timeFormat.format(initCal.time))
            if (elements.endtimeInput.text.isEmpty() or (endCal.timeInMillis < initCal.timeInMillis)) {
                endCal.set(Calendar.HOUR_OF_DAY, initCal.get(Calendar.HOUR_OF_DAY))
                endCal.set(Calendar.MINUTE, initCal.get(Calendar.MINUTE))
                endCal.add(Calendar.MINUTE, 30)
                elements.endtimeInput.setText(timeFormat.format(endCal.time))
            }
        }

        val is24Hour = android.text.format.DateFormat.is24HourFormat(applicationContext)

        elements.inittimeInput.setOnClickListener {
            TimePickerDialog(
                this@EditContactActivity, initTimeSetListener,
                initCal.get(Calendar.HOUR_OF_DAY),
                initCal.get(Calendar.MINUTE),
                is24Hour
            ).show()
        }

        val endTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            endCal.set(Calendar.HOUR_OF_DAY, hour)
            endCal.set(Calendar.MINUTE, minute)
            endCal.set(Calendar.MILLISECOND, 1)

            elements.endtimeInput.setText(timeFormat.format(endCal.time))
        }

        elements.endtimeInput.setOnClickListener {
            TimePickerDialog(
                this@EditContactActivity, endTimeSetListener,
                endCal.get(Calendar.HOUR_OF_DAY),
                endCal.get(Calendar.MINUTE),
                is24Hour
            ).show()
        }

        elements.mitigation.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val checkedItems = BooleanArray(4) {i -> preventionMeasures.contains(resources.getStringArray(
                R.array.mitigation_values
            )[i])}
            builder.setTitle(getString(R.string.mitigation_title))
            builder.setMultiChoiceItems(
                R.array.mitigation_entries, checkedItems
            ) { _, which, isChecked ->
                val measures = this.resources.getStringArray(R.array.mitigation_values)
                if (isChecked) {
                    preventionMeasures.add(measures[which])
                } else if (preventionMeasures.contains(measures[which])) {
                    preventionMeasures.remove(measures[which])
                }
            }

            builder.setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                var measuresTaken = getString(R.string.none)
                if (preventionMeasures.isNotEmpty()) {
                    measuresTaken = preventionMeasures.sorted().joinToString(", ")
                }
                elements.mitigation.text = measuresTaken
            }
            builder.setNegativeButton(getString(android.R.string.cancel)) { _, _ -> }
            builder.create().show()
        }

        elements.okButtonAddContact.setOnClickListener {
            val relativeId = elements.knownGroup.checkedRadioButtonId
            var relativeChoice = -1
            if (relativeId != -1) {
                val btn: View = elements.knownGroup.findViewById(relativeId)
                relativeChoice = elements.knownGroup.indexOfChild(btn)
            }

            val contactIndoorOutdoorId = elements.contactIndoorOutdoor.checkedRadioButtonId
            var contactIndoorOutdoorChoice = -1
            if (contactIndoorOutdoorId != -1) {
                val btn: View = elements.contactIndoorOutdoor.findViewById(contactIndoorOutdoorId)
                contactIndoorOutdoorChoice = elements.contactIndoorOutdoor.indexOfChild(btn)
            }

            val maskMe = preventionMeasures.contains(getString(R.string.mitigation_mask_me_value)).compareTo(false)
            val maskOther = preventionMeasures.contains(getString(R.string.mitigation_mask_other_value)).compareTo(false)

            var errorCount = 0
            val contactName = elements.nameInput.text.toString()
            if (contactName.isEmpty()) {
                elements.nameInput.error = getString(R.string.compulsory_field)
                errorCount++
            }
            if (errorCount == 0) {
                val values = ContentValues().apply {
                    put(ContactDatabase.ContactDatabase.FeedEntry.TYPE_COLUMN, "Contact")
                    put(ContactDatabase.ContactDatabase.FeedEntry.NAME_COLUMN, contactName)
                    put(ContactDatabase.ContactDatabase.FeedEntry.PLACE_COLUMN, elements.placeInput.text.toString())
                    put(ContactDatabase.ContactDatabase.FeedEntry.TIME_BEGIN_COLUMN, initCal.timeInMillis)
                    put(ContactDatabase.ContactDatabase.FeedEntry.TIME_END_COLUMN, endCal.timeInMillis)
                    put(ContactDatabase.ContactDatabase.FeedEntry.PHONE_COLUMN, elements.phoneInput.text.toString())
                    put(ContactDatabase.ContactDatabase.FeedEntry.RELATIVE_COLUMN, relativeChoice)
                    put(
                        ContactDatabase.ContactDatabase.FeedEntry.CLOSECONTACT_COLUMN,
                        if (elements.mitigation.text != getString(R.string.click_to_select)) {
                            (!preventionMeasures.contains(getString(R.string.mitigation_distance_value))).compareTo(
                                false
                            )
                        } else { -1 }
                    )
                    put(ContactDatabase.ContactDatabase.FeedEntry.ENCOUNTER_COLUMN, contactIndoorOutdoorChoice)
                    put(ContactDatabase.ContactDatabase.FeedEntry.NOTES_COLUMN, elements.notesInput.text.toString())
                    put(
                        ContactDatabase.ContactDatabase.FeedEntry.MASK_COLUMN,
                        if (elements.mitigation.text != getString(R.string.click_to_select)) {
                            2 * maskMe + maskOther
                        } else { -1 }
                    )
                    put(
                        ContactDatabase.ContactDatabase.FeedEntry.VENTILATION_COLUMN,
                        if (elements.mitigation.text != getString(R.string.click_to_select)) {
                            (preventionMeasures.contains(getString(R.string.mitigation_ventilation_value))).compareTo(
                                false
                            )
                        } else { -1 }
                    )
                }

                val selection = "_id LIKE ?"
                val selectionArgs = arrayOf(info.toString())
                db.update(ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME, values, selection, selectionArgs)

                Toast.makeText(
                    applicationContext,
                    getString(R.string.contact_saved),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    fun deleteContact(view: View) {
        val db = dbHelper.writableDatabase
        val info = intent.extras?.getString("entry")
        db.delete(ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME, "_id LIKE ?", arrayOf(info))

        Toast.makeText(
            applicationContext,
            applicationContext.resources.getString(R.string.entry_deleted),
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }

    fun duplicateContact(view: View) {
        val db = dbHelper.writableDatabase
        val info = intent.extras?.getString("entry")
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM ${ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME}" +
                    " WHERE _id=" + info, null
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
                ContactDatabase.ContactDatabase.FeedEntry.TYPE_COLUMN, cursor.getString(cursor.getColumnIndexOrThrow(
                    ContactDatabase.ContactDatabase.FeedEntry.TYPE_COLUMN
                )))
            put(
                ContactDatabase.ContactDatabase.FeedEntry.NAME_COLUMN, cursor.getString(cursor.getColumnIndexOrThrow(
                    ContactDatabase.ContactDatabase.FeedEntry.NAME_COLUMN
                )))
            put(
                ContactDatabase.ContactDatabase.FeedEntry.PLACE_COLUMN, cursor.getString(cursor.getColumnIndexOrThrow(
                    ContactDatabase.ContactDatabase.FeedEntry.PLACE_COLUMN
                )))
            put(ContactDatabase.ContactDatabase.FeedEntry.TIME_BEGIN_COLUMN, initCal.timeInMillis)
            put(ContactDatabase.ContactDatabase.FeedEntry.TIME_END_COLUMN, endCal.timeInMillis)
            put(
                ContactDatabase.ContactDatabase.FeedEntry.PHONE_COLUMN, cursor.getString(cursor.getColumnIndexOrThrow(
                    ContactDatabase.ContactDatabase.FeedEntry.PHONE_COLUMN
                )))
            put(
                ContactDatabase.ContactDatabase.FeedEntry.RELATIVE_COLUMN, cursor.getInt(cursor.getColumnIndexOrThrow(
                    ContactDatabase.ContactDatabase.FeedEntry.RELATIVE_COLUMN
                )))
            put(
                ContactDatabase.ContactDatabase.FeedEntry.COMPANIONS_COLUMN, cursor.getString(cursor.getColumnIndexOrThrow(
                    ContactDatabase.ContactDatabase.FeedEntry.COMPANIONS_COLUMN
                )))
            put(
                ContactDatabase.ContactDatabase.FeedEntry.CLOSECONTACT_COLUMN, cursor.getInt(cursor.getColumnIndexOrThrow(
                    ContactDatabase.ContactDatabase.FeedEntry.CLOSECONTACT_COLUMN
                )))
            put(
                ContactDatabase.ContactDatabase.FeedEntry.ENCOUNTER_COLUMN, cursor.getInt(cursor.getColumnIndexOrThrow(
                    ContactDatabase.ContactDatabase.FeedEntry.ENCOUNTER_COLUMN
                )))
            put(
                ContactDatabase.ContactDatabase.FeedEntry.NOTES_COLUMN, cursor.getString(cursor.getColumnIndexOrThrow(
                    ContactDatabase.ContactDatabase.FeedEntry.NOTES_COLUMN
                )))
            put(
                ContactDatabase.ContactDatabase.FeedEntry.MASK_COLUMN, cursor.getInt(cursor.getColumnIndexOrThrow(
                    ContactDatabase.ContactDatabase.FeedEntry.MASK_COLUMN
                )))
            put(
                ContactDatabase.ContactDatabase.FeedEntry.VENTILATION_COLUMN, cursor.getInt(cursor.getColumnIndexOrThrow(
                    ContactDatabase.ContactDatabase.FeedEntry.VENTILATION_COLUMN
                )))
        }

        db?.insert(ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME, null, values)
        cursor.close()

        Toast.makeText(
            applicationContext,
            applicationContext.resources.getString(R.string.entry_duplicated),
            Toast.LENGTH_SHORT
        ).show()
        cursor.close()
        finish()
    }

    private fun setupUI(view: View) {
        if (!((view is EditText) or (view is FloatingActionButton))) {
            view.setOnTouchListener { v, _ ->
                v.clearFocus()
                hideSoftKeyboard()
                false
            }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupUI(innerView)
            }
        }
    }

    private fun hideSoftKeyboard() {
        val inputMethodManager: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}
