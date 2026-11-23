package com.example.parcial3_vitapaw.model;

public class RegistroSalud {
    private int id;
    private int mascotaId;
    private String nombreMascota; // Auxiliar para Agenda Global
    private String tipo; // "Vacuna", "Desparasitación", "Consulta Veterinaria"
    private String descripcion;
    private String fechaAplicacion;
    private String fechaProximoRecordatorio;
    private String notas;

    public RegistroSalud() {}

    public RegistroSalud(int id, int mascotaId, String tipo, String descripcion, String fechaAplicacion, String fechaProximoRecordatorio, String notas) {
        this.id = id;
        this.mascotaId = mascotaId;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fechaAplicacion = fechaAplicacion;
        this.fechaProximoRecordatorio = fechaProximoRecordatorio;
        this.notas = notas;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMascotaId() { return mascotaId; }
    public void setMascotaId(int mascotaId) { this.mascotaId = mascotaId; }
    public String getNombreMascota() { return nombreMascota; }
    public void setNombreMascota(String nombreMascota) { this.nombreMascota = nombreMascota; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getFechaAplicacion() { return fechaAplicacion; }
    public void setFechaAplicacion(String fechaAplicacion) { this.fechaAplicacion = fechaAplicacion; }
    public String getFechaProximoRecordatorio() { return fechaProximoRecordatorio; }
    public void setFechaProximoRecordatorio(String fechaProximoRecordatorio) { this.fechaProximoRecordatorio = fechaProximoRecordatorio; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}