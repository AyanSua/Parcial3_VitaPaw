package com.example.parcial3_vitapaw.model;

public class Mascota {
    private int id;
    private String nombre;
    private String especie;
    private String raza;
    private String fechaNacimiento;
    private double peso;
    private String fotoUri;
    private String alergias;
    private String microchip;
    private boolean esterilizado;

    public Mascota() {}

    public Mascota(int id, String nombre, String especie, String raza, String fechaNacimiento,
                   double peso, String fotoUri, String alergias, String microchip, boolean esterilizado) {
        this.id = id;
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.fechaNacimiento = fechaNacimiento;
        this.peso = peso;
        this.fotoUri = fotoUri;
        this.alergias = alergias;
        this.microchip = microchip;
        this.esterilizado = esterilizado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }
    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public double getPeso() { return peso; }
    public void setPeso(double peso) { this.peso = peso; }
    public String getFotoUri() { return fotoUri; }
    public void setFotoUri(String fotoUri) { this.fotoUri = fotoUri; }
    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }
    public String getMicrochip() { return microchip; }
    public void setMicrochip(String microchip) { this.microchip = microchip; }
    public boolean isEsterilizado() { return esterilizado; }
    public void setEsterilizado(boolean esterilizado) { this.esterilizado = esterilizado; }
}