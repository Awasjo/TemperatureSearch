package com.awas.awas_workmanager

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awas.awas_workmanager.databinding.FragmentWeatherDisplayBinding
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class WeatherDisplay : Fragment() {
    private val TAG = this.javaClass.canonicalName
    private var _binding : FragmentWeatherDisplayBinding? = null
    private val binding
        get() = checkNotNull(_binding){
            "Cannot access binding because it is null"
        }
    private var result = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentWeatherDisplayBinding.inflate(inflater, container, false)

        val myResult = arguments?.getString(KEY_RESULT)
        if (!myResult.isNullOrEmpty()) {
            processWeatherData(myResult)
        }else{
            binding.locationTextView.text = "City does not exist."
        }



        return binding.root

    }
    //method for getting the information from the json and then assigning to the UI components
    private fun processWeatherData(response: String) {
        try {
            val jsonObject = JSONObject(response)
            val location = jsonObject.getJSONObject("location").getString("name")  + " " + jsonObject.getJSONObject("location").getString("country")
            val time = jsonObject.getJSONObject("location").getString("localtime")
            val current = jsonObject.getJSONObject("current")
            val tempC = current.getDouble("temp_c").toString()
            val feelsLikeC = current.getDouble("feelslike_c").toString()
            val windKph = current.getDouble("wind_kph").toString()
            val windDir = current.getString("wind_dir")
            val humidity = current.getDouble("humidity").toString()
            val uv = current.getDouble("uv").toString()
            val visibilityKm = current.getDouble("vis_km").toString()
            val condition = current.getJSONObject("condition").getString("text")

            val weatherIconUrl = current.getJSONObject("condition").getString("icon")
            // load weather icon image using HttpURLConnection
            loadWeatherIconImage("https:$weatherIconUrl")

            updateUI(location, time, tempC, feelsLikeC, windKph, windDir, humidity, uv, visibilityKm, condition)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //i used the image link found in the API to be used in my app
    private fun loadWeatherIconImage(iconUrl: String) {
        Thread {
            try {
                val url = URL(iconUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream) //bit map factory class enables you to make bitmap from different types of sources, in this case an inputstream from the secure https request

                requireActivity().runOnUiThread {
                    binding.weatherImageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    // helper method update the UI
    private fun updateUI(
        location: String,
        time : String,
        tempC: String,
        feelsLikeC: String,
        windKph: String,
        windDir: String,
        humidity: String,
        uv: String,
        visibilityKm: String,
        condition: String
    ) {
        binding.locationTextView.text = location
        binding.timeTextView.text = "Date: $time"
        binding.tempCTextView.text = "$tempC °C"
        binding.feelsLikeCTextView.text = "Feels like $feelsLikeC °C"
        binding.windTextView.text = "Wind: $windKph kph $windDir"
        binding.humidityTextView.text = "Humidity: $humidity%"
        binding.uvTextView.text = "UV: $uv"
        binding.visibilityTextView.text = "Visibility: $visibilityKm km"
        binding.conditionTextView.text = condition
    }

    companion object {
        private const val KEY_CITY = "CITY"
        private const val KEY_RESULT = "RESULT"
    }
}