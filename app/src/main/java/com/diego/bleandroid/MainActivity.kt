package com.diego.bleandroid

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.diego.bleandroid.mvvm.BluetoothViewModel
import com.diego.bleandroid.views.BluetoothScreen

class MainActivity : ComponentActivity() {

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Permissão de localização é obrigatória para BLE", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        // Pede permissão no Android 6+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }




        val viewModel = BluetoothViewModel(application)

        setContent {
            BluetoothScreen(viewModel)
        }
    }
}
