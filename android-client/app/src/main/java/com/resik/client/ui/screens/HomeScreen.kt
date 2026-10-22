package com.resik.client.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resik.client.R
import com.resik.client.ui.theme.*

data class QuickMenuItem(val label: String, val icon: ImageVector, val bgColor: Color, val onClick: () -> Unit)

@Composable
fun HomeScreen(
    userName: String,
    saldo: String,
    poin: String,
    onLogout: () -> Unit,
    onNavEdukasi: () -> Unit,
    onNavPenjemputan: () -> Unit,
    onNavRiwayat: () -> Unit,
    onNavChat: () -> Unit,
    onNavMap: () -> Unit,
    onNavQr: () -> Unit,
    onNavDompet: () -> Unit,
    onNavLeaderboard: () -> Unit,
    onNavProfil: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        // ===== Header dengan gradient hijau =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(GreenDark, GreenPrimary)))
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)
        ) {
            // Top bar: greeting + logout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_logo_small),
                        contentDescription = "Resik",
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(BgWhite).padding(4.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Selamat Datang", fontSize = 13.sp, color = GreenMint)
                        Text(userName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BgWhite)
                    }
                }
                IconButton(onClick = onLogout, modifier = Modifier.size(40.dp).clip(CircleShape).background(RedLogout.copy(alpha = 0.15f))) {
                    Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = RedLogout, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(16.dp))

            // ===== Balance card (seperti Gopay) =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgWhite)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Saldo", fontSize = 12.sp, color = TextSecondary)
                        }
                        Text(saldo, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                    }
                    Box(modifier = Modifier.width(1.dp).height(44.dp).background(BorderLight))
                    Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Poin", fontSize = 12.sp, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(poin, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)
                            Text(" pts", fontSize = 13.sp, color = OrangeAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ===== 4 Quick Menu buttons (seperti Gopay) =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgWhite)
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickMenuButton("Jemput", Icons.Filled.LocalShipping, GreenPrimary, onNavPenjemputan)
            QuickMenuButton("Edukasi", Icons.Filled.MenuBook, OrangeAccent, onNavEdukasi)
            QuickMenuButton("Dompet", Icons.Filled.AccountBalanceWallet, Color(0xFF8B5CF6), onNavDompet)
            QuickMenuButton("Scan QR", Icons.Filled.QrCodeScanner, Color(0xFF3B82F6), onNavQr)
        }

        // ===== Menu tambahan =====
        Column(modifier = Modifier.weight(1f).padding(20.dp)) {
            Text("Layanan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(12.dp))

            val extraMenus = listOf(
                QuickMenuItem("Riwayat", Icons.Filled.ReceiptLong, Color(0xFFDBEAFE), onNavRiwayat),
                QuickMenuItem("Lokasi", Icons.Filled.LocationOn, Color(0xFFFCE7F3), onNavMap),
                QuickMenuItem("Leaderboard", Icons.Filled.EmojiEvents, Color(0xFFFEF3C7), onNavLeaderboard),
                QuickMenuItem("Chat Admin", Icons.Filled.Chat, GreenMint, onNavChat),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                extraMenus.take(4).forEach { item ->
                    ExtraMenuCard(item, modifier = Modifier.weight(1f))
                }
            }
        }

        // ===== Bottom Navigation Bar =====
        BottomNav(
            activeTab = "home",
            onHome = { },
            onRiwayat = onNavRiwayat,
            onChat = onNavChat,
            onProfil = onNavProfil,
        )
    }
}

@Composable
fun QuickMenuButton(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() }.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
    }
}

@Composable
fun ExtraMenuCard(item: QuickMenuItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clickable { item.onClick() }.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(item.bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, contentDescription = item.label, tint = TextPrimary, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(item.label, fontSize = 10.sp, color = TextSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
fun BottomNav(
    activeTab: String,
    onHome: () -> Unit,
    onRiwayat: () -> Unit,
    onChat: () -> Unit,
    onProfil: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(BgWhite),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val tabs = listOf(
            Triple("home", Icons.Filled.Home, "Home"),
            Triple("riwayat", Icons.Filled.ReceiptLong, "Riwayat"),
            Triple("chat", Icons.Filled.Chat, "Chat"),
            Triple("profil", Icons.Filled.Person, "Profil"),
        )
        tabs.forEach { (id, icon, label) ->
            val isActive = activeTab == id
            Column(
                modifier = Modifier.clickable {
                    when (id) {
                        "home" -> onHome()
                        "riwayat" -> onRiwayat()
                        "chat" -> onChat()
                        "profil" -> onProfil()
                    }
                }.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (isActive) GreenPrimary else TextHint,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    label,
                    fontSize = 10.sp,
                    color = if (isActive) GreenPrimary else TextHint,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
