package com.smartbot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartbot.ui.viewmodel.MainViewModel
import kotlin.math.abs

@Composable
fun SensorsScreen(viewModel: MainViewModel) {
    val appState by viewModel.appState.collectAsState()
    val sensorData = appState.sensorData
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Sensor Data",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            SensorCard(
                title = "Accelerometer",
                icon = Icons.Default.Speed,
                values = listOf(
                    "X: ${String.format("%.2f", sensorData.accelerometerX)} m/s²",
                    "Y: ${String.format("%.2f", sensorData.accelerometerY)} m/s²",
                    "Z: ${String.format("%.2f", sensorData.accelerometerZ)} m/s²"
                )
            )
        }
        
        item {
            SensorCard(
                title = "Gyroscope",
                icon = Icons.Default.RotateRight,
                values = listOf(
                    "X: ${String.format("%.2f", sensorData.gyroscopeX)} rad/s",
                    "Y: ${String.format("%.2f", sensorData.gyroscopeY)} rad/s",
                    "Z: ${String.format("%.2f", sensorData.gyroscopeZ)} rad/s"
                )
            )
        }
        
        item {
            SensorCard(
                title = "Magnetometer",
                icon = Icons.Default.Explore,
                values = listOf(
                    "X: ${String.format("%.2f", sensorData.magnetometerX)} μT",
                    "Y: ${String.format("%.2f", sensorData.magnetometerY)} μT",
                    "Z: ${String.format("%.2f", sensorData.magnetometerZ)} μT"
                )
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Motion Analysis",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Motion Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val totalAcceleration = kotlin.math.sqrt(
                        sensorData.accelerometerX * sensorData.accelerometerX +
                        sensorData.accelerometerY * sensorData.accelerometerY +
                        sensorData.accelerometerZ * sensorData.accelerometerZ
                    )
                    
                    val isMoving = abs(totalAcceleration - 9.81f) > 2.0f
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Device Status",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isMoving) "Moving" else "Stationary",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = if (isMoving) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Total Acceleration",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format("%.2f", totalAcceleration)} m/s²",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Sensor Controls",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { /* TODO: Calibrate sensors */ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Calibrate",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Calibrate")
                        }
                        
                        OutlinedButton(
                            onClick = { /* TODO: Reset sensors */ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensorCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    values: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            values.forEach { value ->
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}