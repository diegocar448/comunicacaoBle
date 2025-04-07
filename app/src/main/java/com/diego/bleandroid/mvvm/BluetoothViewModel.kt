package com.diego.bleandroid.mvvm

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
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

import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.diego.bleandroid.utils.PermissionHelper


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

    //scanear e iniciar
    //Ei, você está chamando algo que precisa de permissão, mas não verificou se o usuário já concedeu
    //A partir do Android 12 (API 31), exigem permissão BLUETOOTH_SCAN em tempo de execução
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScan() {
        val context = getApplication<Application>().applicationContext

        if (!PermissionHelper.hasBluetoothScanPermission(context)) {
            Toast.makeText(context, "Permissão de scan Bluetooth não concedida", Toast.LENGTH_SHORT).show()
            return
        }

        stopScan()
        scannedDevices.clear()
        _devices.value = emptyList()
        scanner?.startScan(scanCallback)
        isScanning = true
    }



    //para de scanear
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        val context = getApplication<Application>().applicationContext

        if (!PermissionHelper.hasBluetoothScanPermission(context)) {
            Toast.makeText(context, "Permissão de scan Bluetooth não concedida", Toast.LENGTH_SHORT).show()
            return
        }

        if (isScanning) {
            scanner?.stopScan(scanCallback)
            isScanning = false
        }
    }


    private fun hasScanPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }



}

