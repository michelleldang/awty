package edu.uw.ischool.mldang.awty

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.telephony.SmsManager
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

const val ALARM_ACTION = "edu.uw.ischool.mldang.ALARM"

class MainActivity : AppCompatActivity() {
    lateinit var button : Button
    lateinit var messageInput: EditText
    lateinit var phoneInput: EditText
    lateinit var minutesInput: EditText
    var receiver : BroadcastReceiver? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button = findViewById(R.id.start_button)
        messageInput = findViewById(R.id.message_input)
        phoneInput = findViewById(R.id.phone_input)
        minutesInput = findViewById(R.id.minutes_input)
        button.setOnClickListener {
            if (messageInput.text.toString() != "" && phoneInput.text.toString()!= "" && minutesInput.text.toString()!= "") {
                validate(
                    messageInput.text.toString(),
                    phoneInput.text.toString(),
                    minutesInput.text.toString()
                )
            } else {
                Toast.makeText(this, "Field is empty", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun validate (message: String, phone: String, minutes: String) {
        val  button: Button = findViewById(R.id.start_button)
        errorMessages(message, phone, minutes)
        if (message.trim() != "" && Regex("^(\\+\\d{1,2}\\s?)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}\$").matches(phone.trim()) && Integer.parseInt(minutes) > 0) {
            if (button.text == "Start") {
                startMessages(message, phone, Integer.parseInt(minutes))
                button.text = "Stop"
            } else {
                button.text = "Start"
                stopMessages()
            }
        } else if ( button.text == "Stop") {
            button.text = "Start"
            stopMessages()
        }
    }
    private fun errorMessages(message: String, phone: String, minutes: String) {
        if(message.trim() == "" ) {
            messageInput.error = "Message field cannot be empty"
        } else {
            messageInput.error = null
        }
        if(!Regex("^(\\+\\d{1,2}\\s?)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}\$").matches(phone.trim()) || phone.trim() == "") {
            phoneInput.error = "Phone number not in correct format"
        } else {
            phoneInput.error = null
        }
        if( Integer.parseInt(minutes) < 0 || minutes.trim() == "") {
            minutesInput.error = "Minutes interval cannot be 0 or below"
        } else {
            minutesInput.error = null
        }
    }
    private fun startMessages (message: String, phone: String, minutes: Int) {
        messageInput.isEnabled = false
        phoneInput.isEnabled = false
        minutesInput.isEnabled = false

        val activityThis = this
        if (receiver == null) {
            receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Log.d("MainActivity", "toast activity ")
                    sendMessages(message, phone)
                }
            }
            val filter = IntentFilter(ALARM_ACTION)
            registerReceiver(receiver, filter)
        }
        val intent = Intent(ALARM_ACTION)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val alarmManager : AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            (1000*60*minutes).toLong(), //  1000 * 60 * minutes
            pendingIntent)
    }
    private fun stopMessages () {
        messageInput.isEnabled = true
        phoneInput.isEnabled = true
        minutesInput.isEnabled = true
        unregisterReceiver(receiver)
        receiver = null
    }

    private fun sendMessages (message: String, phone: String) {
            try {
                val smsManager:SmsManager = if (Build.VERSION.SDK_INT>=23) {
                    this.getSystemService(SmsManager::class.java)
                } else{
                    SmsManager.getDefault()
                }
                val phoneNumber = phone.replace(Regex("[^\\d]"), "")
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Log.d("MainActivity", "message sent")
        } catch (e: Exception) {
                Log.e("sendMessages", "$e")
        }
    }

}





