package com.resik.client.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.resik.client.R;
import com.resik.client.api.ApiClient;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView userName, saldoText, poinText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userName = findViewById(R.id.userName);
        saldoText = findViewById(R.id.saldoText);
        poinText = findViewById(R.id.poinText);

        loadUserData();

        LinearLayout navEdukasi = findViewById(R.id.navEdukasi);
        LinearLayout navPenjemputan = findViewById(R.id.navPenjemputan);
        LinearLayout navRiwayat = findViewById(R.id.navRiwayat);
        LinearLayout navChat = findViewById(R.id.navChat);
        LinearLayout navMap = findViewById(R.id.navMap);
        LinearLayout navQr = findViewById(R.id.navQr);

        navEdukasi.setOnClickListener(v -> startActivity(new Intent(this, EdukasiActivity.class)));
        navPenjemputan.setOnClickListener(v -> startActivity(new Intent(this, PenjemputanActivity.class)));
        navRiwayat.setOnClickListener(v -> startActivity(new Intent(this, RiwayatActivity.class)));
        navChat.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));
        navMap.setOnClickListener(v -> startActivity(new Intent(this, MapActivity.class)));
        navQr.setOnClickListener(v -> startActivity(new Intent(this, QrScannerActivity.class)));

        findViewById(R.id.logoutBtn).setOnClickListener(v -> {
            ApiClient.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void loadUserData() {
        ApiClient.getApi().getMe(ApiClient.getAuthHeader()).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> user = response.body();
                    userName.setText(String.valueOf(user.get("nama")));
                    Object saldo = user.get("saldoDompet");
                    saldoText.setText("Rp " + (saldo != null ? saldo : "0"));
                    Object poin = user.get("poinReward");
                    poinText.setText(String.valueOf(poin != null ? poin : "0") + " poin");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                userName.setText("Error loading data");
            }
        });
    }
}
