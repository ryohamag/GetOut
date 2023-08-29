package com.websarva.wings.getout

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NotificationActivity : AppCompatActivity() {
    private lateinit var numberInput1: EditText
    private lateinit var numberInput2: EditText
    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        numberInput1 = findViewById(R.id.numberInput1)
        numberInput2 = findViewById(R.id.numberInput2)
        resultText = findViewById(R.id.resultText)

        val button: Button = findViewById(R.id.completionbutton)
        button.setOnClickListener {
            addNumbers()
        }
    }

    private fun addNumbers() {
        val hoursText = numberInput1.text.toString()
        val minutesText = numberInput2.text.toString()

        if (hoursText.isNotEmpty() && minutesText.isNotEmpty()) {
            val hours = hoursText.toInt()
            val minutes = minutesText.toInt()

            // Calculate something or perform an action here

            val result = "Calculated result: ${hours + minutes}"

            // Display a Toast message with the calculated result
            Toast.makeText(this, "目標時間が設定されました", Toast.LENGTH_SHORT).show()
        }
    }

    fun addNumbers(view: View) {
        val num1 = numberInput1.text.toString().toIntOrNull() ?: 0
        val num2 = numberInput2.text.toString().toIntOrNull() ?: 0

        val sum = 60*num1 + num2
        resultText.text = "Result: $sum"
    }
}
