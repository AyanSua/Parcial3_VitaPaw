package com.example.parcial3_vitapaw.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.parcial3_vitapaw.R;
import com.example.parcial3_vitapaw.controller.PdfCarnetGenerator;
import com.example.parcial3_vitapaw.database.VitaPawDAO;
import com.example.parcial3_vitapaw.model.Mascota;
import com.example.parcial3_vitapaw.model.RegistroSalud;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MascotaDetalleActivity extends AppCompatActivity {

    private int mascotaId;
    private Mascota mascotaActual;
    private VitaPawDAO dao;
    private ImageView ivFoto;
    private TextView tvNombre, tvInfo, tvAlerta;
    private RecyclerView rvHistorial;
    private EditText etBuscar;
    private Spinner spFiltro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mascota_detalle);
        setTitle("Expediente de Mascota");

        dao = new VitaPawDAO(this);
        mascotaId = getIntent().getIntExtra("mascota_id", -1);

        ivFoto = findViewById(R.id.ivDetalleFoto);
        tvNombre = findViewById(R.id.tvDetalleNombre);
        tvInfo = findViewById(R.id.tvDetalleInfo);
        tvAlerta = findViewById(R.id.tvAlertaEmergencia);
        rvHistorial = findViewById(R.id.rvHistorialSalud);
        etBuscar = findViewById(R.id.etBuscarHistorial);
        spFiltro = findViewById(R.id.spFiltroTipo);

        rvHistorial.setLayoutManager(new LinearLayoutManager(this));

        String[] filtros = {"Todos", "Vacuna", "Desparasitación", "Consulta Veterinaria"};
        spFiltro.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, filtros));

        spFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actualizarListaHistorial();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                actualizarListaHistorial();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Configurar Checklist de Bienestar Mensual
        configurarChecklistBienestar(mascotaId);

        // Eventos de botones con efecto táctil dinámico
        findViewById(R.id.btnEditarMascota).setOnClickListener(v -> animarClickYEjecutar(v, () -> {
            Intent i = new Intent(this, MascotaFormActivity.class);
            i.putExtra("mascota_id", mascotaId);
            startActivity(i);
        }));

        findViewById(R.id.btnRegistrarPeso).setOnClickListener(v -> animarClickYEjecutar(v, this::mostrarDialogoPeso));

        findViewById(R.id.btnExportarPdf).setOnClickListener(v -> animarClickYEjecutar(v, () -> {
            List<RegistroSalud> todoHistorial = dao.obtenerHistorialFiltrado(mascotaId, "Todos", "");
            PdfCarnetGenerator.generarYCompartirCarnet(this, mascotaActual, todoHistorial);
        }));

        findViewById(R.id.btnEliminarMascota).setOnClickListener(v -> animarClickYEjecutar(v, this::confirmarEliminacion));

        findViewById(R.id.btnNuevoRegistroSalud).setOnClickListener(v -> animarClickYEjecutar(v, () -> {
            Intent i = new Intent(this, SaludFormActivity.class);
            i.putExtra("mascota_id", mascotaId);
            i.putExtra("mascota_nombre", mascotaActual.getNombre());
            startActivity(i);
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPerfil();
        actualizarListaHistorial();
    }

    private void cargarPerfil() {
        mascotaActual = dao.obtenerMascotaPorId(mascotaId);
        if (mascotaActual == null) {
            finish();
            return;
        }

        tvNombre.setText(mascotaActual.getNombre());

        String edadYEtapa = calcularEdadYEtapa(mascotaActual.getFechaNacimiento());
        String infoBase = mascotaActual.getEspecie() +
                (mascotaActual.getRaza() != null && !mascotaActual.getRaza().isEmpty() ? " • " + mascotaActual.getRaza() : "");

        tvInfo.setText(infoBase +
                "\n" + edadYEtapa +
                "\nPeso: " + mascotaActual.getPeso() + " kg | Esterilizado: " + (mascotaActual.isEsterilizado() ? "Sí" : "No"));

        String alerta = "";
        if (mascotaActual.getAlergias() != null && !mascotaActual.getAlergias().isEmpty()) {
            alerta += "⚠️ Alergias: " + mascotaActual.getAlergias() + " ";
        }
        if (mascotaActual.getMicrochip() != null && !mascotaActual.getMicrochip().isEmpty()) {
            alerta += (alerta.isEmpty() ? "" : " | ") + "Chip: " + mascotaActual.getMicrochip();
        }
        tvAlerta.setText(alerta.isEmpty() ? "✓ Sin alertas médicas registradas" : alerta);

        if (mascotaActual.getFotoUri() != null && !mascotaActual.getFotoUri().isEmpty()) {
            ivFoto.setImageURI(Uri.parse(mascotaActual.getFotoUri()));
        }
    }

    private String calcularEdadYEtapa(String fechaNacStr) {
        if (fechaNacStr == null || fechaNacStr.isEmpty()) return "Edad no registrada";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date fechaNac = sdf.parse(fechaNacStr);
            if (fechaNac == null) return "Edad no registrada";

            Calendar nac = Calendar.getInstance();
            nac.setTime(fechaNac);
            Calendar hoy = Calendar.getInstance();

            int anios = hoy.get(Calendar.YEAR) - nac.get(Calendar.YEAR);
            int meses = hoy.get(Calendar.MONTH) - nac.get(Calendar.MONTH);

            if (meses < 0 || (meses == 0 && hoy.get(Calendar.DAY_OF_MONTH) < nac.get(Calendar.DAY_OF_MONTH))) {
                anios--;
                meses += 12;
            }

            String etapa = (anios < 1) ? "🍼 Cachorro" : (anios >= 7 ? "🌿 Senior" : "🐾 Adulto");
            if (anios <= 0) {
                return Math.max(0, meses) + " mes(es) • " + etapa;
            }
            return anios + " año(s), " + meses + " mes(es) • " + etapa;
        } catch (Exception e) {
            return "Edad no registrada";
        }
    }

    private void configurarChecklistBienestar(int idMascota) {
        ProgressBar pb = findViewById(R.id.pbBienestar);
        TextView tvProg = findViewById(R.id.tvProgresoBienestar);
        CheckBox cbBano = findViewById(R.id.cbBano);
        CheckBox cbUnas = findViewById(R.id.cbUnas);
        CheckBox cbOidos = findViewById(R.id.cbOidos);
        CheckBox cbPipeta = findViewById(R.id.cbPipeta);

        // Protección por si aún no se actualiza el XML
        if (pb == null || cbBano == null) return;

        SharedPreferences prefs = getSharedPreferences("Bienestar_" + idMascota, MODE_PRIVATE);
        CheckBox[] boxes = {cbBano, cbUnas, cbOidos, cbPipeta};
        String[] keys = {"bano", "unas", "oidos", "pipeta"};

        Runnable actualizarBarra = () -> {
            int completados = 0;
            SharedPreferences.Editor editor = prefs.edit();
            for (int i = 0; i < boxes.length; i++) {
                if (boxes[i].isChecked()) completados++;
                editor.putBoolean(keys[i], boxes[i].isChecked());
            }
            editor.apply();
            pb.setProgress(completados);
            if (tvProg != null) tvProg.setText(completados + "/4");
        };

        for (int i = 0; i < boxes.length; i++) {
            boxes[i].setChecked(prefs.getBoolean(keys[i], false));
            boxes[i].setOnCheckedChangeListener((buttonView, isChecked) -> actualizarBarra.run());
        }
        actualizarBarra.run();
    }

    private void actualizarListaHistorial() {
        String filtroSeleccionado = spFiltro.getSelectedItem() != null ? spFiltro.getSelectedItem().toString() : "Todos";
        String texto = etBuscar.getText().toString();

        List<RegistroSalud> registros = dao.obtenerHistorialFiltrado(mascotaId, filtroSeleccionado, texto);
        SaludAdapter adapter = new SaludAdapter(registros, this::confirmarEliminacionSalud);
        rvHistorial.setAdapter(adapter);
    }

    private void confirmarEliminacionSalud(RegistroSalud registro) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Registro")
                .setMessage("¿Seguro que deseas eliminar este registro de " + registro.getDescripcion() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    dao.eliminarRegistroSalud(registro.getId());
                    Toast.makeText(this, "Registro eliminado", Toast.LENGTH_SHORT).show();
                    cargarPerfil();
                    actualizarListaHistorial();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoPeso() {
        List<String> historialPeso = dao.obtenerHistorialPeso(mascotaId);

        // Parsear pesos y fechas para alimentar la gráfica
        List<Float> listaPesos = new ArrayList<>();
        List<String> listaFechas = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (String h : historialPeso) {
            sb.append("• ").append(h).append("\n");
            try {
                // Extrae números y fechas asumiendo formato tipo "12.5 kg - 19/11/2026" o similar
                String[] partes = h.split("-");
                String numStr = partes[0].replaceAll("[^0-9.,]", "").replace(",", ".");
                if (!numStr.isEmpty()) {
                    listaPesos.add(Float.parseFloat(numStr));
                    String fechaCorta = partes.length > 1 ? partes[1].trim() : "";
                    if (fechaCorta.length() >= 5) fechaCorta = fechaCorta.substring(0, 5); // dd/MM
                    listaFechas.add(fechaCorta);
                }
            } catch (Exception ignored) {}
        }

        // Si el DAO devuelve del más reciente al más antiguo, invertimos para graficar cronológicamente
        Collections.reverse(listaPesos);
        Collections.reverse(listaFechas);

        LinearLayout layoutDialogo = new LinearLayout(this);
        layoutDialogo.setOrientation(LinearLayout.VERTICAL);
        layoutDialogo.setPadding(48, 24, 48, 12);

        if (!listaPesos.isEmpty()) {
            GraficaPesoView grafica = new GraficaPesoView(this, null);
            LinearLayout.LayoutParams paramsGrafica = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 380);
            paramsGrafica.bottomMargin = 24;
            grafica.setLayoutParams(paramsGrafica);
            grafica.setDatos(listaPesos, listaFechas);
            layoutDialogo.addView(grafica);
        }

        EditText inputPeso = new EditText(this);
        inputPeso.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputPeso.setHint("Nuevo peso en kg (Ej: 14.2)");
        layoutDialogo.addView(inputPeso);

        new AlertDialog.Builder(this)
                .setTitle("📈 Control y Evolución de Peso")
                .setMessage(historialPeso.isEmpty() ? "Aún no hay pesajes registrados." : sb.toString().trim())
                .setView(layoutDialogo)
                .setPositiveButton("Registrar Peso", (dialog, which) -> {
                    String val = inputPeso.getText().toString().trim().replace(",", ".");
                    if (!val.isEmpty()) {
                        try {
                            double nuevoPeso = Double.parseDouble(val);
                            if (nuevoPeso <= 0 || nuevoPeso > 500) {
                                Toast.makeText(this, "Peso inválido (debe estar entre 0.1 y 500 kg)", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            dao.registrarPeso(mascotaId, nuevoPeso);
                            cargarPerfil();
                            actualizarListaHistorial();
                            Toast.makeText(this, "Peso actualizado con éxito", Toast.LENGTH_SHORT).show();
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Por favor ingrese un número válido", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void confirmarEliminacion() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Mascota")
                .setMessage("¿Seguro que deseas eliminar a " + mascotaActual.getNombre() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    dao.eliminarMascota(mascotaId);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void animarClickYEjecutar(View vista, Runnable accion) {
        vista.animate()
                .scaleX(0.93f)
                .scaleY(0.93f)
                .setDuration(80)
                .withEndAction(() -> vista.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .setInterpolator(new OvershootInterpolator(2f))
                        .withEndAction(accion)
                        .start())
                .start();
    }


}