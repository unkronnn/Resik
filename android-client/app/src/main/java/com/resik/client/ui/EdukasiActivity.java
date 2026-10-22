package com.resik.client.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.resik.client.R;
import com.resik.client.api.ApiClient;
import com.resik.client.model.Sampah;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EdukasiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edukasi);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        ApiClient.getApi().getAllSampah().enqueue(new Callback<List<Sampah>>() {
            @Override
            public void onResponse(Call<List<Sampah>> call, Response<List<Sampah>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Sampah> list = response.body();
                    EdukasiAdapter adapter = new EdukasiAdapter(list);
                    recyclerView.setLayoutManager(new LinearLayoutManager(EdukasiActivity.this));
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Sampah>> call, Throwable t) {
                TextView empty = new TextView(EdukasiActivity.this);
                empty.setText("Gagal memuat data. Pastikan server berjalan.");
                ((LinearLayout) recyclerView.getParent()).addView(empty);
            }
        });
    }

    static class EdukasiAdapter extends RecyclerView.Adapter<EdukasiAdapter.VH> {
        private final List<Sampah> items;
        EdukasiAdapter(List<Sampah> items) { this.items = items; }

        @Override
        public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setPadding(32, 32, 32, 32);
            tv.setTextSize(14);
            return new VH(tv);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            Sampah s = items.get(position);
            holder.text.setText(String.format("%s (%s)\nHarga: Rp %s/kg\n%s",
                    s.getNama(), s.getKategori(), s.getHargaPerKg(), s.getDeskripsi()));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView text;
            VH(View v) { super(v); text = (TextView) v; }
        }
    }
}
