package com.wiveb.agentplatform.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.koin.koinScreenModel
import com.wiveb.agentplatform.ui.theme.*

@Composable
fun SettingsScreen() {
    val model = koinScreenModel<SettingsScreenModel>()
    val baseUrl by model.baseUrl.collectAsState()
    val testResult by model.testResult.collectAsState()
    val testing by model.testing.collectAsState()
    var urlInput by remember(baseUrl) { mutableStateOf(baseUrl) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Settings", color = Gray100, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        // Backend URL
        Card(colors = CardDefaults.cardColors(containerColor = Gray900)) {
            Column(Modifier.padding(16.dp)) {
                Text("Backend URL", color = Gray400, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Gray100,
                        unfocusedTextColor = Gray100,
                        focusedBorderColor = Indigo600,
                        unfocusedBorderColor = Gray700,
                        cursorColor = Indigo400,
                    ),
                    shape = RoundedCornerShape(8.dp),
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { model.setBaseUrl(urlInput) },
                        enabled = urlInput != baseUrl,
                    ) {
                        Text("Save")
                    }
                    Button(
                        onClick = { model.testConnection() },
                        enabled = !testing,
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                    ) {
                        if (testing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Gray100,
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Test Connection")
                    }
                }

                if (testResult != null) {
                    Spacer(Modifier.height(8.dp))
                    val isOk = testResult!!.startsWith("Connected")
                    Text(
                        testResult!!,
                        color = if (isOk) Emerald400 else Red400,
                        fontSize = 13.sp,
                    )
                }
            }
        }

        // App info
        Card(colors = CardDefaults.cardColors(containerColor = Gray900)) {
            Column(Modifier.padding(16.dp)) {
                Text("About", color = Gray400, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                InfoRow("Version", "1.0.0")
                InfoRow("Platform", "Android (CMP)")
                InfoRow("Target API", "agents.home-server.com")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = Gray500, fontSize = 13.sp)
        Text(value, color = Gray300, fontSize = 13.sp)
    }
}
