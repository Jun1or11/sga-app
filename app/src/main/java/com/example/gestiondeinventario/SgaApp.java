package com.example.gestiondeinventario;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.gestiondeinventario.util.PreferenciasApp;

/**
 * Aplica el tema guardado antes de crear cualquier Activity.
 */
public class SgaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(
                PreferenciasApp.isTemaOscuro(this)
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
