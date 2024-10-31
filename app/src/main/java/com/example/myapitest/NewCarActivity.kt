package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.databinding.ActivityNewCarBinding
import com.example.myapitest.model.CarValue
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitCar
import com.example.myapitest.service.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom

class NewCarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewCarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.saveCta.setOnClickListener {
            save()
        }
    }

    private fun save() {
        if (!validateForm()) return
        CoroutineScope(Dispatchers.IO).launch {
            val id = SecureRandom().nextInt().toString()
            val carValue = CarValue(
                id,
                binding.model.text.toString(),
                binding.year.text.toString(),
                binding.license.text.toString(),
                binding.imageUrl.text.toString()
            )
            val result = safeApiCall { RetrofitCar.apiService.addCar(carValue) }
            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(
                            this@NewCarActivity,
                            R.string.error_create,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is Result.Success -> {
                        Toast.makeText(
                            this@NewCarActivity,
                            getString(R.string.success_create, result.data.id),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }


    private fun validateForm(): Boolean {
        if (binding.model.text.toString().isBlank()) {
            Toast.makeText(
                this,
                getString(R.string.error_validate_form, "Modelo"),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.year.text.toString().isBlank()) {
            Toast.makeText(this, getString(R.string.error_validate_form, "Ano"), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (binding.license.text.toString().isBlank()) {
            Toast.makeText(
                this,
                getString(R.string.error_validate_form, "Placa"),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.imageUrl.text.toString().isBlank()) {
            Toast.makeText(
                this,
                getString(R.string.error_validate_form, "Image Url"),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        return true
    }

    companion object {
        fun newIntent(context: Context) =
            Intent(context, NewCarActivity::class.java)
    }

}