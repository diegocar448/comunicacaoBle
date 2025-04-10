package com.diego.bleandroid.mvvm

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.diego.bleandroid.utils.PermissionHelper
import com.diego.bleandroid.storage.BluetoothStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*



class BluetoothViewModel(application: Application) : AndroidViewModel(application) {

    private val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val scanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner

    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothDevice>> = _devices

    private val scannedDevices = mutableSetOf<BluetoothDevice>()

    private var isScanning = false

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting

    private val _connectionStatus = MutableStateFlow<String>("")
    val connectionStatus: StateFlow<String> = _connectionStatus


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

    @SuppressLint("MissingPermission")
    fun saveConnectedDevice(device: BluetoothDevice) {
        val context = getApplication<Application>().applicationContext
        BluetoothStorage.saveLastConnectedDevice(context, device.name, device.address)
    }

    fun tryReconnectLastDevice() {
        val context = getApplication<Application>().applicationContext
        val lastDeviceInfo = BluetoothStorage.getLastConnectedDevice(context)
        val adapter = bluetoothAdapter ?: return

        lastDeviceInfo?.let { (_, address) ->
            val device = adapter.getRemoteDevice(address)

            _isConnecting.value = true
            Toast.makeText(context, "Tentando reconectar com ${device.name ?: "Desconhecido"}", Toast.LENGTH_SHORT).show()

            // Simula um delay de conexão
            viewModelScope.launch {
                delay(6000) // espera 2 segundos

                // Aqui você colocaria a lógica real de conexão BLE
                _isConnecting.value = false

                // Simulando que deu certo:
                saveConnectedDevice(device)
                Toast.makeText(context, "Reconectado com sucesso!", Toast.LENGTH_SHORT).show()
            }

        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        val context = getApplication<Application>().applicationContext

        _isConnecting.value = true
        _connectionStatus.value = "Conectando ao ${device.name ?: "desconhecido"}..."

        device.connectGatt(context, false, object : BluetoothGattCallback() {

            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        _connectionStatus.value = "Conectado a ${device.name}"
                        _isConnecting.value = false
                        saveConnectedDevice(device)
                        gatt?.discoverServices() // Inicia descoberta dos serviços
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        _connectionStatus.value = "Desconectado de ${device.name}"
                        _isConnecting.value = false
                    }
                    else -> {
                        _connectionStatus.value = "Status desconhecido: $newState"
                        _isConnecting.value = false
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    _connectionStatus.value = "Serviços descobertos com sucesso!"
                    // Aqui você pode navegar pelos serviços/características
                } else {
                    _connectionStatus.value = "Erro ao descobrir serviços"
                }
            }
        })


    }


}
