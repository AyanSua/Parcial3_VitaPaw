package com.example.parcial3_vitapaw.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.parcial3_vitapaw.R;
import com.example.parcial3_vitapaw.controller.SaludController;
import com.example.parcial3_vitapaw.model.RegistroSalud;
import java.util.Calendar;
import java.util.Locale;

public class SaludFormActivity extends AppCompatActivity {

    private Spinner spTipo;
    private EditText etDescripcion, etFechaAplicacion, etFechaProximo, etNotas;
    private int mascotaId = -1;
    private String nombreMascota = "";
    private SaludController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salud_form);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Registrar Salud");
        }

        controller = new SaludController(this);
        mascotaId = getIntent().getIntExtra("mascota_id", -1);
        nombreMascota = getIntent().getStringExtra("mascota_nombre");
        if (nombreMascota == null) nombreMascota = "Mascota";

        spTipo = findViewById(R.id.spTipoRegistro);
        etDescripcion = findViewById(R.id.etDescripcionSalud);
        etFechaAplicacion = findViewById(R.id.etFechaAplicacion);
        etFechaProximo = findViewById(R.id.etFechaProximo);
        etNotas = findViewById(R.id.etNotasSalud);
        Button btnGuardar = findViewById(R.id.btnGuardarSalud);

        String[] opciones = {"Vacuna", "Desparasitación", "Consulta Veterinaria", "Tratamiento / Otro"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opciones);
        spTipo.setAdapter(spinnerAdapter);

        etFechaAplicacion.setOnClickListener(v -> mostrarDatePicker(etFechaAplicacion));
        etFechaProximo.setOnClickListener(v -> mostrarDatePicker(etFechaProximo));

        btnGuardar.setOnClickListener(v -> guardarRegistro());
    }

    private void guardarRegistro() {
        String tipo = spTipo.getSelectedItem() != null ? spTipo.getSelectedItem().toString() : "Cuidado";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";
        if (descripcion.isEmpty()) {
            etDescripcion.setError("La descripción o nombre del cuidado es obligatorio");
            etDescripcion.requestFocus();
            return;
        }

        String fechaAplicacion = etFechaAplicacion.getText() != null ? etFechaAplicacion.getText().toString().trim() : "";
        if (fechaAplicacion.isEmpty()) {
            etFechaAplicacion.setError("La fecha de aplicación es obligatoria");
            etFechaAplicacion.requestFocus();
            return;
        }

        String fechaProximo = etFechaProximo.getText() != null ? etFechaProximo.getText().toString().trim() : "";
        String notas = etNotas.getText() != null ? etNotas.getText().toString().trim() : "";

        RegistroSalud r = new RegistroSalud(0, mascotaId, tipo, descripcion, fechaAplicacion, fechaProximo, notas);
        String resultado = controller.registrarCuidadoYNotificar(r, nombreMascota);

        if ("OK".equals(resultado)) {
            Toast.makeText(this, "Registro de salud guardado con éxito", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, resultado, Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarDatePicker(EditText target) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String fecha = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
            target.setText(fecha);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}