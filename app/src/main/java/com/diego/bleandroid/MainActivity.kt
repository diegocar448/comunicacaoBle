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


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher



class MainActivity : ComponentActivity() {

    private lateinit var bluetoothEnableLauncher: ActivityResultLauncher<Intent>
    private lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o adaptador Bluetooth
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Lança a Intent para ativar o Bluetooth
        bluetoothEnableLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val isEnabled = bluetoothAdapter.isEnabled
            if (!isEnabled) {
                // Usuário recusou → pede de novo com jeitinho
                Toast.makeText(this, "Bluetooth é necessário para continuar", Toast.LENGTH_LONG).show()
                askToEnableBluetooth()
            }
        }

        // Solicita permissão de localização
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val locationPermissionRequest = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (!isGranted) {
                    Toast.makeText(this, "Permissão de localização é necessária", Toast.LENGTH_LONG).show()
                }
            }

            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Checa e solicita Bluetooth já na abertura
        askToEnableBluetooth()

        val viewModel = BluetoothViewModel(application)
        setContent {
            BluetoothScreen(viewModel)
        }
    }

    private fun askToEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothEnableLauncher.launch(enableBtIntent)
        }
    }
}
