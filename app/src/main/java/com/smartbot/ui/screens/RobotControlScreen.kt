package com.smartbot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartbot.ui.viewmodel.MainViewModel

@Composable
fun RobotControlScreen(viewModel: MainViewModel) {
    val appState by viewModel.appState.collectAsState()
    val robotStatus = appState.robotStatus
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Robot Control",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            ConnectionStatusCard(
                isConnected = appState.isConnectedToRobot,
                onConnect = { /* TODO: Connect to robot */ },
                onDisconnect = { /* TODO: Disconnect from robot */ }
            )
        }
        
        item {
            RobotStatusCard(
                batteryLevel = robotStatus.batteryLevel,
                isMoving = robotStatus.isMoving,
                lastCommand = robotStatus.lastCommand
            )
        }
        
        item {
            DirectionalControlCard(
                onCommand = { command ->
                    viewModel.sendRobotCommand(command)
                }
            )
        }
        
        item {
            MotorControlCard(
                leftSpeed = robotStatus.motorLeftSpeed,
                rightSpeed = robotStatus.motorRightSpeed,
                onSpeedChange = { left, right ->
                    viewModel.sendRobotCommand("MOTOR:$left,$right")
                }
            )
        }
        
        item {
            QuickCommandsCard(
                onCommand = { command ->
                    viewModel.sendRobotCommand(command)
                }
            )
        }
    }
}

@Composable
fun ConnectionStatusCard(
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled,
                    contentDescription = "Connection Status",
                    tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ESP32 Robot",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isConnected) "Connected" else "Disconnected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
            
            if (isConnected) {
                OutlinedButton(onClick = onDisconnect) {
                    Text("Disconnect")
                }
            } else {
                Button(onClick = onConnect) {
                    Text("Connect")
                }
            }
        }
    }
}

@Composable
fun RobotStatusCard(
    batteryLevel: Int,
    isMoving: Boolean,
    lastCommand: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Robot Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Battery",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$batteryLevel%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isMoving) "Moving" else "Idle",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (isMoving) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column {
                    Text(
                        text = "Last Command",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = lastCommand.ifEmpty { "None" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun DirectionalControlCard(onCommand: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Directional Control",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Forward button
            FilledIconButton(
                onClick = { onCommand("FORWARD") },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Forward",
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Left, Stop, Right buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilledIconButton(
                    onClick = { onCommand("LEFT") },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Left",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                FilledTonalIconButton(
                    onClick = { onCommand("STOP") },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                FilledIconButton(
                    onClick = { onCommand("RIGHT") },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Right",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Backward button
            FilledIconButton(
                onClick = { onCommand("BACKWARD") },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Backward",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun MotorControlCard(
    leftSpeed: Int,
    rightSpeed: Int,
    onSpeedChange: (Int, Int) -> Unit
) {
    var leftSpeedSlider by remember { mutableFloatStateOf(leftSpeed.toFloat()) }
    var rightSpeedSlider by remember { mutableFloatStateOf(rightSpeed.toFloat()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Motor Control",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Left motor
            Text(
                text = "Left Motor: ${leftSpeedSlider.toInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = leftSpeedSlider,
                onValueChange = { leftSpeedSlider = it },
                onValueChangeFinished = {
                    onSpeedChange(leftSpeedSlider.toInt(), rightSpeedSlider.toInt())
                },
                valueRange = -100f..100f,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Right motor
            Text(
                text = "Right Motor: ${rightSpeedSlider.toInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = rightSpeedSlider,
                onValueChange = { rightSpeedSlider = it },
                onValueChangeFinished = {
                    onSpeedChange(leftSpeedSlider.toInt(), rightSpeedSlider.toInt())
                },
                valueRange = -100f..100f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun QuickCommandsCard(onCommand: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Commands",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onCommand("DANCE") }
                ) {
                    Text("Dance")
                }
                
                Button(
                    onClick = { onCommand("SPIN") }
                ) {
                    Text("Spin")
                }
                
                Button(
                    onClick = { onCommand("PATROL") }
                ) {
                    Text("Patrol")
                }
            }
        }
    }
}