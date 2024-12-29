package ru.mrfix1033.myalarmclock

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatWithExitMenu() {
    private var alarmList = mutableListOf<Long>()
    private lateinit var listView: ListView

    private val formatTime = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var calendar: Calendar? = null
    private var materialTimePicker: MaterialTimePicker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        title=getString(R.string.app_name)
        setSupportActionBar(findViewById(R.id.toolbar))

        listView = findViewById(R.id.listView)
        createAndSetAdapter()

        findViewById<Button>(R.id.buttonAddAlarm).setOnClickListener {
            val now = LocalTime.now()
            materialTimePicker =
                MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(now.hour)
                    .setMinute(now.minute)
                    .setTitleText("Выберите время будильника")
                    .build()

            materialTimePicker!!.addOnPositiveButtonClickListener {
                calendar = Calendar.getInstance()
                calendar!!.set(Calendar.SECOND, 0)
                calendar!!.set(Calendar.MILLISECOND, 0)
                calendar!!.set(Calendar.MINUTE, materialTimePicker!!.minute)
                calendar!!.set(Calendar.HOUR_OF_DAY, materialTimePicker!!.hour)

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.setExact(
                    RTC_WAKEUP,
                    calendar!!.timeInMillis,
                    getAlarmPendingIntent()
                )

                val formattedTime = formatTime.format(calendar!!.time)
                Toast.makeText(
                    this,
                    "Будильник установлен на $formattedTime",
                    Toast.LENGTH_SHORT
                ).show()
                alarmList.add(calendar!!.time.time)
                dataSetChanged()
            }
            materialTimePicker!!.show(supportFragmentManager, "time_picker")
        }
    }

    private fun createAndSetAdapter() {
        listView.adapter = object : ArrayAdapter<Long>(this, R.layout.alarm_item, alarmList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var view = convertView
                if (view == null) view = LayoutInflater.from(this@MainActivity)
                    .inflate(R.layout.alarm_item, parent, false)!!
                view.findViewById<TextView>(R.id.textViewTime).text =
                    formatTime.format(alarmList[position])
                return view
            }
        }
    }

    private fun dataSetChanged() {
        (listView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
    }

    private fun getAlarmPendingIntent(): PendingIntent {
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getBroadcast(
            this,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        alarmList = savedInstanceState.getLongArray("alarmList")!!.toMutableList()
        createAndSetAdapter()
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLongArray("alarmList", alarmList.toLongArray())
        super.onSaveInstanceState(outState)
    }
}