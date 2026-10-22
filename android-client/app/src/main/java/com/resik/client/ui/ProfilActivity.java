package com.resik.client.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.resik.client.R;
import com.resik.client.api.ApiClient;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        TextView namaUser = findViewById(R.id.namaUser);
        TextView emailUser = findViewById(R.id.emailUser);
        TextView alamatUser = findViewById(R.id.alamatUser);
        TextView telpUser = findViewById(R.id.telpUser);
        TextView roleUser = findViewById(R.id.roleUser);
        Button editBtn = findViewById(R.id.editBtn);
        Button logoutBtn = findViewById(R.id.logoutBtn);
        LinearLayout btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        editBtn.setOnClickListener(v -> {
            // TODO: implement edit profile
        });
        logoutBtn.setOnClickListener(v -> {
            ApiClient.logout();
            startActivity(new Intent(this, WelcomeActivity.class));
            finishAffinity();
        });

        ApiClient.getApi().getMe(ApiClient.getAuthHeader()).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> user = response.body();
                    namaUser.setText(String.valueOf(user.get("nama")));
                    emailUser.setText(String.valueOf(user.get("email")));
                    alamatUser.setText(String.valueOf(user.get("alamat")));
                    telpUser.setText(String.valueOf(user.get("noTelp")));
                    Object role = user.get("role");
                    roleUser.setText(role != null ? String.valueOf(role) : "MASYARAKAT");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                namaUser.setText("Error loading");
            }
        });
    }
}
