package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myapplication.databinding.ActivityMainBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.Flow.Subscription
import java.util.logging.LogManager


const val URL = "https://api.icndb.com/jokes/random"
const val DELAY: Long = 3000


class MainActivity : AppCompatActivity() {
    var okHttpClient: OkHttpClient = OkHttpClient()

    lateinit var binding: ActivityMainBinding

    lateinit var mainHandler: Handler

    private val getDrivePermissionTask = object : Runnable {
        override fun run() {
            getDrivePermission()
            mainHandler.postDelayed(this, DELAY)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.button2.setOnClickListener {
            val newIntent = Intent(this, DriveActivity::class.java)
            startActivity(newIntent)
        }
        mainHandler = Handler(Looper.getMainLooper())

    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(getDrivePermissionTask)
    }

    override fun onResume() {
        super.onResume()
        mainHandler.post(getDrivePermissionTask)
    }

    private fun getDrivePermission() {

        runOnUiThread {
            binding.progressBar.visibility = View.VISIBLE
        }


        val request: Request = Request.Builder().url(URL).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                print("lel")
            }

            override fun onResponse(call: Call?, response: Response?) {
                val json = JSONObject(response?.body()?.string()!!)
                val jsonValue = json.getJSONObject("value")
                if (Integer.valueOf(jsonValue.get("id").toString()) % 5 == 0){
                    mainHandler.removeCallbacks(getDrivePermissionTask)
                    runOnUiThread{
                        binding.button2.isEnabled = true
                    }
                }

                val txt = "${jsonValue.get("id")} ${jsonValue.get("joke")}"

                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.textView.text = Html.fromHtml(txt, 0)
                }
            }
        })
    }
}