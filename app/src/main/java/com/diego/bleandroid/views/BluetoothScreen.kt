package com.diego.bleandroid.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.diego.bleandroid.mvvm.BluetoothViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight


import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun BluetoothScreen(viewModel: BluetoothViewModel) {
    val context = LocalContext.current
    val devices by viewModel.devices.collectAsState()
    val isConnecting by viewModel.isConnecting.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.tryReconnectLastDevice()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.startScan()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // 🔄 Indicador de reconexão
        if (isConnecting) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reconectando...", modifier = Modifier.alignByBaseline())
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Botões de ação
        Button(
            enabled = !isConnecting,
            onClick = {
                when {
                    context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                        viewModel.startScan()
                    }
                    else -> {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            }
        ) {
            Text("Buscar Dispositivos")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.stopScan() }) {
            Text("Parar Busca")
        }

        if (devices.isEmpty() && !isConnecting) {
            Text("Nenhum dispositivo encontrado", color = Color.Gray)
        }


        Spacer(modifier = Modifier.height(16.dp))

        // Lista de dispositivos encontrados
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(devices) { device ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE0F7FA)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = device.name ?: "Dispositivo Desconhecido",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = device.address,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}



