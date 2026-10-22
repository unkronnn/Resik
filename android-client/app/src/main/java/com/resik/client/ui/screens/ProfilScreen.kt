package com.resik.client.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import com.resik.client.R
import com.resik.client.ui.theme.*

@Composable
fun ProfilScreen(
    nama: String, email: String, alamat: String, telp: String, role: String,
    onBack: () -> Unit, onLogout: () -> Unit,
) {
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var editMode by remember { mutableStateOf(false) }
    var editNama by remember { mutableStateOf(nama) }
    var editTelp by remember { mutableStateOf(telp) }
    var editAlamat by remember { mutableStateOf(alamat) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
    }

    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        // Header with gradient
        Column(
            modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(GreenDark, GreenPrimary))).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = BgWhite) }
                IconButton(onClick = { editMode = !editMode }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = BgWhite)
                }
            }

            // Profile photo with upload
            Box(contentAlignment = Alignment.BottomEnd) {
                if (photoUri != null) {
                    val bitmap = remember(photoUri) {
                        photoUri?.let { uri ->
                            // Can't easily load in pure Compose without Coil
                            null
                        }
                    }
                    Box(
                        modifier = Modifier.size(96.dp).clip(CircleShape).background(BgWhite.copy(alpha = 0.2f)).clickable { photoPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = BgWhite, modifier = Modifier.size(44.dp))
                    }
                } else {
                    Image(
                        painter = painterResource(R.drawable.ic_logo),
                        contentDescription = "Resik Logo",
                        modifier = Modifier.size(96.dp).clip(CircleShape).background(BgWhite).padding(8.dp)
                    )
                }
                // Camera button overlay
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(GreenLight).clickable { photoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Upload", tint = BgWhite, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(if (editMode) editNama else nama, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BgWhite)
            Text(role, fontSize = 12.sp, color = GreenMint)
        }

        // Info section
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Informasi Akun", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BgWhite)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Email (read-only)
                    InfoRow(Icons.Filled.Email, "Email", email, Color(0xFFE0F2FE), Color(0xFF3B82F6))
                    Spacer(Modifier.height(16.dp))

                    if (editMode) {
                        // Editable fields
                        Text("Nama", fontSize = 12.sp, color = TextSecondary)
                        OutlinedTextField(
                            value = editNama, onValueChange = { editNama = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = BorderLight)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("No. Telepon", fontSize = 12.sp, color = TextSecondary)
                        OutlinedTextField(
                            value = editTelp, onValueChange = { editTelp = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = BorderLight)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Alamat", fontSize = 12.sp, color = TextSecondary)
                        OutlinedTextField(
                            value = editAlamat, onValueChange = { editAlamat = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = BorderLight)
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { editMode = false },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Simpan Perubahan", color = BgWhite, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        InfoRow(Icons.Filled.Person, "Nama", nama, GreenMint, GreenPrimary)
                        Spacer(Modifier.height(16.dp))
                        InfoRow(Icons.Filled.Phone, "No. Telepon", telp, OrangeLight, OrangeAccent)
                        Spacer(Modifier.height(16.dp))
                        InfoRow(Icons.Filled.LocationOn, "Alamat", alamat, Color(0xFFFCE7F3), Color(0xFFEC4899))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedLight, contentColor = RedLogout)
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String, bgColor: Color, iconTint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(bgColor), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = TextSecondary)
            Text(value, fontSize = 14.sp, color = TextPrimary)
        }
    }
}
