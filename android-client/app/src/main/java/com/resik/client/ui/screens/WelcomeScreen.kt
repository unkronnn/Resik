package com.resik.client.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resik.client.R
import com.resik.client.ui.theme.*

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(GreenDark, GreenPrimary, GreenLight)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            // Logo
            Image(
                painter = painterResource(R.drawable.ic_logo_large),
                contentDescription = "RESIK Logo",
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(BgWhite)
                    .padding(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("RESIK", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = BgWhite, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Bank Sampah Digital", fontSize = 16.sp, color = GreenMint)

            Spacer(modifier = Modifier.weight(1f))

            Text(
                "Kelola sampah, hemat lingkungan,\ndapatkan poin reward",
                fontSize = 14.sp,
                color = GreenMint,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BgWhite)
            ) {
                Text("Mulai", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
