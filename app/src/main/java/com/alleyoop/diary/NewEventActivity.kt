package com.alleyoop.diary


import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.alleyoop.diary.R
import com.alleyoop.diary.databinding.ActivityAddeventBinding
import com.alleyoop.diary.databinding.ActivityAddeventInsideBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class NewEventActivity : AppCompatActivity() {

    private val feedEntry = ContactDatabase.ContactDatabase.FeedEntry
    private lateinit var binding: ActivityAddeventBinding
    private lateinit var elements: ActivityAddeventInsideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddeventBinding.inflate(layoutInflater)
        elements = binding.activityAddeventInside
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))
        setupUI(findViewById(R.id.neweventlayout))

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

        val timeFormat = SimpleDateFormat("H:mm")

//      设置当前值
        elements.eventdateInput.setText(DateFormat.getDateInstance().format(initCal.time))
        elements.endeventdateInput.setText(DateFormat.getDateInstance().format(endCal.time))

        //打开菜单,地点和时间
        if ((intent.type != null) and (intent.action.equals(Intent.ACTION_SEND))) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)
            if (preferences.getBoolean("remindWarning", true)) {
                val checkInReminder = androidx.appcompat.app.AlertDialog.Builder(this)
                checkInReminder.setTitle(getString(R.string.checkin_title))
                    .setMessage(getString(R.string.checkin_reminder))
                    .setCancelable(true)
                    .setPositiveButton(getString(android.R.string.ok)) {_, _ -> }
                    .setNegativeButton(getString(R.string.do_not_show_again)) { _, _ ->
                    val editor = preferences.edit()
                    editor.putBoolean("remindWarning", false)
                    editor.apply()
                }
                    .create()
                    .show()
            }
            val data = getPlace(intent.getStringExtra(Intent.EXTRA_TEXT) as String)
            if (data.any {it.isNotEmpty()}) {
                elements.eventnameInput.setText(data[0])
                elements.eventplaceInput.setText(data[1])
                elements.eventnotesInput.setText(data[2])

                initCal.timeInMillis = Calendar.getInstance().timeInMillis
                endCal.timeInMillis = initCal.timeInMillis + 60 * 60 * 1000
                elements.eventinittimeInput.setText(timeFormat.format(initCal.time))
                elements.eventendtimeInput.setText(timeFormat.format(endCal.time))
            }
        }

