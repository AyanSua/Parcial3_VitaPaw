package com.example.parcial3_vitapaw.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.parcial3_vitapaw.R;
import com.example.parcial3_vitapaw.model.RegistroSalud;
import java.util.List;

public class SaludAdapter extends RecyclerView.Adapter<SaludAdapter.ViewHolder> {

    public interface OnSaludDeleteListener {
        void onDeleteClick(RegistroSalud registro);
    }

    public interface OnSaludCompletarListener {
        void onCompletarClick(RegistroSalud registro);
    }

    private final List<RegistroSalud> lista;
    private final OnSaludDeleteListener deleteListener;
    private final OnSaludCompletarListener completarListener;

    // Constructor original (mantiene retrocompatibilidad)
    public SaludAdapter(List<RegistroSalud> lista, OnSaludDeleteListener deleteListener) {
        this(lista, deleteListener, null);
    }

    // Constructor extendido con botón de marcar como completado
    public SaludAdapter(List<RegistroSalud> lista, OnSaludDeleteListener deleteListener, OnSaludCompletarListener completarListener) {
        this.lista = lista;
        this.deleteListener = deleteListener;
        this.completarListener = completarListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_salud, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RegistroSalud r = lista.get(position);
        holder.tvDescripcion.setText(r.getDescripcion());
        holder.tvFechaAplicacion.setText("Aplicado: " + r.getFechaAplicacion());

        String tipo = r.getTipo() != null ? r.getTipo() : "Cuidado";
        holder.tvTipoBadge.setText(tipo.toUpperCase());

        // Badge y color dinámico de la barra lateral según categoría
        int colorBarraRes;
        if (tipo.equalsIgnoreCase("Vacuna")) {
            holder.tvTipoBadge.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_badge_vacuna));
            colorBarraRes = R.color.color_teal;
        } else if (tipo.equalsIgnoreCase("Desparasitación") || tipo.equalsIgnoreCase("Desparasitacion")) {
            holder.tvTipoBadge.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_badge_desparasitacion));
            colorBarraRes = R.color.color_sage;
        } else if (tipo.toLowerCase().contains("consulta") || tipo.toLowerCase().contains("urgencia")) {
            holder.tvTipoBadge.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_badge_consulta));
            colorBarraRes = R.color.color_rose;
        } else {
            holder.tvTipoBadge.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_badge_consulta));
            colorBarraRes = R.color.color_pine;
        }

        if (holder.viewBarraCategoria != null) {
            holder.viewBarraCategoria.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), colorBarraRes)
            );
        }

        // Control de visibilidad de próximo recordatorio y botón "Marcar como aplicado hoy"
        boolean tieneRecordatorio = r.getFechaProximoRecordatorio() != null && !r.getFechaProximoRecordatorio().trim().isEmpty();
        if (tieneRecordatorio) {
            holder.tvFechaProximo.setVisibility(View.VISIBLE);
            holder.tvFechaProximo.setText("⏰ Próximo: " + r.getFechaProximoRecordatorio());

            if (holder.btnCompletar != null) {
                holder.btnCompletar.setVisibility(completarListener != null ? View.VISIBLE : View.GONE);
                holder.btnCompletar.setOnClickListener(v -> {
                    if (completarListener != null) {
                        completarListener.onCompletarClick(r);
                    }
                });
            }
        } else {
            holder.tvFechaProximo.setVisibility(View.GONE);
            if (holder.btnCompletar != null) {
                holder.btnCompletar.setVisibility(View.GONE);
            }
        }

        if (r.getNotas() != null && !r.getNotas().trim().isEmpty()) {
            holder.tvNotas.setVisibility(View.VISIBLE);
            holder.tvNotas.setText("Notas: " + r.getNotas());
        } else {
            holder.tvNotas.setVisibility(View.GONE);
        }

        holder.btnEliminar.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(r);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewBarraCategoria;
        TextView tvTipoBadge, tvDescripcion, tvFechaAplicacion, tvFechaProximo, tvNotas;
        ImageView btnEliminar;
        Button btnCompletar;

        ViewHolder(View itemView) {
            super(itemView);
            viewBarraCategoria = itemView.findViewById(R.id.viewBarraCategoria);
            tvTipoBadge = itemView.findViewById(R.id.tvTipoBadge);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionSalud);
            tvFechaAplicacion = itemView.findViewById(R.id.tvFechaAplicacion);
            tvFechaProximo = itemView.findViewById(R.id.tvFechaProximo);
            tvNotas = itemView.findViewById(R.id.tvNotasSalud);
            btnEliminar = itemView.findViewById(R.id.btnEliminarSaludRecord);
            btnCompletar = itemView.findViewById(R.id.btnCompletarRecordatorio);
        }
    }
}