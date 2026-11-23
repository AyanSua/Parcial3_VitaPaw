package com.example.parcial3_vitapaw.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.parcial3_vitapaw.R;
import com.example.parcial3_vitapaw.controller.BackupManager;
import com.example.parcial3_vitapaw.database.VitaPawDAO;
import com.example.parcial3_vitapaw.model.Mascota;
import com.example.parcial3_vitapaw.model.RegistroSalud;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvMascotas;
    private TextView tvAgendaGlobal, tvStatsHero;
    private View llEmptyState;
    private VitaPawDAO dao;

    private final ActivityResultLauncher<String> exportLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/octet-stream"), uri -> {
                if (uri != null) BackupManager.exportarBaseDatos(this, uri);
            });

    private final ActivityResultLauncher<String[]> importLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    BackupManager.importarBaseDatos(this, uri);
                    cargarDatos();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("VitaPaw - Salud y Cuidado");

        solicitarPermisoNotificaciones();

        dao = new VitaPawDAO(this);
        rvMascotas = findViewById(R.id.rvMascotas);
        tvAgendaGlobal = findViewById(R.id.tvAgendaGlobal);
        tvStatsHero = findViewById(R.id.tvStatsHero);
        llEmptyState = findViewById(R.id.llEmptyState);
        rvMascotas.setLayoutManager(new LinearLayoutManager(this));

        View btnEmergencia = findViewById(R.id.btnEmergenciaMaps);
        View btnCalculadora = findViewById(R.id.btnCalculadora);
        View btnBackup = findViewById(R.id.btnBackup);
        View fab = findViewById(R.id.fabAgregarMascota);

        // Animación de entrada en cascada para los botones principales
        animarEntradaCascada(btnEmergencia, 100);
        animarEntradaCascada(btnCalculadora, 200);
        animarEntradaCascada(btnBackup, 300);
        animarEntradaCascada(fab, 400);

        // Eventos con efecto rebote táctil
        btnEmergencia.setOnClickListener(v -> animarClickYEjecutar(v, () -> {
            // Busca veterinarias cerca de la ubicación actual del teléfono
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=veterinaria+24+horas+cerca+de+mi");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Si Google Maps está instalado lo abre directo; si no, abre el navegador centrado en tu zona
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Uri webUri = Uri.parse("https://www.google.com/maps/search/veterinaria+24+horas/");
                startActivity(new Intent(Intent.ACTION_VIEW, webUri));
            }
        }));

        btnCalculadora.setOnClickListener(v -> animarClickYEjecutar(v, () ->
                startActivity(new Intent(this, CalculadoraAlimentoActivity.class))));

        btnBackup.setOnClickListener(v -> animarClickYEjecutar(v, this::mostrarOpcionesBackup));

        fab.setOnClickListener(v -> animarClickYEjecutar(v, () ->
                startActivity(new Intent(this, MascotaFormActivity.class))));
    }

    /**
     * Hace que el botón entre suavemente desde abajo con un leve efecto elástico.
     */
    private void animarEntradaCascada(View vista, long delayMs) {
        vista.setAlpha(0f);
        vista.setTranslationY(40f);
        vista.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(450)
                .setStartDelay(delayMs)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();
    }

    /**
     * Simula presión física (se encoge y rebota) antes de ejecutar la acción.
     */
    private void animarClickYEjecutar(View vista, Runnable accion) {
        vista.animate()
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(90)
                .withEndAction(() -> vista.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(110)
                        .setInterpolator(new OvershootInterpolator(2f))
                        .withEndAction(accion)
                        .start())
                .start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatos();
    }

    private void cargarDatos() {
        List<Mascota> mascotas = dao.obtenerTodasMascotas();
        if (mascotas.isEmpty()) {
            rvMascotas.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
            tvStatsHero.setText("0 mascotas registradas • Sin pacientes activos");
        } else {
            rvMascotas.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
            tvStatsHero.setText(mascotas.size() + " mascota(s) registrada(s) bajo cuidado activo");
        }

        MascotaAdapter adapter = new MascotaAdapter(mascotas, mascota -> {
            Intent intent = new Intent(MainActivity.this, MascotaDetalleActivity.class);
            intent.putExtra("mascota_id", mascota.getId());
            startActivity(intent);
        });
        rvMascotas.setAdapter(adapter);

        List<RegistroSalud> proximos = dao.obtenerProximosCuidadosGlobales();
        if (proximos.isEmpty()) {
            tvAgendaGlobal.setText("No hay cuidados próximos programados.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (RegistroSalud r : proximos) {
                sb.append("• ").append(r.getNombreMascota()).append(": ")
                        .append(r.getTipo()).append(" (").append(r.getDescripcion()).append(") → ")
                        .append(r.getFechaProximoRecordatorio()).append("\n");
            }
            tvAgendaGlobal.setText(sb.toString().trim());
        }
    }

    private void mostrarOpcionesBackup() {
        String[] opciones = {"Exportar copia de seguridad (.db)", "Restaurar copia de seguridad"};
        new AlertDialog.Builder(this)
                .setTitle("Respaldo de Base de Datos")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        exportLauncher.launch("respaldo_vitapaw.db");
                    } else {
                        importLauncher.launch(new String[]{"*/*"});
                    }
                })
                .show();
    }

    private void solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
}