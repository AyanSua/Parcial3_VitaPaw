package com.example.parcial3_vitapaw.ui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.parcial3_vitapaw.R;
import com.example.parcial3_vitapaw.model.Mascota;
import java.util.List;

public class MascotaAdapter extends RecyclerView.Adapter<MascotaAdapter.ViewHolder> {

    public interface OnMascotaClickListener {
        void onMascotaClick(Mascota mascota);
    }

    private final List<Mascota> lista;
    private final OnMascotaClickListener listener;

    public MascotaAdapter(List<Mascota> lista, OnMascotaClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mascota, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mascota m = lista.get(position);
        holder.tvNombre.setText(m.getNombre());
        
        String razaStr = (m.getRaza() != null && !m.getRaza().isEmpty()) ? " • " + m.getRaza() : "";
        holder.tvDetalle.setText(m.getEspecie() + razaStr + " • " + m.getPeso() + " kg");

        if (m.getFotoUri() != null && !m.getFotoUri().isEmpty()) {
            try {
                holder.ivFoto.setImageURI(Uri.parse(m.getFotoUri()));
                holder.ivFoto.setPadding(0, 0, 0, 0);
            } catch (Exception e) {
                holder.ivFoto.setImageResource(R.drawable.ic_paw);
            }
        } else {
            holder.ivFoto.setImageResource(R.drawable.ic_paw);
        }

        holder.itemView.setOnClickListener(v -> listener.onMascotaClick(m));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;
        TextView tvNombre, tvDetalle;

        ViewHolder(View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivFotoItem);
            tvNombre = itemView.findViewById(R.id.tvNombreItem);
            tvDetalle = itemView.findViewById(R.id.tvDetalleItem);
        }
    }
}