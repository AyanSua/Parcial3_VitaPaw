package com.example.parcial3_vitapaw.controller;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;
import com.example.parcial3_vitapaw.database.DatabaseHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class BackupManager {

    public static void exportarBaseDatos(Context context, Uri destinoUri) {
        File dbFile = context.getDatabasePath(DatabaseHelper.DATABASE_NAME);
        try (InputStream in = new FileInputStream(dbFile);
             OutputStream out = context.getContentResolver().openOutputStream(destinoUri)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            Toast.makeText(context, "Copia de seguridad guardada con éxito", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Error al exportar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void importarBaseDatos(Context context, Uri origenUri) {
        File dbFile = context.getDatabasePath(DatabaseHelper.DATABASE_NAME);
        try (InputStream in = context.getContentResolver().openInputStream(origenUri);
             OutputStream out = new FileOutputStream(dbFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            Toast.makeText(context, "Base de datos restaurada correctamente", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Error al restaurar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}