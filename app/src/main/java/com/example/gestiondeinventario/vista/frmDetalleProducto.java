package com.example.gestiondeinventario.vista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.gestiondeinventario.R;
import com.example.gestiondeinventario.datos.repositorio.ProductoRepositorio;
import com.example.gestiondeinventario.modelo.Producto;
import com.example.gestiondeinventario.negocio.ProductoNegocio;
import com.google.android.material.chip.Chip;

public class frmDetalleProducto extends AppCompatActivity {

    private static final int REQUEST_EDIT = 1001;

    private Producto producto;
    private TextView stockTexto, estadoTexto;

    private ProductoRepositorio repositorio;
    private ProductoNegocio negocio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_detalle_producto);

        repositorio = new ProductoRepositorio(this);
        negocio = new ProductoNegocio();

        producto = (Producto) getIntent().getSerializableExtra("producto");
        if (producto == null) {
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView nombreTexto = findViewById(R.id.detail_name);
        TextView skuTexto = findViewById(R.id.detail_sku);
        Chip categoriaChip = findViewById(R.id.detail_category);
        stockTexto = findViewById(R.id.detail_stock);
        estadoTexto = findViewById(R.id.detail_status);
        TextView minStockTexto = findViewById(R.id.detail_min_stock);
        TextView ubicacionTexto = findViewById(R.id.detail_location);
        TextView precioTexto = findViewById(R.id.detail_price);

        nombreTexto.setText(producto.getNombre());
        skuTexto.setText("Código: " + producto.getCodigo());
        categoriaChip.setText(producto.getCategoria());
        minStockTexto.setText(String.valueOf(producto.getStockMinimo()));
        ubicacionTexto.setText(producto.getUbicacion());
        precioTexto.setText("S/ " + String.format("%.2f", producto.getPrecio()));

        actualizarStockUI();

        findViewById(R.id.btn_increase).setOnClickListener(v -> {
            producto.setStock(producto.getStock() + 1);
            actualizarRepositorio();
            actualizarStockUI();
        });

        findViewById(R.id.btn_decrease).setOnClickListener(v -> {
            if (producto.getStock() > 0) {
                producto.setStock(producto.getStock() - 1);
                actualizarRepositorio();
                actualizarStockUI();
            }
        });

        findViewById(R.id.btn_edit).setOnClickListener(v -> {
            Intent intent = new Intent(this, frmInsertarProducto.class);
            intent.putExtra("producto", producto);
            startActivityForResult(intent, REQUEST_EDIT);
        });

        findViewById(R.id.btn_delete).setOnClickListener(v -> {
            repositorio.eliminarProducto(producto);
            Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
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
        TextView ubicacionTexto = findViewById(R.id.detail_location);
        TextView precioTexto = findViewById(R.id.detail_price);

        nombreTexto.setText(p.getNombre());
        skuTexto.setText("Código: " + p.getCodigo());
        categoriaChip.setText(p.getCategoria());
        minStockTexto.setText(String.valueOf(p.getStockMinimo()));
        ubicacionTexto.setText(p.getUbicacion());
        precioTexto.setText("S/ " + String.format("%.2f", p.getPrecio()));

        actualizarStockUI();
    }

    private void actualizarRepositorio() {
        repositorio.actualizarProducto(producto);
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
    }
}