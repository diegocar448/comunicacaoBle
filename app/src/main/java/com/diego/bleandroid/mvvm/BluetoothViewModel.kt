package com.diego.bleandroid.mvvm

import android.app.Application
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult


class BluetoothViewModel(application: Application) : AndroidViewModel(application) {

    private val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val scanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner

    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothDevice>> = _devices

    private val scannedDevices = mutableSetOf<BluetoothDevice>()

    private var isScanning = false


    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (scannedDevices.add(device)) {
                _devices.value = scannedDevices.toList().sortedBy { it.name ?: "z" }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Toast.makeText(getApplication(), "Erro no scan: $errorCode", Toast.LENGTH_SHORT).show()
        }
    }

    fun enableBluetooth(context: Context) {
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth não suportado", Toast.LENGTH_LONG).show()
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (context is android.app.Activity) {
                context.startActivityForResult(enableBtIntent, 1)
            }

        }
    }

    fun startScan() {
        stopScan() // para qualquer scan em andamento

        scannedDevices.clear()
        _devices.value = emptyList()
        scanner?.startScan(scanCallback)
        isScanning = true
        println("StartScan AQUI")
    }


    fun stopScan() {
        if (isScanning) {
            println("StopScan FDP")
            scanner?.stopScan(scanCallback)
            isScanning = false
        }

    }


//    fun startScan() {
//        scannedDevices.clear()
//        _devices.value = emptyList()
//        scanner?.startScan(scanCallback)
//    }

//    fun stopScan() {
//        scanner?.stopScan(scanCallback)
//    }
}

