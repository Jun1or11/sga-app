package com.example.gestiondeinventario.vista;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.gestiondeinventario.R;
import com.example.gestiondeinventario.datos.repositorio.EnvioRepositorio;
import com.example.gestiondeinventario.modelo.Empresa;
import com.example.gestiondeinventario.modelo.Envio;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Historial de envíos del almacén a empresas.
 */
public class frmEnvios extends AppCompatActivity {

    private EnvioRepositorio repositorio;
    private LinearLayout contenedor;
    private TextView tvVacio;
    private TextView tvTotal;
    private TextView tvRegistrados;
    private TextView tvUnidades;
    private Spinner spinnerEmpresa;

    private final List<Empresa> empresas = new ArrayList<>();
    private String filtroEstado = null;
    private int filtroEmpresaId = -1;
    private boolean configurandoFiltros = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_envios);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        repositorio = new EnvioRepositorio(this);

        contenedor = findViewById(R.id.container_envios);
        tvVacio = findViewById(R.id.tv_envios_vacio);
        tvTotal = findViewById(R.id.tv_envios_total);
        tvRegistrados = findViewById(R.id.tv_envios_registrados);
        tvUnidades = findViewById(R.id.tv_envios_unidades);
        spinnerEmpresa = findViewById(R.id.spinner_filtro_empresa);

        MaterialButton btnNuevo = findViewById(R.id.btn_nuevo_envio);
        btnNuevo.setOnClickListener(v ->
                startActivity(new Intent(this, frmNuevoEnvio.class)));

        MaterialButton btnEmpresa = findViewById(R.id.btn_nueva_empresa);
        btnEmpresa.setOnClickListener(v -> mostrarDialogoEmpresa());

        configurarChips();
        cargarEmpresas();
        configurandoFiltros = false;
        cargarEnvios();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (repositorio != null) cargarEnvios();
    }

    @Override
    protected void onDestroy() {
        if (repositorio != null) repositorio.cerrar();
        super.onDestroy();
    }

    private void configurarChips() {
        ChipGroup grupo = findViewById(R.id.chip_group_envios);
        grupo.setOnCheckedChangeListener((group1, checkedId) -> {
            if (checkedId == R.id.chip_registrados) {
                filtroEstado = Envio.ESTADO_REGISTRADO;
            } else if (checkedId == R.id.chip_anulados) {
                filtroEstado = Envio.ESTADO_ANULADO;
            } else {
                filtroEstado = null;
            }
            cargarEnvios();
        });
    }

    private void cargarEmpresas() {
        empresas.clear();
        empresas.addAll(repositorio.obtenerEmpresas());

        List<String> nombres = new ArrayList<>();
        nombres.add("Todas las empresas");
        for (Empresa empresa : empresas) {
            nombres.add(empresa.getNombre());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nombres);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEmpresa.setAdapter(adapter);

        if (filtroEmpresaId > 0) {
            for (int i = 0; i < empresas.size(); i++) {
                if (empresas.get(i).getId() == filtroEmpresaId) {
                    spinnerEmpresa.setSelection(i + 1);
                    break;
                }
            }
        }

        spinnerEmpresa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (configurandoFiltros) return;
                if (position <= 0 || position > empresas.size()) {
                    filtroEmpresaId = -1;
                } else {
                    filtroEmpresaId = empresas.get(position - 1).getId();
                }
                cargarEnvios();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void cargarEnvios() {
        List<Envio> todos = repositorio.obtenerTodos(null);

        int total = todos.size();
        int registrados = 0;
        int unidades = 0;
        for (Envio envio : todos) {
            if (Envio.ESTADO_REGISTRADO.equals(envio.getEstado())) {
                registrados++;
                unidades += envio.getUnidades();
            }
        }

        tvTotal.setText(String.valueOf(total));
        tvRegistrados.setText(String.valueOf(registrados));
        tvUnidades.setText(String.valueOf(unidades));

        List<Envio> filtrados = new ArrayList<>();
        for (Envio envio : todos) {
            if (filtroEstado != null && !filtroEstado.equals(envio.getEstado())) continue;
            if (filtroEmpresaId > 0 && envio.getEmpresaId() != filtroEmpresaId) continue;
            filtrados.add(envio);
        }

        contenedor.removeAllViews();
        if (filtrados.isEmpty()) {
            tvVacio.setVisibility(View.VISIBLE);
            return;
        }
        tvVacio.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        for (Envio envio : filtrados) {
            View item = inflater.inflate(R.layout.item_envio, contenedor, false);

            TextView tvNumero = item.findViewById(R.id.tv_envio_numero);
            TextView tvEstado = item.findViewById(R.id.tv_envio_estado);
            TextView tvEmpresa = item.findViewById(R.id.tv_envio_empresa);
            TextView tvFecha = item.findViewById(R.id.tv_envio_fecha);
            TextView tvUnidadesEnvio = item.findViewById(R.id.tv_envio_unidades);

            tvNumero.setText(envio.getNumero());
            tvEstado.setText(envio.getEstado());
            tvEstado.setTextColor(getColor(colorEstado(envio.getEstado())));
            tvEmpresa.setText("Enviado a: " + envio.getEmpresaNombre());
            tvFecha.setText(formato.format(new Date(envio.getFecha()))
                    + " · " + envio.getItems() + (envio.getItems() == 1 ? " producto" : " productos")
                    + " · " + envio.getUnidades() + " uds");
            tvUnidadesEnvio.setText(envio.getUnidades() + " unidades despachadas");

            item.setOnClickListener(v -> {
                Intent intent = new Intent(this, frmDetalleEnvio.class);
                intent.putExtra(frmDetalleEnvio.EXTRA_ENVIO_ID, envio.getId());
                startActivity(intent);
            });

            contenedor.addView(item);
        }
    }

    private int colorEstado(String estado) {
        if (Envio.ESTADO_REGISTRADO.equals(estado)) return R.color.success;
        return R.color.on_surface_variant;
    }

    private void mostrarDialogoEmpresa() {
        View vista = LayoutInflater.from(this).inflate(R.layout.dialog_nueva_empresa, null);
        EditText etNombre = vista.findViewById(R.id.et_empresa_nombre);
        EditText etRuc = vista.findViewById(R.id.et_empresa_ruc);
        EditText etTelefono = vista.findViewById(R.id.et_empresa_telefono);
        EditText etContacto = vista.findViewById(R.id.et_empresa_contacto);

        new AlertDialog.Builder(this)
                .setTitle("Nueva empresa")
                .setView(vista)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        Toast.makeText(this, "Ingresa el nombre de la empresa", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    long id = repositorio.agregarEmpresa(
                            nombre,
                            etRuc.getText().toString().trim(),
                            etTelefono.getText().toString().trim(),
                            etContacto.getText().toString().trim()
                    );
                    if (id > 0) {
                        Toast.makeText(this, "Empresa registrada", Toast.LENGTH_SHORT).show();
                        cargarEmpresas();
                    } else {
                        Toast.makeText(this, "No se pudo guardar. ¿La empresa ya existe?",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
