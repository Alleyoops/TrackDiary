package com.alleyoop.diary

import android.app.Activity
import android.app.AlarmManager
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.Cursor.*
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.alleyoop.diary.ContactDatabase.Companion.SQL_CREATE_ENTRIES
import com.alleyoop.diary.ContactDatabase.Companion.SQL_DELETE_ENTRIES
import com.alleyoop.diary.R
import com.alleyoop.diary.databinding.ActivityPreferencesBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat


class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreferencesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction().replace(R.id.container, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private val EXPORT_DB = 1
        private val IMPORT_DB = 2
        private val feedEntry = ContactDatabase.ContactDatabase.FeedEntry

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val preferences = preferenceManager.sharedPreferences
            val prefsedit = preferences?.edit()

            val oldTime = preferences?.getString("reminder_time", "21:00").toString()
            val reminderTime = findPreference<EditTextPreference>("reminder_time")
            val reminderToggle =
                findPreference<SwitchPreference>("reminder_toggle") as SwitchPreference

            reminderTime?.setOnPreferenceChangeListener { _, newValue ->
                val alarmMgr = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val canSetAlarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmMgr.canScheduleExactAlarms()
                } else { true }
                if ( !canSetAlarm ) {
                    val permissionAsker: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                    permissionAsker.setMessage(getString(R.string.alarm_permission_required))
                    permissionAsker.setCancelable(true)
                    permissionAsker.setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                        startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    }
                    permissionAsker.setNegativeButton(android.R.string.cancel) { _, _ -> }
                    permissionAsker.create().show()
                    false
                } else {
                    var isTimeGood = true
                    val newTime = newValue as String
                    if (newTime.split(":").size == 2) {
                        val timeparts = newValue.split(":")
                        if ((timeparts[0].toInt() > 23) || (timeparts[1].toInt() > 59)) {
                            Toast.makeText(
                                context,
                                getString(R.string.incorrect_alarm_time),
                                Toast.LENGTH_LONG
                            ).show()
                            isTimeGood = false
                        }
                    } else {
                        Toast.makeText(
                            context,
                            getString(R.string.incorrect_alarm_time),
                            Toast.LENGTH_LONG
                        ).show()
                        isTimeGood = false
                    }
                    if ((newValue.toString() != oldTime) && isTimeGood) {
                        prefsedit?.putString("reminder_time", newValue)
                        prefsedit?.apply()
                        Toast.makeText(context, getString(R.string.alarm_modified), Toast.LENGTH_SHORT)
                            .show()
                        updateNotificationPreferences(reminderToggle.isEnabled)
                        true
                    } else {
                        prefsedit?.putString("reminder_time", oldTime)
                        prefsedit?.apply()
                        false
                    }
                }
            }
            reminderToggle.setOnPreferenceChangeListener { _, newValue ->
                updateNotificationPreferences(newValue as Boolean)
                true
            }

            val numDays = findPreference<EditTextPreference>("number_of_days")
            numDays?.setOnPreferenceChangeListener { _, newValue ->
                val newDays = newValue as String
                try {
                    val intDays = newDays.toInt()
                    if (intDays > 0) {
                        prefsedit?.putString("number_of_days", newDays)
                        prefsedit?.apply()
                        true
                    } else {Toast.makeText(
                        context,
                        getString(R.string.incorrect_number_of_days),
                        Toast.LENGTH_LONG
                    ).show()
                        false}
                } catch (e: NumberFormatException) {
                    Toast.makeText(
                        context,
                        getString(R.string.incorrect_number_of_days),
                        Toast.LENGTH_LONG
                    ).show()
                    false
                }
            }

            val prefTheme = findPreference<ListPreference>("theme")
            prefTheme!!.setOnPreferenceChangeListener { _, newValue ->
                when (newValue) {
                    "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    "System" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                true
            }

            val export = findPreference<Preference>("export")
            export!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                createFile()
                true
            }

            val import = findPreference<Preference>("import")
            import!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                readFile()
                true
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
            if ((requestCode == EXPORT_DB) && (resultCode == Activity.RESULT_OK)) {
                resultData?.data?.also { uri ->
                    exportDB(requireActivity().applicationContext, uri)
                }
            }
            if ((requestCode == IMPORT_DB) && (resultCode == Activity.RESULT_OK)) {
                resultData?.data?.also { uri ->
                    importDB(requireActivity().applicationContext, uri)
                }
            }
        }

        private fun updateNotificationPreferences(on: Boolean) {
            val receiver = ComponentName(
                requireActivity().applicationContext, NotificationReceiver::class.java
            )
            val pm = requireActivity().applicationContext.packageManager
            val notificationHandler = NotificationHandler()
            if (on) {
                notificationHandler.scheduleNotification(requireActivity().applicationContext)
                pm.setComponentEnabledSetting(
                    receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
            } else {
                notificationHandler.disableNotification(requireActivity().applicationContext)
                pm.setComponentEnabledSetting(
                    receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
        }

        private fun exportDB(context: Context, uri: Uri) {
            val dbHelper = FeedReaderDbHelper(context)
            val dateFormatter = SimpleDateFormat("yyyy-LL-dd-HH:mm")
            try {
                val csvWriter = context.contentResolver.openOutputStream(uri)
                val db: SQLiteDatabase = dbHelper.readableDatabase
                val cursor: Cursor = db.rawQuery(
                    "SELECT * FROM ${ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME}",
                    null
                )
                val columnNames =
                    cursor.columnNames.drop(1).toMutableList()
                columnNames[columnNames.indexOf("CloseContact")] = "DistanceKept"
                columnNames[columnNames.indexOf("Masks")] = "MaskMe"
                columnNames.add(columnNames.size-1,"MaskOthers")
                csvWriter!!.write(
                    columnNames.joinToString(separator = "\t", postfix = "\n").toByteArray()
                )
                while (cursor.moveToNext()) {
                    val columns = cursor.columnCount
                    val arrStr = mutableListOf<String>()
                    for (i in 1 until columns + 1) {
                        when (columnNames[i - 1]) {
                            "BeginTime" -> arrStr.add(dateFormatter.format(cursor.getLong(i)))
                            "EndTime" -> arrStr.add(dateFormatter.format(cursor.getLong(i)))
                            "Relative" -> arrStr.add(
                                when (cursor.getInt(i)) {
                                    -1 -> ""
                                    0 -> ""
                                    1 -> "Yes"
                                    3 -> "No"
                                    else -> cursor.getInt(i).toString()
                                }
                            )
                            "EncounterType" -> arrStr.add(
                                when (cursor.getInt(i)) {
                                    -1 -> ""
                                    1 -> "Indoors"
                                    3 -> "Outdoors"
                                    else -> cursor.getInt(i).toString()
                                }
                            )
                            "DistanceKept" -> arrStr.add(
                                when (cursor.getInt(i)) {
                                    -1 -> ""
                                    0 -> "Yes"
                                    1 -> "No"
                                    else -> cursor.getInt(i).toString()
                                }
                            )
                            "MaskMe" -> when (cursor.getInt(i)) {
                                    -1 -> {
                                        arrStr.add("")
                                        arrStr.add("")
                                    }
                                    0 -> {
                                        arrStr.add("No")
                                        arrStr.add("No")
                                    }
                                    1 -> {
                                        arrStr.add("No")
                                        arrStr.add("Yes")
                                    }
                                    2 -> {
                                        arrStr.add("Yes")
                                        arrStr.add("No")
                                    }
                                    3 -> {
                                        arrStr.add("Yes")
                                        arrStr.add("Yes")
                                    }
                                    else -> cursor.getInt(i).toString()
                                }
                            "MaskOthers" -> {}
                            "Ventilation" -> arrStr.add(
                                when (cursor.getInt(i - 1)) {
                                    -1 -> ""
                                    0 -> "No"
                                    1 -> "Yes"
                                    else -> cursor.getInt(i - 1).toString()
                                }
                            )
                            else -> when (cursor.getType(i)) {
                                FIELD_TYPE_STRING -> arrStr.add(cursor.getString(i))
                                FIELD_TYPE_INTEGER -> arrStr.add(cursor.getLong(i).toString())
                                FIELD_TYPE_NULL -> arrStr.add("")
                            }
                        }
                    }
                    csvWriter.write(
                        arrStr.joinToString(separator = "\t", postfix = "\n").toByteArray()
                    )
                }
                csvWriter.close()
                cursor.close()
                Toast.makeText(context, getString(R.string.export_success), Toast.LENGTH_LONG).show()
            } catch (sqlEx: Exception) {
                Log.e("Export", sqlEx.message, sqlEx)
            }
        }

        private fun importDB(context: Context, uri: Uri) {
            val dbHelper = FeedReaderDbHelper(context)
            val db = dbHelper.writableDatabase
            val csvReader = BufferedReader(
                InputStreamReader(
                    context.contentResolver.openInputStream(
                        uri
                    )
                )
            )

            val columnNames = csvReader.readLine().split("\t")
            if (columnNames != listOf(
                    ContactDatabase.ContactDatabase.FeedEntry.TYPE_COLUMN,
                    ContactDatabase.ContactDatabase.FeedEntry.NAME_COLUMN,
                    ContactDatabase.ContactDatabase.FeedEntry.PLACE_COLUMN,
                    ContactDatabase.ContactDatabase.FeedEntry.TIME_BEGIN_COLUMN,
                    ContactDatabase.ContactDatabase.FeedEntry.TIME_END_COLUMN,
                    ContactDatabase.ContactDatabase.FeedEntry.PHONE_COLUMN,
                    ContactDatabase.ContactDatabase.FeedEntry.RELATIVE_COLUMN,
                    ContactDatabase.ContactDatabase.FeedEntry.COMPANIONS_COLUMN,
                    ContactDatabase.ContactDatabase.FeedEntry.ENCOUNTER_COLUMN, "DistanceKept",
                    ContactDatabase.ContactDatabase.FeedEntry.NOTES_COLUMN, "MaskMe",
                    "MaskOthers",
                    ContactDatabase.ContactDatabase.FeedEntry.VENTILATION_COLUMN
                )
            ) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    getString(R.string.import_fail),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val dateFormatter = SimpleDateFormat("yyyy-MM-dd-HH:mm")
                try {
                    db.execSQL(SQL_DELETE_ENTRIES)
                    db.execSQL(SQL_CREATE_ENTRIES)
                    var nextLine = csvReader.readLine()
                    while (nextLine != null) {
                        val nextLineList = nextLine.split("\t")
                        val type = nextLineList[0]
                        val name = nextLineList[1]
                        val place = nextLineList[2]
                        val beginTime = nextLineList[3]
                        val endTime = nextLineList[4]
                        val phone = nextLineList[5]
                        val relative = nextLineList[6]
                        val companions = nextLineList[7]
                        val encounterType = nextLineList[8]
                        val distanceKept = nextLineList[9]
                        val notes = nextLineList[10]
                        val maskMe = when (nextLineList[11]) {
                            "Yes" -> 1
                            "No" -> 0
                            else -> 0
                        }
                        val maskOther = when (nextLineList[12]) {
                            "Yes" -> 1
                            "No" -> 0
                            else -> 0
                        }
                        val ventilation = nextLineList[13]

                        val values = ContentValues().apply {
                            put(ContactDatabase.ContactDatabase.FeedEntry.TYPE_COLUMN, type)
                            put(ContactDatabase.ContactDatabase.FeedEntry.NAME_COLUMN, name)
                            put(ContactDatabase.ContactDatabase.FeedEntry.PLACE_COLUMN, place)
                            put(ContactDatabase.ContactDatabase.FeedEntry.TIME_BEGIN_COLUMN, dateFormatter.parse(beginTime).time)
                            put(ContactDatabase.ContactDatabase.FeedEntry.TIME_END_COLUMN, dateFormatter.parse(endTime).time)
                            put(ContactDatabase.ContactDatabase.FeedEntry.PHONE_COLUMN, phone)
                            put(
                                ContactDatabase.ContactDatabase.FeedEntry.RELATIVE_COLUMN, when (relative) {
                                "Yes" -> 1
                                "No" -> 3
                                else -> -1
                            })
                            put(
                                ContactDatabase.ContactDatabase.FeedEntry.CLOSECONTACT_COLUMN, when (distanceKept) {
                                "Yes" -> 0
                                "No" -> 1
                                else -> -1
                            })
                            put(
                                ContactDatabase.ContactDatabase.FeedEntry.ENCOUNTER_COLUMN, when (encounterType) {
                                "Indoors" -> 1
                                "Outdoors" -> 3
                                else -> -1
                            })
                            put(ContactDatabase.ContactDatabase.FeedEntry.COMPANIONS_COLUMN, companions)
                            put(ContactDatabase.ContactDatabase.FeedEntry.NOTES_COLUMN, notes)
                            put(ContactDatabase.ContactDatabase.FeedEntry.MASK_COLUMN, 2*maskMe + maskOther)
                            put(
                                ContactDatabase.ContactDatabase.FeedEntry.VENTILATION_COLUMN, when (ventilation) {
                                "Yes" -> 1
                                "No" -> 0
                                else -> -1
                            })
                        }
                        db?.insert(ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME, null, values)
                        nextLine = csvReader.readLine()
                    }
                    Toast.makeText(context, getString(R.string.import_success), Toast.LENGTH_LONG).show()
                    db.close()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, getString(R.string.import_notfound), Toast.LENGTH_LONG)
                        .show()
                }
            }
            csvReader.close()
        }

        private fun createFile() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/csv"
                    putExtra(Intent.EXTRA_TITLE, "TrackDiary.csv")
                }
                startActivityForResult(intent, EXPORT_DB)
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage("I am afraid that the Export option is not yet available for " +
                        "Android versions below KitKat (4.4). I appreciate your patience while " +
                        "this is being developed. Feel free to drop me an email at any point.")
                builder.setPositiveButton(android.R.string.ok) { _, _ -> }
                builder.create().show()
            }
        }

        private fun readFile() {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "file/*"
            }
            val mimeTypes = arrayOf(
                "text/csv", "text/comma-separated-values", "text/tab-separated-values"
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    mimeTypes
                )
            } else {
                intent.type = mimeTypes.joinToString(separator = "|")
            }
            try {
                startActivityForResult(
                    Intent.createChooser(intent, getString(R.string.import_select)),
                    IMPORT_DB
                )
            } catch (ex: ActivityNotFoundException) {
                //对话框
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please install a File Manager",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
