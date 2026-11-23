package com.example.parcial3_vitapaw.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.parcial3_vitapaw.model.Mascota;
import com.example.parcial3_vitapaw.model.RegistroSalud;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VitaPawDAO {
    private final DatabaseHelper dbHelper;

    public VitaPawDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long insertarMascota(Mascota m) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", m.getNombre());
        values.put("especie", m.getEspecie());
        values.put("raza", m.getRaza());
        values.put("fecha_nacimiento", m.getFechaNacimiento());
        values.put("peso", m.getPeso());
        values.put("foto_uri", m.getFotoUri());
        values.put("alergias", m.getAlergias());
        values.put("microchip", m.getMicrochip());
        values.put("esterilizado", m.isEsterilizado() ? 1 : 0);
        long id = db.insert(DatabaseHelper.TABLE_MASCOTAS, null, values);
        if (id != -1 && m.getPeso() > 0) {
            registrarPeso((int) id, m.getPeso());
        }
        return id;
    }

    public int actualizarMascota(Mascota m) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", m.getNombre());
        values.put("especie", m.getEspecie());
        values.put("raza", m.getRaza());
        values.put("fecha_nacimiento", m.getFechaNacimiento());
        values.put("peso", m.getPeso());
        values.put("foto_uri", m.getFotoUri());
        values.put("alergias", m.getAlergias());
        values.put("microchip", m.getMicrochip());
        values.put("esterilizado", m.isEsterilizado() ? 1 : 0);
        return db.update(DatabaseHelper.TABLE_MASCOTAS, values, "id = ?", new String[]{String.valueOf(m.getId())});
    }

    public void eliminarMascota(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_PESO, "mascota_id = ?", new String[]{String.valueOf(id)});
        db.delete(DatabaseHelper.TABLE_SALUD, "mascota_id = ?", new String[]{String.valueOf(id)});
        db.delete(DatabaseHelper.TABLE_MASCOTAS, "id = ?", new String[]{String.valueOf(id)});
    }

    public List<Mascota> obtenerTodasMascotas() {
        List<Mascota> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_MASCOTAS + " ORDER BY nombre ASC", null);
        if (c.moveToFirst()) {
            do {
                lista.add(cursorAMascota(c));
            } while (c.moveToNext());
        }
        c.close();
        return lista;
    }

    public Mascota obtenerMascotaPorId(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_MASCOTAS + " WHERE id = ?", new String[]{String.valueOf(id)});
        Mascota m = c.moveToFirst() ? cursorAMascota(c) : null;
        c.close();
        return m;
    }

    private Mascota cursorAMascota(Cursor c) {
        return new Mascota(
                c.getInt(0), c.getString(1), c.getString(2), c.getString(3),
                c.getString(4), c.getDouble(5), c.getString(6),
                c.getString(7), c.getString(8), c.getInt(9) == 1
        );
    }

    // --- HISTORIAL DE PESO ---
    public void registrarPeso(int mascotaId, double nuevoPeso) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String hoy = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        ContentValues v = new ContentValues();
        v.put("mascota_id", mascotaId);
        v.put("peso", nuevoPeso);
        v.put("fecha", hoy);
        db.insert(DatabaseHelper.TABLE_PESO, null, v);

        ContentValues updateMascota = new ContentValues();
        updateMascota.put("peso", nuevoPeso);
        db.update(DatabaseHelper.TABLE_MASCOTAS, updateMascota, "id = ?", new String[]{String.valueOf(mascotaId)});
    }

    public List<String> obtenerHistorialPeso(int mascotaId) {
        List<String> historial = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT fecha, peso FROM " + DatabaseHelper.TABLE_PESO + " WHERE mascota_id = ? ORDER BY id DESC",
                new String[]{String.valueOf(mascotaId)});
        if (c.moveToFirst()) {
            do {
                historial.add(c.getString(0) + "  →  " + c.getDouble(1) + " kg");
            } while (c.moveToNext());
        }
        c.close();
        return historial;
    }

    // --- REGISTROS DE SALUD Y AGENDA GLOBAL ---
    public long insertarRegistroSalud(RegistroSalud r) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("mascota_id", r.getMascotaId());
        values.put("tipo", r.getTipo());
        values.put("descripcion", r.getDescripcion());
        values.put("fecha_aplicacion", r.getFechaAplicacion());
        values.put("fecha_proximo", r.getFechaProximoRecordatorio());
        values.put("notas", r.getNotas());
        return db.insert(DatabaseHelper.TABLE_SALUD, null, values);
    }

    public List<RegistroSalud> obtenerHistorialFiltrado(int mascotaId, String tipoFiltro, String textoBusqueda) {
        List<RegistroSalud> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT * FROM " + DatabaseHelper.TABLE_SALUD + " WHERE mascota_id = ?";
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(mascotaId));

        if (tipoFiltro != null && !tipoFiltro.equals("Todos")) {
            sql += " AND tipo = ?";
            args.add(tipoFiltro);
        }
        if (textoBusqueda != null && !textoBusqueda.trim().isEmpty()) {
            sql += " AND (descripcion LIKE ? OR notas LIKE ?)";
            args.add("%" + textoBusqueda.trim() + "%");
            args.add("%" + textoBusqueda.trim() + "%");
        }
        sql += " ORDER BY id DESC";

        Cursor c = db.rawQuery(sql, args.toArray(new String[0]));
        if (c.moveToFirst()) {
            do {
                lista.add(new RegistroSalud(
                        c.getInt(0), c.getInt(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5), c.getString(6)
                ));
            } while (c.moveToNext());
        }
        c.close();
        return lista;
    }

    public void eliminarRegistroSalud(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_SALUD, "id = ?", new String[]{String.valueOf(id)});
    }

    public List<RegistroSalud> obtenerProximosCuidadosGlobales() {
        List<RegistroSalud> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT s.*, m.nombre FROM " + DatabaseHelper.TABLE_SALUD + " s " +
                "INNER JOIN " + DatabaseHelper.TABLE_MASCOTAS + " m ON s.mascota_id = m.id " +
                "WHERE s.fecha_proximo IS NOT NULL AND s.fecha_proximo != '' ORDER BY s.id DESC LIMIT 5";
        Cursor c = db.rawQuery(sql, null);
        if (c.moveToFirst()) {
            do {
                RegistroSalud r = new RegistroSalud(
                        c.getInt(0), c.getInt(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5), c.getString(6)
                );
                r.setNombreMascota(c.getString(7));
                lista.add(r);
            } while (c.moveToNext());
        }
        c.close();
        return lista;
    }
}