//      监听新值
        val eventdateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            initCal.set(Calendar.YEAR, year)
            initCal.set(Calendar.MONTH, monthOfYear)
            initCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            endCal.set(Calendar.YEAR, year)
            endCal.set(Calendar.MONTH, monthOfYear)
            endCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            elements.eventdateInput.setText(DateFormat.getDateInstance().format(initCal.time))
            elements.endeventdateInput.setText(DateFormat.getDateInstance().format(initCal.time))
        }

        elements.eventdateInput.setOnClickListener {
            DatePickerDialog(
                this@NewEventActivity, eventdateSetListener,
                initCal.get(Calendar.YEAR),
                initCal.get(Calendar.MONTH),
                initCal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val endeventdateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            endCal.set(Calendar.YEAR, year)
            endCal.set(Calendar.MONTH, monthOfYear)
            endCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            elements.endeventdateInput.setText(DateFormat.getDateInstance().format(endCal.time))
        }

        elements.endeventdateInput.setOnClickListener {
            val pickDialog = DatePickerDialog(
                this@NewEventActivity, endeventdateSetListener,
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

            elements.eventinittimeInput.setText(timeFormat.format(initCal.time))
            if (elements.eventendtimeInput.text.isEmpty() or (endCal.timeInMillis < initCal.timeInMillis)) {
                endCal.set(Calendar.HOUR_OF_DAY, initCal.get(Calendar.HOUR_OF_DAY))
                endCal.set(Calendar.MINUTE, initCal.get(Calendar.MINUTE))
                endCal.add(Calendar.MINUTE, 30)
                elements.eventendtimeInput.setText(timeFormat.format(endCal.time))
            }
        }

        val is24Hour = android.text.format.DateFormat.is24HourFormat(applicationContext)

        elements.eventinittimeInput.setOnClickListener {
            TimePickerDialog(
                this@NewEventActivity, initTimeSetListener,
                initCal.get(Calendar.HOUR_OF_DAY),
                initCal.get(Calendar.MINUTE),
                is24Hour
            ).show()
        }

        val endTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            endCal.set(Calendar.HOUR_OF_DAY, hour)
            endCal.set(Calendar.MINUTE, minute)
            endCal.set(Calendar.MILLISECOND, 1)
                elements.eventendtimeInput.setText(timeFormat.format(endCal.time))
        }

        elements.eventendtimeInput.setOnClickListener {
            TimePickerDialog(
                this@NewEventActivity, endTimeSetListener,
                endCal.get(Calendar.HOUR_OF_DAY),
                endCal.get(Calendar.MINUTE),
                is24Hour
            ).show()
        }

        val preventionMeasures = ArrayList<String>()
        elements.eventMitigation.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val checkedItems = BooleanArray(4) { i -> preventionMeasures.contains(
                resources.getStringArray(R.array.mitigation_values)[i]
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
                elements.eventMitigation.text = measuresTaken
            }
            builder.setNegativeButton(getString(android.R.string.cancel)) { _, _ -> }
            builder.create().show()
        }

        val dbHelper = FeedReaderDbHelper(this)

        elements.okButtonAddEvent.setOnClickListener {
            val db = dbHelper.writableDatabase
            var errorCount = 0
            val eventIndoorOutdoorId = elements.eventIndoorOutdoor.checkedRadioButtonId
            var eventIndoorOutdoorChoice = -1
            if (eventIndoorOutdoorId != -1) {
                val btn: View = elements.eventIndoorOutdoor.findViewById(eventIndoorOutdoorId)
                eventIndoorOutdoorChoice = elements.eventIndoorOutdoor.indexOfChild(btn)
            }

            val maskMe = preventionMeasures.contains(getString(R.string.mitigation_mask_me_value)).compareTo(false)
            val maskOther = preventionMeasures.contains(getString(R.string.mitigation_mask_other_value)).compareTo(false)

            val eventName = elements.eventnameInput.text.toString()
            if (eventName.isEmpty()) {
                elements.eventnameInput.error = getString(R.string.compulsory_field)
                errorCount++
            }
            if (errorCount == 0) {
                val values = ContentValues().apply {
                    put(ContactDatabase.ContactDatabase.FeedEntry.TYPE_COLUMN, "Event")
                    put(ContactDatabase.ContactDatabase.FeedEntry.NAME_COLUMN, eventName)
                    put(ContactDatabase.ContactDatabase.FeedEntry.PLACE_COLUMN, elements.eventplaceInput.text.toString())
                    put(ContactDatabase.ContactDatabase.FeedEntry.TIME_BEGIN_COLUMN, initCal.timeInMillis)
                    put(ContactDatabase.ContactDatabase.FeedEntry.TIME_END_COLUMN, endCal.timeInMillis)
                    put(ContactDatabase.ContactDatabase.FeedEntry.PHONE_COLUMN, elements.eventphoneInput.text.toString())
                    put(ContactDatabase.ContactDatabase.FeedEntry.COMPANIONS_COLUMN, elements.eventpeopleInput.text.toString())
                    put(ContactDatabase.ContactDatabase.FeedEntry.ENCOUNTER_COLUMN, eventIndoorOutdoorChoice)
                    put(
                        ContactDatabase.ContactDatabase.FeedEntry.CLOSECONTACT_COLUMN,
                        if (elements.eventMitigation.text != getString(R.string.click_to_select)) {
                            (!preventionMeasures.contains(getString(R.string.mitigation_distance_value))).compareTo(
                                false
                            )
                        } else { -1 }
                    )
                    put(ContactDatabase.ContactDatabase.FeedEntry.NOTES_COLUMN, elements.eventnotesInput.text.toString())
                    put(
                        ContactDatabase.ContactDatabase.FeedEntry.MASK_COLUMN,
                        if (elements.eventMitigation.text != getString(R.string.click_to_select)) {
                            2 * maskMe + maskOther
                        } else { -1 }
                    )
                    put(
                        ContactDatabase.ContactDatabase.FeedEntry.VENTILATION_COLUMN,
                        if (elements.eventMitigation.text != getString(R.string.click_to_select)) {
                            (preventionMeasures.contains(getString(R.string.mitigation_ventilation_value))).compareTo(
                                false
                            )
                        } else { -1 }
                    )
                }
                db?.insert(ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME, null, values)

                Toast.makeText(
                    applicationContext,
                    applicationContext.resources.getString(R.string.event_saved),
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
        }
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

    private fun getPlace(qrCode: String): List<String> {
        var name = ""
        var place = ""
        var notes = ""
        var postalCode = ""
        when {
            qrCode.take(12) == "UKC19TRACING" -> {
                val data = qrCode.split(":").last().split(".")[1]
                val decoded = String(Base64.decode(data, Base64.DEFAULT), charset("UTF-8"))
                val parts = decoded.drop(1).dropLast(1).split(",")
                run loop@{
                    parts.forEach {
                        when {
                            (it.split(":")[0] == "\"opn\"") -> {
                                name = it.split(":")[1].drop(1).dropLast(1)
                            }
                            (it.split(":")[0] == "\"pc\"") -> {
                                postalCode = "PC " + it.split(":")[1].drop(1).dropLast(1)
                            }
                            (it.split(":")[0] == "\"adr\"") -> {
                                place = it.split(":")[1].drop(1).dropLast(1)
                                place = place.split("\\n").joinToString(", ")
                            }
                        }
                    }
                }
                if (place == "") { place = postalCode }
                notes = getString(R.string.shared_from) + " UKC19TRACING"
            }
            qrCode.take(13) == "NZCOVIDTRACER" -> {
                val data = qrCode.split(":").last()
                val decoded = String(Base64.decode(data, Base64.DEFAULT), charset("UTF-8"))
                val parts = decoded.drop(1).dropLast(1).split(",")
                run loop@{
                    parts.forEach {
                        when {
                            (it.split(":")[0] == "\"opn\"") -> {
                                name = it.split(":")[1].drop(1).dropLast(1)
                            }
                            (it.split(":")[0] == "\"adr\"") -> {
                                place = it.split(":")[1].drop(1).dropLast(1)
                                place = place.split("\\n").joinToString(", ")
                            }
                        }
                    }
                }
                notes = getString(R.string.shared_from) + " NZCOVIDTRACER"
            }
            qrCode.split("\n")[2].take(6) == ("N:TUR-") -> {
                val data = qrCode.split("\n")
                val parts = data.subList(3, data.size-4)
                run loop@{
                    parts.forEach {
                        when {
                            (it.split(":")[0] == "FN") -> {
                                name = it.split(":")[1]
                            }
                            (it.split(";")[0] == "ADR") -> {
                                place = it.split(";").drop(3)
                                    .joinToString(",").split("\\")
                                    .joinToString("")
                            }
                        }
                    }
                }
                notes = getString(R.string.shared_from) + " PassCOVID-GAL"
            }
            else -> {
                Toast.makeText(this, getString(R.string.qr_error), Toast.LENGTH_LONG).show()
            }
        }
        return listOf(name, place, notes)
    }
}