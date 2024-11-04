package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapitest.databinding.ActivityCarDetailBinding
import com.example.myapitest.model.Car
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitCar
import com.example.myapitest.service.safeApiCall
import com.example.myapitest.ui.loadUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CarDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarDetailBinding
    private lateinit var car: Car

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_car_detail)
        setContentView(binding.root)
        setupView()
        loadCar()
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.deleteCTA.setOnClickListener {
            deleteCar()
        }
        binding.deleteCTA.setOnClickListener {
            editCar()
        }
    }

    private fun editCar(){
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall {
                RetrofitCar.apiService.updateCar(
                    car.id,
                    car.value.copy(binding.license.text.toString())
                )
            }
            withContext(Dispatchers.Main) {
                when (result){
                    is Result.Error -> {
                        Toast.makeText(
                            this@CarDetailActivity,
                            R.string.unknown_error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is Result.Success -> {
                        Toast.makeText(
                            this@CarDetailActivity,
                            R.string.success_update,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }


    private fun loadCar() {
        var carId = intent.getStringExtra(ARG_ID) ?: ""

        CoroutineScope(Dispatchers.IO).launch {

            val result = safeApiCall { RetrofitCar.apiService.getCarsId(carId) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {}
                    is Result.Success -> {
                        car = result.data
                        handleSuccess()
                    }
                }
            }
        }
    }

    private fun deleteCar() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitCar.apiService.deleteCar(car.id) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(
                            this@CarDetailActivity,
                            R.string.error_delete,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is Result.Success -> {
                        Toast.makeText(
                            this@CarDetailActivity,
                            R.string.success_delete,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun handleSuccess() {
        binding.name.text = car.value.name
        binding.year.text = car.value.year
        binding.license.setText(car.value.licence)
        binding.image.loadUrl(car.value.imageUrl)
    }


    companion object {
        private const val ARG_ID = "ARG_ID"

        fun newIntent(
            context: Context,
            carId: String
        ) =
            Intent(context, CarDetailActivity::class.java).apply {
                putExtra(ARG_ID, carId)
            }
    }
}