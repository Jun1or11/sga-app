package com.example.gestiondeinventario.modelo;

import java.io.Serializable;

public class Usuario implements Serializable {

    public static final String TURNO_MANANA = "mañana";
    public static final String TURNO_NOCHE = "noche";

    private int id;
    private String username;
    private String password;
    private String nombre;
    private String rol;
    private String turno;

    public Usuario(int id, String username, String password, String nombre, String rol) {
        this(id, username, password, nombre, rol, TURNO_MANANA);
    }

    public Usuario(int id, String username, String password, String nombre, String rol, String turno) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nombre = nombre;
        this.rol = rol;
        this.turno = turno;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }
}
