package com.example.gestiondeinventario;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.example.gestiondeinventario.vista.frmInicio;
import com.example.gestiondeinventario.vista.frmInventario;
import com.example.gestiondeinventario.vista.frmPerfil;
import com.example.gestiondeinventario.vista.frmReportes;
import com.example.gestiondeinventario.vista.frmEscanear;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.act_principal);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new frmInicio();
            } else if (itemId == R.id.nav_inventory) {
                selectedFragment = new frmInventario();
            } else if (itemId == R.id.nav_scan) {
                selectedFragment = new frmEscanear();
            } else if (itemId == R.id.nav_reports) {
                selectedFragment = new frmReportes();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new frmPerfil();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    public void setSelectedTab(int id) {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(id);
    }
}