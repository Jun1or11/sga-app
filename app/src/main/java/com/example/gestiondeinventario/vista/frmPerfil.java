package com.example.gestiondeinventario.vista;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gestiondeinventario.LoginActivity;
import com.example.gestiondeinventario.R;
import com.example.gestiondeinventario.controlador.UsuarioControlador;
import com.example.gestiondeinventario.modelo.Usuario;

public class frmPerfil extends Fragment {

    private static final String TAG = "frmPerfil";
    private UsuarioControlador usuarioControlador;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frm_perfil, container, false);

        try {
            usuarioControlador = new UsuarioControlador(requireContext());
            usuarioControlador.abrir();
        } catch (Exception e) {
            Log.e(TAG, "Error init usuarioControlador", e);
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

        return view;
    }

    private void cargarUsuario(View view, String username) {
        if (username.isEmpty()) return;

        try {
            Usuario usuario = usuarioControlador.obtenerPorUsername(username);
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
                if (tvRol != null) tvRol.setText(capitalize(usuario.getRol()));
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
        if (usuarioControlador != null) usuarioControlador.cerrar();
    }
}