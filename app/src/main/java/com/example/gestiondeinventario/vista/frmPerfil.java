package com.example.gestiondeinventario.vista;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.gestiondeinventario.LoginActivity;
import com.example.gestiondeinventario.R;
import com.example.gestiondeinventario.datos.dao.UsuarioDao;
import com.example.gestiondeinventario.datos.repositorio.ActividadRepositorio;
import com.example.gestiondeinventario.modelo.Actividad;
import com.example.gestiondeinventario.modelo.Usuario;
import com.example.gestiondeinventario.util.PreferenciasApp;
import com.example.gestiondeinventario.util.SesionActual;
import com.example.gestiondeinventario.util.TurnoUtil;
import com.google.android.material.textfield.TextInputEditText;

public class frmPerfil extends Fragment {

    private static final String TAG = "frmPerfil";
    private UsuarioDao usuarioDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frm_perfil, container, false);

        try {
            usuarioDao = new UsuarioDao(requireContext());
            usuarioDao.abrir();
        } catch (Exception e) {
            Log.e(TAG, "Error init usuarioDao", e);
            Toast.makeText(getContext(), "Error BD: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return view;
        }

        // Obtener username de SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String username = prefs.getString(LoginActivity.KEY_USERNAME, "");

        // Cargar datos del usuario desde BD
        cargarUsuario(view, username);

        // Botón cerrar sesión
        View btnLogout = view.findViewById(R.id.btn_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> cerrarSesion());
        }

        // Acceso a notificaciones
        View btnNotificaciones = view.findViewById(R.id.btn_notificaciones_perfil);
        if (btnNotificaciones != null) {
            btnNotificaciones.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), frmNotificaciones.class)));
        }

        View btnEscaner = view.findViewById(R.id.btn_config_escaner);
        if (btnEscaner != null) {
            btnEscaner.setOnClickListener(v -> mostrarDialogoEscaner());
        }

        View btnPassword = view.findViewById(R.id.btn_cambiar_password);
        if (btnPassword != null) {
            btnPassword.setOnClickListener(v -> mostrarDialogoPassword());
        }

        View btnAcercaDe = view.findViewById(R.id.btn_acerca_de);
        if (btnAcercaDe != null) {
            btnAcercaDe.setOnClickListener(v -> mostrarAcercaDe());
        }

        configurarTema(view);

        return view;
    }

    private void configurarTema(View view) {
        Switch switchTema = view.findViewById(R.id.switch_tema_oscuro);
        if (switchTema == null) return;

        switchTema.setOnCheckedChangeListener(null);
        switchTema.setChecked(PreferenciasApp.isTemaOscuro(requireContext()));
        switchTema.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferenciasApp.setTemaOscuro(requireContext(), isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });
    }

    private void mostrarDialogoEscaner() {
        View vista = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_config_escaner, null);

        Switch swSonido = vista.findViewById(R.id.switch_escaner_sonido);
        Switch swVibracion = vista.findViewById(R.id.switch_escaner_vibracion);
        Switch swAbrir = vista.findViewById(R.id.switch_escaner_abrir);

        swSonido.setChecked(PreferenciasApp.isEscanerSonido(requireContext()));
        swVibracion.setChecked(PreferenciasApp.isEscanerVibracion(requireContext()));
        swAbrir.setChecked(PreferenciasApp.isEscanerAbrirProducto(requireContext()));

        new AlertDialog.Builder(requireContext())
                .setTitle("Configurar escáner")
                .setView(vista)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    PreferenciasApp.guardarEscaner(requireContext(),
                            swSonido.isChecked(), swVibracion.isChecked(), swAbrir.isChecked());
                    Toast.makeText(getContext(), "Preferencias del escáner guardadas",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoPassword() {
        View vista = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_cambiar_password, null);

        TextInputEditText etActual = vista.findViewById(R.id.et_password_actual);
        TextInputEditText etNueva = vista.findViewById(R.id.et_password_nueva);
        TextInputEditText etConfirmar = vista.findViewById(R.id.et_password_confirmar);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Cambiar contraseña")
                .setView(vista)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String actual = texto(etActual);
                    String nueva = texto(etNueva);
                    String confirmar = texto(etConfirmar);

                    if (actual.isEmpty()) {
                        etActual.setError("Ingresa tu contraseña actual");
                        etActual.requestFocus();
                        return;
                    }
                    if (nueva.length() < 4) {
                        etNueva.setError("Mínimo 4 caracteres");
                        etNueva.requestFocus();
                        return;
                    }
                    if (!nueva.equals(confirmar)) {
                        etConfirmar.setError("Las contraseñas no coinciden");
                        etConfirmar.requestFocus();
                        return;
                    }

                    String username = SesionActual.getUsername(requireContext());
                    if (username.isEmpty()) {
                        Toast.makeText(getContext(), "No hay una sesión activa",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (usuarioDao.cambiarPassword(username, actual, nueva)) {
                        Toast.makeText(getContext(), "Contraseña actualizada correctamente",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        etActual.setError("Contraseña actual incorrecta");
                        etActual.requestFocus();
                    }
                }));

        dialog.show();
    }

    private void mostrarAcercaDe() {
        String version = "";
        try {
            PackageInfo info = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0);
            if (info.versionName != null) version = info.versionName;
        } catch (Exception ignored) {
        }

        String mensaje = "SGA · Almacén Tecnológico\n"
                + (version.isEmpty() ? "" : "Versión " + version + "\n\n")
                + "Sistema de gestión de inventario para productos tecnológicos.\n\n"
                + "Incluye control de stock, escaneo de códigos, envíos a empresas, "
                + "turnos, notificaciones y reportes.\n\n"
                + "Proyecto académico desarrollado en Android Studio.";

        new AlertDialog.Builder(requireContext())
                .setTitle("Acerca de")
                .setMessage(mensaje)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private String texto(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void cargarUsuario(View view, String username) {
        TextView tvHoraSalida = view.findViewById(R.id.tv_hora_salida);
        if (tvHoraSalida != null) {
            tvHoraSalida.setText("Hora de salida: "
                    + TurnoUtil.horaSalida(SesionActual.getTurno(requireContext())));
        }

        if (username.isEmpty()) return;

        try {
            Usuario usuario = usuarioDao.obtenerPorUsername(username);
            if (usuario != null) {
                // Actualizar vistas
                TextView tvIniciales = view.findViewById(R.id.tv_user_initials);
                TextView tvNombre = view.findViewById(R.id.tv_user_name);
                TextView tvRol = view.findViewById(R.id.tv_user_role);

                if (tvIniciales != null) {
                    String iniciales = "";
                    if (usuario.getNombre() != null && !usuario.getNombre().isEmpty()) {
                        String[] partes = usuario.getNombre().split(" ");
                        for (String p : partes) {
                            if (!p.isEmpty()) iniciales += p.charAt(0);
                            if (iniciales.length() >= 2) break;
                        }
                    } else {
                        iniciales = username.substring(0, Math.min(2, username.length())).toUpperCase();
                    }
                    tvIniciales.setText(iniciales);
                }
                if (tvNombre != null) tvNombre.setText(usuario.getNombre() != null ? usuario.getNombre() : username);
                if (tvRol != null) {
                    tvRol.setText(capitalize(usuario.getRol()) + " · "
                            + TurnoUtil.etiqueta(usuario.getTurno()).toLowerCase());
                }
                if (tvHoraSalida != null) {
                    tvHoraSalida.setText("Hora de salida: " + TurnoUtil.horaSalida(usuario.getTurno()));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cargarUsuario", e);
            Toast.makeText(getContext(), "Error cargando perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private void cerrarSesion() {
        // Registrar el cierre ANTES de borrar las preferencias (se necesita el nombre)
        ActividadRepositorio actividadRepositorio = null;
        try {
            actividadRepositorio = new ActividadRepositorio(requireContext());
            actividadRepositorio.registrar(Actividad.TIPO_SESION_FIN,
                    "cerró sesión (" + TurnoUtil.etiqueta(SesionActual.getTurno(requireContext())).toLowerCase() + ")");
        } catch (Exception e) {
            Log.e(TAG, "Error registrando cierre de sesión", e);
        } finally {
            if (actividadRepositorio != null) actividadRepositorio.cerrar();
        }

        SharedPreferences prefs = requireContext().getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        Toast.makeText(getContext(), "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (usuarioDao != null) usuarioDao.cerrar();
    }
}