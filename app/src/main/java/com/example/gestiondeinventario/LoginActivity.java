package com.example.gestiondeinventario;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gestiondeinventario.datos.dao.UsuarioDao;
import com.example.gestiondeinventario.datos.repositorio.ActividadRepositorio;
import com.example.gestiondeinventario.modelo.Actividad;
import com.example.gestiondeinventario.modelo.Usuario;
import com.example.gestiondeinventario.util.SesionActual;
import com.example.gestiondeinventario.util.TurnoUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "SGA_PREFS";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_NOMBRE = "nombre";
    public static final String KEY_TURNO = "turno";

    private TextInputEditText editUsername, editPassword;
    private MaterialButton btnLogin;

    private UsuarioDao usuarioDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_login);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            navigateToMain();
            return;
        }

        try {
            usuarioDao = new UsuarioDao(this);
            usuarioDao.abrir();
        } catch (Exception e) {
            Toast.makeText(this, "Error al abrir base de datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String username = editUsername.getText() != null ? editUsername.getText().toString().trim() : "";
        String password = editPassword.getText() != null ? editPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(username)) {
            editUsername.setError("Ingrese su usuario");
            editUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editPassword.setError("Ingrese su contraseña");
            editPassword.requestFocus();
            return;
        }

        // Autenticación contra base de datos SQLite
        try {
            if (usuarioDao == null) {
                usuarioDao = new UsuarioDao(this);
                usuarioDao.abrir();
            }

            Usuario usuario = usuarioDao.login(username, password);

            if (usuario != null) {
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit()
                        .putBoolean(KEY_IS_LOGGED_IN, true)
                        .putString(KEY_USERNAME, usuario.getUsername())
                        .putString(KEY_NOMBRE, usuario.getNombre() != null ? usuario.getNombre() : usuario.getUsername())
                        .putString(KEY_TURNO, usuario.getTurno() != null ? usuario.getTurno() : Usuario.TURNO_MANANA)
                        .apply();

                registrarInicioSesion();

                Toast.makeText(this, "¡Bienvenido, " + usuario.getNombre() + "!", Toast.LENGTH_SHORT).show();
                navigateToMain();
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error en inicio de sesión: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void registrarInicioSesion() {
        ActividadRepositorio actividadRepositorio = null;
        try {
            actividadRepositorio = new ActividadRepositorio(this);
            actividadRepositorio.registrar(Actividad.TIPO_SESION_INICIO,
                    "inició sesión (" + TurnoUtil.etiqueta(SesionActual.getTurno(this)).toLowerCase() + ")");
        } catch (Exception e) {
            // No bloquear el login si falla el registro de actividad
        } finally {
            if (actividadRepositorio != null) actividadRepositorio.cerrar();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usuarioDao != null) {
            usuarioDao.cerrar();
        }
    }
}