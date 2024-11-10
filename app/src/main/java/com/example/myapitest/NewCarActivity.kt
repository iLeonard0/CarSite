package com.example.myapitest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.myapitest.databinding.ActivityNewCarBinding
import com.example.myapitest.model.Car
import com.example.myapitest.model.Place
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitCar
import com.example.myapitest.service.safeApiCall
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import java.security.SecureRandom

class NewCarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewCarBinding
    private lateinit var imageUri: Uri
    private var imageFile: File? = null

    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            binding.imageUrl.setText("Imagem Obtida")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Solicitar permissões em tempo de execução
        requestPermissions()

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

        binding.takePictureCta.setOnClickListener {
            takePicture()
        }
    }

    private fun requestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(android.Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                CAMERA_REQUEST_CODE
            )
        }
    }

    private fun takePicture() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            requestPermissions()  // Solicita permissões de câmera, caso necessário
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = createImageUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    private fun createImageUri(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

        return FileProvider.getUriForFile(
            this,
            "com.example.myapitest.fileprovider",
            imageFile!!
        )
    }


    private fun uploadImageToFirebase() {
        // Inicializar o Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference

        // criar uma referência para o arquivo no Firebase
        val imagesRef = storageRef.child("${UUID.randomUUID()}.jpg")
        // converter o Bitmap para ByteArrayOutputStream
        val baos = ByteArrayOutputStream()
        val imageBitmap = BitmapFactory.decodeFile(imageFile!!.path)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        // Desabilita botões para evitar duplo click
        binding.loadImageProgress.visibility = View.VISIBLE
        binding.takePictureCta.isEnabled = false
        binding.saveCta.isEnabled = false
        imagesRef.putBytes(data)
            .addOnFailureListener {
                binding.loadImageProgress.visibility = View.GONE
                binding.takePictureCta.isEnabled = true
                binding.saveCta.isEnabled = true
                Toast.makeText(this, "Falha ao realizar o upload", Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener {
                binding.loadImageProgress.visibility = View.GONE
                binding.takePictureCta.isEnabled = true
                binding.saveCta.isEnabled = true
                imagesRef.downloadUrl.addOnSuccessListener { uri ->
                    saveData(uri.toString())
                }
            }
    }


    private fun save() {
        if (!validateForm()) return
        uploadImageToFirebase()
    }

    private fun saveData(imageUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val id = SecureRandom().nextInt().toString()
            val carValue = Car(
                id,
                binding.name.text.toString(),
                binding.year.text.toString(),
                binding.license.text.toString(),
                imageUrl,
                Place(0.0, .0)
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
        if (binding.name.text.toString().isBlank()) {
            Toast.makeText(
                this,
                getString(R.string.error_validate_form, "Nome"),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.year.text.toString().isBlank()) {
            Toast.makeText(
                this,
                getString(R.string.error_validate_form, "Ano"),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.license.text.toString().isBlank()) {
            Toast.makeText(this, getString(R.string.error_validate_form, "Placa"), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (imageFile == null) {
            Toast.makeText(
                this,
                getString(R.string.error_validate_take_picture),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }


    companion object {
        private const val CAMERA_REQUEST_CODE = 101

        fun newIntent(context: Context) =
            Intent(context, NewCarActivity::class.java)
    }
}
