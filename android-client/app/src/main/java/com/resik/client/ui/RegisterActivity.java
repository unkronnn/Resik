package com.resik.client.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.resik.client.R;
import com.resik.client.api.ApiClient;
import com.resik.client.model.AuthResponse;
import com.resik.client.model.RegisterRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText namaField = findViewById(R.id.namaField);
        EditText emailField = findViewById(R.id.emailField);
        EditText passwordField = findViewById(R.id.passwordField);
        EditText alamatField = findViewById(R.id.alamatField);
        EditText telpField = findViewById(R.id.telpField);
        Button registerBtn = findViewById(R.id.registerBtn);
        TextView loginLink = findViewById(R.id.loginLink);

        loginLink.setOnClickListener(v -> finish());

        registerBtn.setOnClickListener(v -> {
            String nama = namaField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String alamat = alamatField.getText().toString().trim();
            String telp = telpField.getText().toString().trim();

            if (nama.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi semua field wajib", Toast.LENGTH_SHORT).show();
                return;
            }

            RegisterRequest req = new RegisterRequest(nama, email, password, alamat, telp);
            ApiClient.getApi().register(req).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AuthResponse auth = response.body();
                        ApiClient.saveToken(auth.getToken());
                        ApiClient.saveUserId(auth.getUserId());
                        Toast.makeText(RegisterActivity.this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Email sudah terdaftar", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this, "Koneksi gagal", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
