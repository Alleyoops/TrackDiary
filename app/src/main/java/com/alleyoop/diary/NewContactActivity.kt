package com.alleyoop.diary


import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alleyoop.diary.R
import com.alleyoop.diary.databinding.ActivityAddcontactBinding
import com.alleyoop.diary.databinding.ActivityAddcontactInsideBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NewContactActivity : AppCompatActivity() {

    private val feedEntry = ContactDatabase.ContactDatabase.FeedEntry
    private lateinit var binding: ActivityAddcontactBinding
    private lateinit var elements: ActivityAddcontactInsideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddcontactBinding.inflate(layoutInflater)
        elements = binding.activityAddcontactInside
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        setupUI(findViewById(R.id.newcontactlayout))

        val initCal = Calendar.getInstance()
        initCal.set(Calendar.HOUR_OF_DAY, 0)
        initCal.set(Calendar.MINUTE, 0)
        initCal.set(Calendar.SECOND, 0)
        initCal.set(Calendar.MILLISECOND, 0)

        val endCal = Calendar.getInstance()
        endCal.set(Calendar.HOUR_OF_DAY, 0)
        endCal.set(Calendar.MINUTE, 0)
        endCal.set(Calendar.SECOND, 0)
        endCal.set(Calendar.MILLISECOND, 0)

//      设置当前值
        elements.dateInput.setText(DateFormat.getDateInstance().format(initCal.time))
        elements.enddateInput.setText(DateFormat.getDateInstance().format(endCal.time))

        val timeFormat = SimpleDateFormat("H:mm")

//      监听new value
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            initCal.set(Calendar.YEAR, year)
            initCal.set(Calendar.MONTH, monthOfYear)
            initCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            endCal.set(Calendar.YEAR, year)
            endCal.set(Calendar.MONTH, monthOfYear)
            endCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            elements.dateInput.setText(DateFormat.getDateInstance().format(initCal.time))
            elements.enddateInput.setText(DateFormat.getDateInstance().format(initCal.time))
        }

        elements.dateInput.setOnClickListener {
            DatePickerDialog(
                this@NewContactActivity, dateSetListener,
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
                this@NewContactActivity, endDateSetListener,
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

        val is24Hour = is24HourFormat(applicationContext)

        elements.inittimeInput.setOnClickListener {
            TimePickerDialog(
                this@NewContactActivity, initTimeSetListener,
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
                this@NewContactActivity, endTimeSetListener,
                endCal.get(Calendar.HOUR_OF_DAY),
                endCal.get(Calendar.MINUTE),
                is24Hour
            ).show()
        }

        val preventionMeasures = ArrayList<String>()
        elements.mitigation.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val checkedItems = BooleanArray(4) { i -> preventionMeasures.contains(
                resources.getStringArray(
                    R.array.mitigation_values
                )[i]
            )}
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

        val dbHelper = FeedReaderDbHelper(this)

        elements.okButtonAddContact.setOnClickListener {
            val db = dbHelper.writableDatabase
            var errorCount = 0
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

            val maskMe = preventionMeasures.contains(getString(R.string.mitigation_mask_me_value)).compareTo(
                false
            )
            val maskOther = preventionMeasures.contains(getString(R.string.mitigation_mask_other_value)).compareTo(
                false
            )

            val contactName = elements.nameInput.text.toString()
            if (contactName.isEmpty()) {
                elements.nameInput.error = getString(R.string.compulsory_field)
                errorCount++
            }

//          创建一个新的映射的值
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

//              插入新行,返回主键值的新行
                db?.insert(ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME, null, values)

                Toast.makeText(
                    applicationContext,
                    applicationContext.resources.getString(R.string.contact_saved),
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
        }
    }

    private fun setupUI(view: View) {
        //为非文本框设置触摸侦听器视图隐藏键盘
        if (!((view is EditText) or (view is FloatingActionButton))) {
            view.setOnTouchListener { v, _ ->
                v.clearFocus()
                hideSoftKeyboard()
                false
            }
        }

        //如果布局容器,递归遍历。
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
