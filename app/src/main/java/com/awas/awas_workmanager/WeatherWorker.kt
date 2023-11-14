package com.awas.awas_workmanager

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class WeatherWorker (context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {

    private val TAG = this.javaClass.canonicalName

    private var result = ""
    private var city = ""

    @SuppressLint("RestrictedApi")
    override fun doWork(): Result {
        Log.d(TAG, "doWork: Notification doing it's work")

        //receiving the data from the main activity
        this.city = inputData.getString(KEY_CITY).toString()

        result = fetchWeatherData(city)

        return if (result != null){
            //sent data to weather display fragment
            val outputData: Data = Data.Builder().putString(KEY_RESULT, result).build()
            Log.d(TAG, "doWork: success $outputData")

            //send status
            Result.success(outputData)

        }else{
            val outputData: Data = Data.Builder().putString(KEY_RESULT, "").build()
            Log.d(TAG, "doWork: Fail $outputData")
            Result.Failure(outputData)
        }

    }

    //handling the weather api and data:
    private fun fetchWeatherData(cityName : String): String {
        val apiKey = "016c76427999460c877221649233107" //achieved from https://www.weatherapi.com/my/
        val apiUrl = "https://api.weatherapi.com/v1/current.json?key=$apiKey&q=$cityName" //format to receive the json ie: https://api.weatherapi.com/v1/current.json?key=016c76427999460c877221649233107&q=43.6701503,-79.3738357 for my location
        var jsonString = ""
        try {
            val url = URL(apiUrl) //converts apiURL string into url object
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection //stores the connection to url
            connection.requestMethod = "GET" //using get method to get the information from url
            connection.connect() //instantiates the connection

            val inputStream = connection.inputStream //stores as input stream object
            val bufferedReader = BufferedReader(InputStreamReader(inputStream)) //stores as buffered reader object
            val response = bufferedReader.readLine() // reads that line from the buffered reader object
            bufferedReader.close() //close the buffered reader

            jsonString = response.toString();
            Log.d(TAG, "fetchWeatherdata: $jsonString")


        } catch (e: Exception) {
            e.printStackTrace()
        }
        return jsonString
    }


    companion object {
        private const val KEY_CITY = "CITY"
        private const val KEY_RESULT = "RESULT"
    }
}