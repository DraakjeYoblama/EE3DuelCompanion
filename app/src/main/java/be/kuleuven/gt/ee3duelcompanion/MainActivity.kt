package be.kuleuven.gt.ee3duelcompanion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private lateinit var editText: EditText;
    private lateinit var receivedText: TextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Check for Internet permission for SDK 23 or higher
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.INTERNET
                )
            ) {
                //rationale to display why the permission is needed

                Toast.makeText(
                    this,
                    "The app needs access to the Internet to send data to the ESP32",
                    Toast.LENGTH_SHORT
                ).show()
            }
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.INTERNET),
                MainActivity.Companion.PERMISSION_REQUEST_CODE
            )
        }

        // Set up a button to send data when clicked
        val sendButton = findViewById<Button>(R.id.send_button)
        sendButton.setOnClickListener { sendData() }

        // Set up a button to send data when clicked
        /*val receiveButton = findViewById<Button>(R.id.receive_button)
        sendButton.setOnClickListener { toggleReceivingData() }*/

        editText = findViewById<EditText>(R.id.send_text);
        receivedText = findViewById<EditText>(R.id.view_received_text);
    }

    //Check for result of permissions and feedback accordingly
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MainActivity.Companion.PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can perform the network operation here
                Log.d("Permission", "Granted")
            } else {
                // Permission denied, show a message to the user
                Log.d("Permission", "Denied")
                Toast.makeText(
                    this,
                    "Permission denied, you cannot perform network operations",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sendData() {
        // Set up a thread to send the data in the background
        Thread {
            try {
                // Connect to the ESP32's IP address and port
                Log.d("Sending Data", "Button Pressed, connecting to ESP32...")
                val socket = Socket("192.168.4.1", 80)
                val out = PrintWriter(socket.getOutputStream(), true) // Auto-flush enabled

                // Send the data
                out.println(editText.getText()) // "Hello, ESP32!"
                Log.d("Sending Data", "Data Sent!")

                // Receive the data
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))
                val receivedData = input.readLine()
                Log.d("Receiving Data", "Data Received: $receivedData")

                // Update UI with received data (ensure this runs on the main thread)
                runOnUiThread {
                    receivedText.setText(receivedData)
                }

                // Close the connection
                input.close()
                out.close()
                socket.close()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("Sending Data", "Error Sending Data: " + e.message)
            }
        }.start()
    }

    /*
    private fun receiveData() {
        // Set up a thread to receive the data in the background
        Thread {
            try {
                // Connect to the ESP32's IP address and port
                Log.d("Receiving Data", "Connecting to ESP32...")
                val socket = Socket("192.168.4.1", 80)
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))

                // Receive the data
                val receivedData = input.readLine()
                Log.d("Receiving Data", "Data Received: $receivedData")
                //receivedText.setText(receivedData)

                // Close the connection
                input.close()
                socket.close()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("Receiving Data", "Error Receiving Data: " + e.message)
            }
        }.start()
    }

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            receiveData()
            handler.postDelayed(this, 5000) // Call this runnable again after 5 seconds
        }
    }

    fun toggleReceivingData() {
        handler.post(runnable) // Start the initial call
    }
    fun startReceivingData() {
        handler.post(runnable) // Start the initial call
    }

    fun stopReceivingData() {
        handler.removeCallbacks(runnable) // Stop the repeated calls
    }

     */

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }
}