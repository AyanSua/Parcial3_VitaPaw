package com.example.parcial3_vitapaw.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "vitapaw.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_MASCOTAS = "mascotas";
    public static final String TABLE_SALUD = "registros_salud";
    public static final String TABLE_PESO = "historial_peso";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_MASCOTAS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "especie TEXT NOT NULL, " +
                "raza TEXT, " +
                "fecha_nacimiento TEXT, " +
                "peso REAL, " +
                "foto_uri TEXT, " +
                "alergias TEXT, " +
                "microchip TEXT, " +
                "esterilizado INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_SALUD + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "mascota_id INTEGER NOT NULL, " +
                "tipo TEXT NOT NULL, " +
                "descripcion TEXT NOT NULL, " +
                "fecha_aplicacion TEXT NOT NULL, " +
                "fecha_proximo TEXT, " +
                "notas TEXT, " +
                "FOREIGN KEY(mascota_id) REFERENCES " + TABLE_MASCOTAS + "(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE " + TABLE_PESO + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "mascota_id INTEGER NOT NULL, " +
                "peso REAL NOT NULL, " +
                "fecha TEXT NOT NULL, " +
                "FOREIGN KEY(mascota_id) REFERENCES " + TABLE_MASCOTAS + "(id) ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PESO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SALUD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MASCOTAS);
        onCreate(db);
    }
}