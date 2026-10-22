package com.resik.client.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.resik.client.R;
import com.resik.client.api.ApiClient;
import com.resik.client.model.Penjemputan;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PenjemputanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penjemputan);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        ApiClient.getApi().getMyPenjemputan(ApiClient.getAuthHeader())
                .enqueue(new Callback<List<Penjemputan>>() {
                    @Override
                    public void onResponse(Call<List<Penjemputan>> call, Response<List<Penjemputan>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            PenjemputanAdapter adapter = new PenjemputanAdapter(response.body());
                            recyclerView.setLayoutManager(new LinearLayoutManager(PenjemputanActivity.this));
                            recyclerView.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Penjemputan>> call, Throwable t) {
                        TextView err = new TextView(PenjemputanActivity.this);
                        err.setText("Gagal memuat: " + t.getMessage());
                    }
                });
    }

    static class PenjemputanAdapter extends RecyclerView.Adapter<PenjemputanAdapter.VH> {
        private final List<Penjemputan> items;
        PenjemputanAdapter(List<Penjemputan> items) { this.items = items; }

        @Override
        public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setPadding(32, 24, 32, 24);
            tv.setTextSize(13);
            return new VH(tv);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            Penjemputan p = items.get(position);
            holder.text.setText(String.format("#%d | %s\nTanggal: %s\nStatus: %s\nQR: %s",
                    p.getId(), p.getAlamat(), p.getTanggalJemput(), p.getStatus(), p.getQrCode()));
        }

        @Override
        public int getItemCount() { return items != null ? items.size() : 0; }

        static class VH extends RecyclerView.ViewHolder {
            TextView text;
            VH(android.view.View v) { super(v); text = (TextView) v; }
        }
    }
}
