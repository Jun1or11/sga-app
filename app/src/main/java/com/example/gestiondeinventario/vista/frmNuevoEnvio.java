package com.example.gestiondeinventario.vista;

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
import com.example.gestiondeinventario.datos.dao.EnvioDao;
import com.example.gestiondeinventario.datos.repositorio.EnvioRepositorio;
import com.example.gestiondeinventario.datos.repositorio.ProductoRepositorio;
import com.example.gestiondeinventario.modelo.Empresa;
import com.example.gestiondeinventario.modelo.EnvioDetalle;
import com.example.gestiondeinventario.modelo.Producto;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Registro de un envío: empresa destino y productos con sus cantidades.
 * Al guardar, el stock se descuenta automáticamente.
 */
public class frmNuevoEnvio extends AppCompatActivity {

    private EnvioRepositorio repositorio;
    private ProductoRepositorio productoRepositorio;

    private Spinner spinnerEmpresa;
    private Spinner spinnerProducto;
    private EditText etCantidad;
    private EditText etObservaciones;
    private LinearLayout contenedorItems;
    private TextView tvSinItems;
    private TextView tvTotalUnidades;

    private final List<Empresa> empresas = new ArrayList<>();
    private final List<Producto> productos = new ArrayList<>();
    private final List<EnvioDetalle> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_nuevo_envio);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        repositorio = new EnvioRepositorio(this);
        productoRepositorio = new ProductoRepositorio(this);

        spinnerEmpresa = findViewById(R.id.spinner_empresa);
        spinnerProducto = findViewById(R.id.spinner_producto);
        etCantidad = findViewById(R.id.et_cantidad);
        etObservaciones = findViewById(R.id.et_observaciones);
        contenedorItems = findViewById(R.id.container_items);
        tvSinItems = findViewById(R.id.tv_sin_items);
        tvTotalUnidades = findViewById(R.id.tv_total_unidades);

        cargarEmpresas();
        cargarProductos();

        MaterialButton btnAgregar = findViewById(R.id.btn_agregar_producto);
        btnAgregar.setOnClickListener(v -> agregarItem());

        MaterialButton btnRegistrar = findViewById(R.id.btn_registrar_envio);
        btnRegistrar.setOnClickListener(v -> registrarEnvio());

        renderizarItems();

        spinnerProducto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < productos.size()) {
                    Producto p = productos.get(position);
                    if (p.getStock() <= 0) {
                        Toast.makeText(frmNuevoEnvio.this,
                                "Ese producto no tiene stock disponible", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (repositorio != null) repositorio.cerrar();
        if (productoRepositorio != null) productoRepositorio.cerrar();
        super.onDestroy();
    }

    private void cargarEmpresas() {
        empresas.clear();
        empresas.addAll(repositorio.obtenerEmpresas());

        List<String> nombres = new ArrayList<>();
        for (Empresa empresa : empresas) {
            nombres.add(empresa.getNombre()
                    + (empresa.getRuc() != null && !empresa.getRuc().isEmpty()
                    ? " · RUC " + empresa.getRuc() : ""));
        }
        if (nombres.isEmpty()) nombres.add("Sin empresas registradas");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nombres);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEmpresa.setAdapter(adapter);
    }

    private void cargarProductos() {
        productos.clear();
        productos.addAll(productoRepositorio.obtenerTodos());

        List<String> nombres = new ArrayList<>();
        for (Producto producto : productos) {
            nombres.add(producto.getNombre() + " · " + producto.getCodigo()
                    + " · stock " + producto.getStock());
        }
        if (nombres.isEmpty()) nombres.add("Sin productos registrados");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nombres);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProducto.setAdapter(adapter);
    }

    private void agregarItem() {
        if (productos.isEmpty()) {
            Toast.makeText(this, "No hay productos registrados", Toast.LENGTH_SHORT).show();
            return;
        }

        int posicion = spinnerProducto.getSelectedItemPosition();
        if (posicion < 0 || posicion >= productos.size()) {
            Toast.makeText(this, "Selecciona un producto", Toast.LENGTH_SHORT).show();
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(texto(etCantidad));
        } catch (NumberFormatException e) {
            etCantidad.setError("Ingresa una cantidad válida");
            etCantidad.requestFocus();
            return;
        }

        if (cantidad <= 0) {
            etCantidad.setError("La cantidad debe ser mayor a 0");
            etCantidad.requestFocus();
            return;
        }

        Producto producto = productos.get(posicion);
        EnvioDetalle existente = null;
        for (EnvioDetalle detalle : items) {
            if (detalle.getProductoId() == producto.getId()) {
                existente = detalle;
                break;
            }
        }

        int yaAgregado = existente == null ? 0 : existente.getCantidad();
        if (yaAgregado + cantidad > producto.getStock()) {
            etCantidad.setError("Solo hay " + producto.getStock()
                    + " disponibles" + (yaAgregado > 0 ? " (ya agregaste " + yaAgregado + ")" : ""));
            etCantidad.requestFocus();
            return;
        }

        if (existente != null) {
            existente.setCantidad(existente.getCantidad() + cantidad);
        } else {
            items.add(new EnvioDetalle(producto.getId(), producto.getNombre(), cantidad));
        }

        etCantidad.setText("");
        renderizarItems();
    }

    private void renderizarItems() {
        contenedorItems.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < items.size(); i++) {
            final int indice = i;
            EnvioDetalle detalle = items.get(i);

            View row = inflater.inflate(R.layout.item_envio_producto, contenedorItems, false);
            TextView tvNombre = row.findViewById(R.id.tv_item_nombre);
            TextView tvDetalle = row.findViewById(R.id.tv_item_detalle);
            TextView tvCantidad = row.findViewById(R.id.tv_item_cantidad);
            TextView btnQuitar = row.findViewById(R.id.btn_quitar_item);

            tvNombre.setText(detalle.getProductoNombre());
            tvDetalle.setText("Stock disponible: " + stockProducto(detalle.getProductoId()));
            tvCantidad.setText(detalle.getCantidad() + " uds");

            btnQuitar.setOnClickListener(v -> {
                items.remove(indice);
                renderizarItems();
            });

            contenedorItems.addView(row);
        }

        tvSinItems.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        int total = 0;
        for (EnvioDetalle detalle : items) {
            total += detalle.getCantidad();
        }
        tvTotalUnidades.setText(total + " uds");
    }

    private int stockProducto(int productoId) {
        for (Producto producto : productos) {
            if (producto.getId() == productoId) return producto.getStock();
        }
        return 0;
    }

    private void registrarEnvio() {
        if (items.isEmpty()) {
            Toast.makeText(this, "Agrega al menos un producto", Toast.LENGTH_SHORT).show();
            return;
        }

        int posicion = spinnerEmpresa.getSelectedItemPosition();
        if (posicion < 0 || posicion >= empresas.size()) {
            Toast.makeText(this, "Selecciona una empresa", Toast.LENGTH_SHORT).show();
            return;
        }

        Empresa empresa = empresas.get(posicion);
        String observaciones = texto(etObservaciones);

        EnvioDao.ResultadoCreacion resultado = repositorio.crearEnvio(
                empresa.getId(), observaciones, new ArrayList<>(items));

        if (resultado.exito) {
            Toast.makeText(this, "Envío " + resultado.numero
                    + " registrado. Stock actualizado.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, resultado.mensaje, Toast.LENGTH_LONG).show();
        }
    }

    private String texto(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
