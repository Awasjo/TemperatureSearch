package com.awas.awas_workmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.awas.awas_workmanager.databinding.FragmentUserCitySearchBinding
import java.sql.DatabaseMetaData


class UserCitySearch : Fragment() {

    private val TAG = this.javaClass.canonicalName
    private var _binding : FragmentUserCitySearchBinding? = null
    private val binding
        get() = checkNotNull(_binding){
            "Cannot access binding because it is null"
        }

    //for notification work
    private var weatherWorkManager: WorkManager? = null
    private var weatherOneTimeWorkRequest: OneTimeWorkRequest? =
        null //this is a process/work that needs to be run in the background, it can be anything



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
    //        return inflater.inflate(R.layout.fragment_user_city_search, container, false)
        _binding = FragmentUserCitySearchBinding.inflate(inflater, container, false)


        binding.btnGetWeather.setOnClickListener{
            initiateWeatherWork()
            //execute or start the work request
            weatherOneTimeWorkRequest?.let { oneTimeWorkRequest ->
                this.weatherWorkManager?.enqueue(
                    oneTimeWorkRequest
                )
            }
//            findNavController().navigate(R.id.action_userCitySearch_to_WeatherDisplay)
        }

        return binding.root
    }

    private fun initiateWeatherWork() {
        //instantiate the work manager
        weatherWorkManager = WorkManager.getInstance(requireContext().applicationContext)

        //data to send to worker
        val inputData: Data = Data.Builder()
            .putString(KEY_CITY, binding.cityMame.text.toString())
            .build()

        weatherOneTimeWorkRequest =
            OneTimeWorkRequest.Builder(WeatherWorker::class.java)
                .setInputData(inputData)
                .build()

        //observe the work status and act acordingly
        this.weatherWorkManager!!.getWorkInfoByIdLiveData(this.weatherOneTimeWorkRequest!!.id)
            .observe(viewLifecycleOwner) { workInfo ->
                if (workInfo != null) {
                    val state = workInfo.state
                    if (state.isFinished) {
                        val myResult = workInfo.outputData.getString(KEY_RESULT)
                        myResult?.let {
                            val weatherDisplayFragment = WeatherDisplay()
                            val bundle = Bundle()
                            bundle.putString(KEY_RESULT, myResult)
                            weatherDisplayFragment.arguments = bundle
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.nav_host_fragment, weatherDisplayFragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                }
            }
    }

    companion object {
        private const val KEY_CITY = "CITY"
        private const val KEY_RESULT = "RESULT"
    }
}