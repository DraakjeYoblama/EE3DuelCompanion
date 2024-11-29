package be.kuleuven.gt.ee3duelcompanion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private lateinit var editText: EditText;

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

        editText = findViewById<EditText>(R.id.send_text);
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
                val out = PrintWriter(socket.getOutputStream())

                // Send the data
                out.println(editText.getText()) // "Hello, ESP32!"
                out.flush()
                Log.d("Sending Data", "Data Sent!")

                // Close the connection
                out.close()
                socket.close()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("Sending Data", "Error Sending Data: " + e.message)
            }
        }.start()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }
}