package com.example.parcial3_vitapaw.controller;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.example.parcial3_vitapaw.model.Mascota;
import com.example.parcial3_vitapaw.model.RegistroSalud;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PdfCarnetGenerator {

    public static void generarYCompartirCarnet(Context context, Mascota mascota, List<RegistroSalud> historial) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // Tamaño A4
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.rgb(30, 58, 138));
        titlePaint.setTextSize(22f);
        titlePaint.setFakeBoldText(true);

        Paint headerPaint = new Paint();
        headerPaint.setColor(Color.BLACK);
        headerPaint.setTextSize(14f);
        headerPaint.setFakeBoldText(true);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(12f);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStrokeWidth(1.5f);

        int y = 50;
        canvas.drawText("VITAPAW - CARNET DIGITAL DE SALUD VETERINARIA", 40, y, titlePaint);
        y += 30;
        canvas.drawLine(40, y, 555, y, linePaint);
        y += 25;

        canvas.drawText("DATOS DEL PACIENTE", 40, y, headerPaint);
        y += 20;
        canvas.drawText("Nombre: " + mascota.getNombre() + "   |   Especie: " + mascota.getEspecie(), 40, y, textPaint);
        y += 18;
        canvas.drawText("Raza: " + mascota.getRaza() + "   |   Peso actual: " + mascota.getPeso() + " kg", 40, y, textPaint);
        y += 18;
        canvas.drawText("Nacimiento: " + mascota.getFechaNacimiento() + "   |   Esterilizado: " + (mascota.isEsterilizado() ? "Sí" : "No"), 40, y, textPaint);
        y += 18;
        canvas.drawText("Microchip: " + (mascota.getMicrochip().isEmpty() ? "No registrado" : mascota.getMicrochip()), 40, y, textPaint);
        y += 18;

        Paint alertPaint = new Paint();
        alertPaint.setColor(Color.rgb(185, 28, 28));
        alertPaint.setTextSize(12f);
        alertPaint.setFakeBoldText(true);
        canvas.drawText("Alergias / Condiciones: " + (mascota.getAlergias().isEmpty() ? "Ninguna reportada" : mascota.getAlergias()), 40, y, alertPaint);

        y += 25;
        canvas.drawLine(40, y, 555, y, linePaint);
        y += 25;

        canvas.drawText("HISTORIAL DE VACUNAS, DESPARASITACIÓN Y CONSULTAS", 40, y, headerPaint);
        y += 25;

        if (historial.isEmpty()) {
            canvas.drawText("No hay registros médicos almacenados aún.", 40, y, textPaint);
        } else {
            for (RegistroSalud r : historial) {
                if (y > 780) break;
                canvas.drawText("• [" + r.getTipo() + "] " + r.getDescripcion() + " — Fecha: " + r.getFechaAplicacion(), 40, y, headerPaint);
                y += 16;
                String detalle = "   Próximo refuerzo: " + (r.getFechaProximoRecordatorio().isEmpty() ? "N/A" : r.getFechaProximoRecordatorio());
                if (!r.getNotas().isEmpty()) detalle += " | Notas: " + r.getNotas();
                canvas.drawText(detalle, 40, y, textPaint);
                y += 24;
            }
        }

        document.finishPage(page);

        File pdfFile = new File(context.getCacheDir(), "Carnet_" + mascota.getNombre() + ".pdf");
        try (FileOutputStream out = new FileOutputStream(pdfFile)) {
            document.writeTo(out);
            document.close();

            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(intent, "Abrir Carnet PDF con..."));
        } catch (IOException e) {
            Toast.makeText(context, "Error generando PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}