package com.example.gestiondeinventario.vista;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.example.gestiondeinventario.R;
import com.example.gestiondeinventario.datos.repositorio.ActividadRepositorio;
import com.example.gestiondeinventario.datos.repositorio.ProductoRepositorio;
import com.example.gestiondeinventario.datos.repositorio.ProveedorRepositorio;
import com.example.gestiondeinventario.modelo.Actividad;
import com.example.gestiondeinventario.modelo.Producto;
import com.example.gestiondeinventario.modelo.Proveedor;
import com.example.gestiondeinventario.negocio.ProductoNegocio;
import com.example.gestiondeinventario.util.CategoriaUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

public class frmDetalleProducto extends AppCompatActivity {

    private static final int REQUEST_EDIT = 1001;

    private Producto producto;
    private TextView stockTexto, estadoTexto;

    private MaterialButton btnActualizarStock;
    private TextView tvCambiosPendientes;
    private int stockOriginal;
    private boolean cambiosPendientes = false;

    private ProductoRepositorio repositorio;
    private ProveedorRepositorio proveedorRepositorio;
    private ActividadRepositorio actividadRepositorio;
    private ProductoNegocio negocio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_detalle_producto);

        repositorio = new ProductoRepositorio(this);
        proveedorRepositorio = new ProveedorRepositorio(this);
        actividadRepositorio = new ActividadRepositorio(this);
        negocio = new ProductoNegocio();

        producto = (Producto) getIntent().getSerializableExtra("producto");
        if (producto == null) {
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> salir());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                salir();
            }
        });

        TextView nombreTexto = findViewById(R.id.detail_name);
        TextView skuTexto = findViewById(R.id.detail_sku);
        Chip categoriaChip = findViewById(R.id.detail_category);
        stockTexto = findViewById(R.id.detail_stock);
        estadoTexto = findViewById(R.id.detail_status);
        btnActualizarStock = findViewById(R.id.btn_update_stock);
        tvCambiosPendientes = findViewById(R.id.tv_cambios_pendientes);
        stockOriginal = producto.getStock();
        marcarCambiosPendientes(false);
        btnActualizarStock.setOnClickListener(v -> guardarStock());
        TextView minStockTexto = findViewById(R.id.detail_min_stock);
        TextView precioTexto = findViewById(R.id.detail_price);
        TextView proveedorTexto = findViewById(R.id.detail_provider);

        nombreTexto.setText(producto.getNombre());
        skuTexto.setText("Código: " + producto.getCodigo());
        mostrarCategoria(categoriaChip, producto.getCategoria());
        minStockTexto.setText(String.valueOf(producto.getStockMinimo()));
        mostrarUbicacion(producto.getUbicacion());
        precioTexto.setText("S/ " + String.format("%.2f", producto.getPrecio()));
        proveedorTexto.setText(nombreProveedor(producto.getProveedorId()));

        actualizarStockUI();

        findViewById(R.id.btn_increase).setOnClickListener(v -> cambiarStock(1));

        findViewById(R.id.btn_decrease).setOnClickListener(v -> cambiarStock(-1));

        findViewById(R.id.btn_damage).setOnClickListener(v -> reportarDano());

        findViewById(R.id.btn_edit).setOnClickListener(v -> {
            Intent intent = new Intent(this, frmInsertarProducto.class);
            intent.putExtra("producto", producto);
            startActivityForResult(intent, REQUEST_EDIT);
        });

        findViewById(R.id.btn_delete).setOnClickListener(v -> {
            repositorio.eliminarProducto(producto);
            registrarActividad(Actividad.TIPO_PRODUCTO, "eliminó '" + producto.getNombre() + "'");
            Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }

    private void cambiarStock(int delta) {
        int nuevoStock = producto.getStock() + delta;
        if (nuevoStock < 0) {
            Toast.makeText(this, "El stock no puede ser negativo", Toast.LENGTH_SHORT).show();
            return;
        }
        producto.setStock(nuevoStock);
        actualizarStockUI();
        marcarCambiosPendientes(nuevoStock != stockOriginal);
    }

    private void guardarStock() {
        int nuevoStock = producto.getStock();
        persistirStock();
        Toast.makeText(this, "Stock actualizado a " + nuevoStock + " uds", Toast.LENGTH_SHORT).show();
    }

    private void persistirStock() {
        repositorio.actualizarProducto(producto);
        registrarActividad(Actividad.TIPO_AJUSTE,
                "actualizó el stock de '" + producto.getNombre() + "' a " + producto.getStock() + " uds");
        stockOriginal = producto.getStock();
        marcarCambiosPendientes(false);
    }

    private void marcarCambiosPendientes(boolean pendiente) {
        cambiosPendientes = pendiente;
        if (btnActualizarStock != null) btnActualizarStock.setEnabled(pendiente);
        if (tvCambiosPendientes != null) {
            tvCambiosPendientes.setVisibility(pendiente ? View.VISIBLE : View.GONE);
        }
    }

    private void salir() {
        if (cambiosPendientes) {
            persistirStock();
        }
        finish();
    }

    private void registrarActividad(String tipo, String descripcion) {
        try {
            actividadRepositorio.registrar(tipo, descripcion);
        } catch (Exception e) {
            Log.e("frmDetalleProducto", "Error registrando actividad", e);
        }
    }

    private View crearContenedorEntrada(EditText entrada) {
        FrameLayout contenedor = new FrameLayout(this);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        contenedor.setPadding(padding, 0, padding, 0);
        contenedor.addView(entrada);
        return contenedor;
    }

    private void reportarDano() {
        if (producto.getStock() <= 0) {
            Toast.makeText(this, "No hay stock disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText entrada = new EditText(this);
        entrada.setInputType(InputType.TYPE_CLASS_NUMBER);
        entrada.setText("1");
        entrada.setSelectAllOnFocus(true);

        new AlertDialog.Builder(this)
                .setTitle("Registrar producto dañado")
                .setMessage("¿Cuántas unidades de \"" + producto.getNombre() + "\" están dañadas?")
                .setView(crearContenedorEntrada(entrada))
                .setPositiveButton("Registrar", (dialog, which) -> {
                    int cantidad;
                    try {
                        cantidad = Integer.parseInt(entrada.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        cantidad = 1;
                    }
                    if (cantidad <= 0) cantidad = 1;
                    if (cantidad > producto.getStock()) cantidad = producto.getStock();

                    producto.setStock(producto.getStock() - cantidad);
                    repositorio.actualizarProducto(producto);
                    registrarActividad(Actividad.TIPO_DANO,
                            "reportó " + cantidad + " ud(s) dañada(s) de '" + producto.getNombre() + "'");
                    stockOriginal = producto.getStock();
                    marcarCambiosPendientes(false);
                    actualizarStockUI();
                    Toast.makeText(this, "Daño registrado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT && resultCode == RESULT_OK) {
            // La edición fue exitosa, recargar el producto actualizado
            Producto actualizado = repositorio.obtenerPorId(producto.getId());
            if (actualizado != null) {
                producto = actualizado;
                actualizarVistaConProducto(producto);
            }
            setResult(RESULT_OK); // Propagar a frmInventario
        }
    }

    private void actualizarVistaConProducto(Producto p) {
        TextView nombreTexto = findViewById(R.id.detail_name);
        TextView skuTexto = findViewById(R.id.detail_sku);
        Chip categoriaChip = findViewById(R.id.detail_category);
        TextView minStockTexto = findViewById(R.id.detail_min_stock);
        TextView precioTexto = findViewById(R.id.detail_price);
        TextView proveedorTexto = findViewById(R.id.detail_provider);

        nombreTexto.setText(p.getNombre());
        skuTexto.setText("Código: " + p.getCodigo());
        mostrarCategoria(categoriaChip, p.getCategoria());
        minStockTexto.setText(String.valueOf(p.getStockMinimo()));
        mostrarUbicacion(p.getUbicacion());
        precioTexto.setText("S/ " + String.format("%.2f", p.getPrecio()));
        proveedorTexto.setText(nombreProveedor(p.getProveedorId()));

        stockOriginal = p.getStock();
        marcarCambiosPendientes(false);
        actualizarStockUI();
    }

    private void mostrarUbicacion(String ubicacion) {
        boolean mostrar = ubicacion != null && !ubicacion.trim().isEmpty();

        TextView ubicacionTexto = findViewById(R.id.detail_location);
        View fila = findViewById(R.id.row_detail_location);
        View divisor = findViewById(R.id.divider_detail_location);

        ubicacionTexto.setText(mostrar ? ubicacion : "");
        if (fila != null) fila.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        if (divisor != null) divisor.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    private void mostrarCategoria(Chip chip, String categoria) {
        if (chip == null) return;
        chip.setVisibility(View.VISIBLE);
        chip.setText(CategoriaUtil.normalizar(categoria));
    }

    private String nombreProveedor(int proveedorId) {
        if (proveedorId <= 0) return "Sin proveedor";
        try {
            Proveedor proveedor = proveedorRepositorio.obtenerPorId(proveedorId);
            return proveedor != null ? proveedor.getNombre() : "Sin proveedor";
        } catch (Exception e) {
            Log.e("frmDetalleProducto", "Error obteniendo proveedor", e);
            return "Sin proveedor";
        }
    }

    private void actualizarStockUI() {
        stockTexto.setText(String.valueOf(producto.getStock()));
        String estado = negocio.calcularEstado(producto);
        estadoTexto.setText(estado);

        int color;
        if (estado.equals("Sin stock")) {
            color = ContextCompat.getColor(this, R.color.error);
        } else if (estado.equals("Stock bajo")) {
            color = ContextCompat.getColor(this, R.color.warning);
        } else {
            color = ContextCompat.getColor(this, R.color.success);
        }
        estadoTexto.setTextColor(color);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repositorio != null) repositorio.cerrar();
        if (proveedorRepositorio != null) proveedorRepositorio.cerrar();
        if (actividadRepositorio != null) actividadRepositorio.cerrar();
    }
}