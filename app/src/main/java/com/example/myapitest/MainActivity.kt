package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapitest.adapter.CarAdapter
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.model.Car
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitCar
import com.example.myapitest.service.safeApiCall
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()

        // 1- Criar tela de Login com algum provedor do Firebase (Telefone, Google)
        //      Cadastrar o Seguinte celular para login de test: +5511912345678
        //      Código de verificação: 101010

        // 2- Criar Opção de Logout no aplicativo

        // 3- Integrar API REST /car no aplicativo
        //      API será disponibilida no Github
        //      JSON Necessário para salvar e exibir no aplicativo
        //      O Image Url deve ser uma foto armazenada no Firebase Storage
        //      { "id": "001", "imageUrl":"https://image", "year":"2020/2020", "name":"Gaspar", "licence":"ABC-1234", "place": {"lat": 0, "long": 0} }

        // Opcionalmente trabalhar com o Google Maps ara enviar o place
    }

    override fun onResume() {
        super.onResume()
        fetchItems()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean{
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                onLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = LoginActivity.newIntent(this)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            fetchItems()
        }
        binding.addCta.setOnClickListener {
            startActivity(NewCarActivity.newIntent(this))
        }
    }



    private fun fetchItems() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitCar.apiService.getCars() }

            Log.d("API Response", result.toString())


            withContext(Dispatchers.Main) {
                binding.swipeRefreshLayout.isRefreshing = false
                when (result) {
                    is Result.Error -> {
                        Log.e("API Error", "Error fetching data: ${result.message}")
                        Toast.makeText(this@MainActivity, R.string.error_fetching_data, Toast.LENGTH_SHORT).show()
                    }
                    is Result.Success -> handleOnSuccess(result.data)
                }
            }
        }
    }

    private fun handleOnSuccess(data: List<Car>) {
        Log.d("Data List", data.toString())
        val adapter = CarAdapter(data) {
            // listener do item clicado
            startActivity(
                CarDetailActivity.newIntent(
                    this,
                    it.id
                )
            )
        }
        binding.recyclerView.adapter = adapter
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

}

