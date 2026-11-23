package com.example.parcial3_vitapaw.controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.example.parcial3_vitapaw.database.VitaPawDAO;
import com.example.parcial3_vitapaw.model.RegistroSalud;
import com.example.parcial3_vitapaw.services.RecordatorioReceiver;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SaludController {
    private final Context context;
    private final VitaPawDAO dao;

    public SaludController(Context context) {
        this.context = context;
        this.dao = new VitaPawDAO(context);
    }

    public String registrarCuidadoYNotificar(RegistroSalud registro, String nombreMascota) {
        // Validación estricta de datos
        if (registro.getTipo() == null || registro.getTipo().trim().isEmpty()) {
            return "Error: Seleccione el tipo de registro médico.";
        }
        if (registro.getDescripcion() == null || registro.getDescripcion().trim().isEmpty()) {
            return "Error: La descripción o nombre del cuidado es obligatorio.";
        }
        if (registro.getFechaAplicacion() == null || registro.getFechaAplicacion().trim().isEmpty()) {
            return "Error: Debe ingresar la fecha de aplicación.";
        }

        // Validar formato de fecha DD/MM/YYYY
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setLenient(false);
        try {
            Date appDate = sdf.parse(registro.getFechaAplicacion());
            if (appDate == null) {
                return "Error: Formato de fecha de aplicación inválido (use DD/MM/AAAA).";
            }
            if (registro.getFechaProximoRecordatorio() != null && !registro.getFechaProximoRecordatorio().trim().isEmpty()) {
                Date proxDate = sdf.parse(registro.getFechaProximoRecordatorio());
                if (proxDate != null && proxDate.before(appDate)) {
                    return "Error: La fecha del próximo recordatorio no puede ser anterior a la fecha de aplicación.";
                }
            }
        } catch (ParseException e) {
            return "Error: Formato de fecha inválido (use DD/MM/AAAA).";
        }

        // Guardar en SQLite
        long id = dao.insertarRegistroSalud(registro);
        if (id == -1) {
            return "Error al guardar el registro en la base de datos.";
        }

        // Programar recordatorio si tiene fecha futura
        if (registro.getFechaProximoRecordatorio() != null && !registro.getFechaProximoRecordatorio().trim().isEmpty()) {
            programarAlarma((int) id, nombreMascota, registro.getTipo(), registro.getDescripcion(), registro.getFechaProximoRecordatorio());
        }

        return "OK";
    }

    private void programarAlarma(int requestCode, String nombreMascota, String tipo, String descripcion, String fechaStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date fecha = sdf.parse(fechaStr);
            if (fecha == null) return;

            Calendar cal = Calendar.getInstance();
            cal.setTime(fecha);
            cal.set(Calendar.HOUR_OF_DAY, 9); // Notificar a las 9:00 AM
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);

            if (cal.getTimeInMillis() <= System.currentTimeMillis()) return;

            Intent intent = new Intent(context, RecordatorioReceiver.class);
            intent.putExtra("titulo", "Recordatorio VitaPaw: " + nombreMascota);
            intent.putExtra("mensaje", tipo + " programada: " + descripcion);
            intent.putExtra("notif_id", requestCode);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}