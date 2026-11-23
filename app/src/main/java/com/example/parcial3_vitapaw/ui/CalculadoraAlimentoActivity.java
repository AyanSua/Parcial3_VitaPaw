package com.example.parcial3_vitapaw.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.parcial3_vitapaw.R;
import java.util.Locale;

public class CalculadoraAlimentoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculadora_alimento);
        setTitle("Nutrición VitaPaw");

        EditText etPeso = findViewById(R.id.etPesoCalc);
        Spinner spEtapa = findViewById(R.id.spEtapaCalc);
        Button btnCalcular = findViewById(R.id.btnCalcularRacion);
        TextView tvResultado = findViewById(R.id.tvResultadoRacion);

        String[] etapas = {
                "Cachorro (4% - 5% del peso)",
                "Adulto Activo (2.5% del peso)",
                "Adulto Sedentario / Esterilizado (2% del peso)",
                "Senior / Mayor (1.8% del peso)"
        };
        spEtapa.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, etapas));

        btnCalcular.setOnClickListener(v -> {
            String pesoStr = etPeso.getText().toString().trim();
            if (pesoStr.isEmpty()) {
                etPeso.setError("Ingresa el peso en kg");
                etPeso.requestFocus();
                return;
            }

            double pesoKg;
            try {
                pesoKg = Double.parseDouble(pesoStr);
                if (pesoKg <= 0 || pesoKg > 500) {
                    etPeso.setError("Ingresa un peso válido entre 0.1 y 500 kg");
                    etPeso.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etPeso.setError("Formato de peso inválido");
                etPeso.requestFocus();
                return;
            }

            double porcentaje;
            switch (spEtapa.getSelectedItemPosition()) {
                case 0: porcentaje = 0.045; break;
                case 1: porcentaje = 0.025; break;
                case 2: porcentaje = 0.020; break;
                default: porcentaje = 0.018; break;
            }
            double gramosDiarios = pesoKg * 1000 * porcentaje;
            tvResultado.setText(String.format(Locale.getDefault(),
                    "Ración recomendada: %.0f gramos al día.\n(Dividir en 2 o 3 comidas de %.0f g cada una)",
                    gramosDiarios, gramosDiarios / 2.0));
        });
    }
}