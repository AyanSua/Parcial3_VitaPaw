package com.example.parcial3_vitapaw.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.parcial3_vitapaw.R;
import com.example.parcial3_vitapaw.database.VitaPawDAO;
import com.example.parcial3_vitapaw.model.Mascota;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MascotaFormActivity extends AppCompatActivity {

    private ImageView ivFoto;
    private EditText etNombre, etEspecie, etRaza, etFechaNac, etPeso, etAlergias, etMicrochip;
    private CheckBox cbEsterilizado;
    private String fotoUriSeleccionada = "";
    private int mascotaIdEditar = -1;
    private VitaPawDAO dao;

    private final ActivityResultLauncher<String[]> selectorImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    fotoUriSeleccionada = uri.toString();
                    ivFoto.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mascota_form);

        dao = new VitaPawDAO(this);
        ivFoto = findViewById(R.id.ivFotoMascota);
        etNombre = findViewById(R.id.etNombre);
        etEspecie = findViewById(R.id.etEspecie);
        etRaza = findViewById(R.id.etRaza);
        etFechaNac = findViewById(R.id.etFechaNac);
        etPeso = findViewById(R.id.etPeso);
        etAlergias = findViewById(R.id.etAlergias);
        etMicrochip = findViewById(R.id.etMicrochip);
        cbEsterilizado = findViewById(R.id.cbEsterilizado);
        Button btnFoto = findViewById(R.id.btnSeleccionarFoto);
        Button btnGuardar = findViewById(R.id.btnGuardarMascota);

        etFechaNac.setOnClickListener(v -> mostrarDatePicker(etFechaNac));
        btnFoto.setOnClickListener(v -> selectorImagenLauncher.launch(new String[]{"image/*"}));

        if (getIntent().hasExtra("mascota_id")) {
            mascotaIdEditar = getIntent().getIntExtra("mascota_id", -1);
            setTitle("Modificar Mascota");
            cargarDatosEdicion();
        } else {
            setTitle("Registrar Nueva Mascota");
        }

        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void cargarDatosEdicion() {
        Mascota m = dao.obtenerMascotaPorId(mascotaIdEditar);
        if (m != null) {
            etNombre.setText(m.getNombre());
            etEspecie.setText(m.getEspecie());
            etRaza.setText(m.getRaza());
            etFechaNac.setText(m.getFechaNacimiento());
            etPeso.setText(m.getPeso() > 0 ? String.valueOf(m.getPeso()) : "");
            etAlergias.setText(m.getAlergias());
            etMicrochip.setText(m.getMicrochip());
            cbEsterilizado.setChecked(m.isEsterilizado());
            fotoUriSeleccionada = m.getFotoUri();
            if (fotoUriSeleccionada != null && !fotoUriSeleccionada.isEmpty()) {
                ivFoto.setImageURI(Uri.parse(fotoUriSeleccionada));
            }
        }
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        if (nombre.isEmpty() || nombre.length() < 2) {
            etNombre.setError("El nombre debe tener al menos 2 caracteres");
            etNombre.requestFocus();
            return;
        }

        String especie = etEspecie.getText().toString().trim();
        if (especie.isEmpty() || especie.length() < 2) {
            etEspecie.setError("La especie es obligatoria (Ej: Perro, Gato)");
            etEspecie.requestFocus();
            return;
        }
        // Validación: No permitir números en la especie
        if (especie.matches(".*\\d.*")) {
            etEspecie.setError("La especie no puede contener números");
            etEspecie.requestFocus();
            return;
        }

        String raza = etRaza.getText().toString().trim();
        // Validación: No permitir números en la raza (solo si el usuario escribió algo)
        if (!raza.isEmpty() && raza.matches(".*\\d.*")) {
            etRaza.setError("La raza no puede contener números");
            etRaza.requestFocus();
            return;
        }

        String fechaNac = etFechaNac.getText().toString().trim();
        if (!fechaNac.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setLenient(false);
            try {
                Date d = sdf.parse(fechaNac);
                if (d != null && d.after(new Date())) {
                    etFechaNac.setError("La fecha no puede ser futura");
                    etFechaNac.requestFocus();
                    return;
                }
                etFechaNac.setError(null);
            } catch (ParseException e) {
                etFechaNac.setError("Formato de fecha inválido (DD/MM/AAAA)");
                etFechaNac.requestFocus();
                return;
            }
        }

        double peso = 0.0;
        String pesoStr = etPeso.getText().toString().trim().replace(",", ".");
        if (!pesoStr.isEmpty()) {
            try {
                peso = Double.parseDouble(pesoStr);
                if (peso <= 0 || peso > 500) {
                    etPeso.setError("Ingresa un peso válido entre 0.1 y 500 kg");
                    etPeso.requestFocus();
                    return;
                }
                etPeso.setError(null);
            } catch (NumberFormatException e) {
                etPeso.setError("Formato de peso numérico inválido");
                etPeso.requestFocus();
                return;
            }
        }

        // Usamos la variable 'raza' que ya limpiamos y validamos
        Mascota m = new Mascota(
                mascotaIdEditar,
                nombre,
                especie,
                raza,
                fechaNac,
                peso,
                fotoUriSeleccionada,
                etAlergias.getText().toString().trim(),
                etMicrochip.getText().toString().trim(),
                cbEsterilizado.isChecked()
        );

        if (mascotaIdEditar == -1) {
            dao.insertarMascota(m);
            Toast.makeText(this, "Mascota registrada con éxito", Toast.LENGTH_SHORT).show();
        } else {
            dao.actualizarMascota(m);
            Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void mostrarDatePicker(EditText target) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String fecha = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
            target.setText(fecha);
            target.setError(null); // Limpiamos cualquier error previo al seleccionar una nueva fecha
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        // Bloquea las fechas futuras directamente en el componente visual
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
}