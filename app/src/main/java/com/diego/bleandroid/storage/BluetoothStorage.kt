package com.diego.bleandroid.storage

import android.content.Context

object BluetoothStorage {
    private const val PREF_NAME = "ble_prefs"
    private const val KEY_LAST_DEVICE_NAME = "last_device_name"
    private const val KEY_LAST_DEVICE_ADDRESS = "last_device_address"

    fun saveLastConnectedDevice(context: Context, name: String?, address: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_LAST_DEVICE_NAME, name)
            putString(KEY_LAST_DEVICE_ADDRESS, address)
            apply()
        }
    }

    fun getLastConnectedDevice(context: Context): Pair<String?, String>? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_LAST_DEVICE_NAME, null)
        val address = prefs.getString(KEY_LAST_DEVICE_ADDRESS, null)
        return if (address != null) Pair(name, address) else null
    }
}
