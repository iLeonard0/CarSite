package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
        binding.editCTA.setOnClickListener {
            editCar()
        }
    }

    private fun editCar() {
        CoroutineScope(Dispatchers.IO).launch {
            val updatedCar =
                car.copy(licence = binding.license.text.toString()) // Atualize o carro com o novo dado
            val result = safeApiCall {
                RetrofitCar.apiService.updateCar(car.id, updatedCar)
            }
            withContext(Dispatchers.Main) {
                when (result) {
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
                        // AQUI: NOTIFICAR A MAIN ACTIVITY PARA ATUALIZAR A LISTA
                        val intent = Intent(this@CarDetailActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Limpa a pilha de atividades
                        startActivity(intent)
                        finish() // Finaliza a activity atual
                    }
                }
            }
        }
    }


    private fun loadCar() {
        val carId = intent.getStringExtra(ARG_ID) ?: ""
        Log.d("CarDetailActivity", "Car ID: $carId") // Log para verificar o ID

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitCar.apiService.getCarsId(carId).value }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Log.e("CarDetailActivity", "Error fetching car details: ${result.message}")
                    }

                    is Result.Success -> {
                        car = result.data
                        Log.d(
                            "CarDetailActivity",
                            "Fetched car: $car"
                        ) // Log para verificar o carro
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
        binding.name.text = car.name
        binding.year.text = car.year
        binding.license.setText(car.licence)

        if (car.imageUrl.isNotEmpty()) {
            binding.image.loadUrl(car.imageUrl)
        } else {
            binding.image.setImageResource(R.drawable.ic_error)
        }
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